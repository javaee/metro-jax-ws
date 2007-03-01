/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.client.sei;

//import com.sun.tools.ws.wsdl.document.soap.SOAPBinding;

import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.ResponseContextReceiver;
import com.sun.xml.ws.client.ResponseImpl;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Common part between {@link CallbackMethodHandler} and {@link PollingMethodHandler}.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
abstract class AsyncMethodHandler extends MethodHandler {

    private final JavaMethodImpl jm;
    private final AsyncBuilder asyncBuilder;
    private final SyncMethodHandler core;
    /**
     * Async Wrapper bean.
     */
    //private final Class wrapper;

    protected AsyncMethodHandler(SEIStub owner, JavaMethodImpl jm, SyncMethodHandler core) {
        super(owner);
        this.jm = jm;
        this.core = core;
        
        List<ParameterImpl> rp = jm.getResponseParameters();
        
        
        
        Class tempWrap = null;
        for(ParameterImpl param : rp) {
            if (param.isWrapperStyle()) {
                WrapperParameter wrapParam = (WrapperParameter)param;
                if (core.getJavaMethod().getBinding().getStyle() == Style.DOCUMENT) {
                    // doc/wrapper style
                    tempWrap = (Class)wrapParam.getTypeReference().type;
                    break;
                }
                for(ParameterImpl p : wrapParam.getWrapperChildren()) {
                    if (p.getIndex() == -1) {
                        tempWrap = (Class)p.getTypeReference().type;
                        break;
                    }
                }
                if (tempWrap != null) {
                    break;
                }
            } else {
                if (param.getIndex() == -1) {
                    tempWrap = (Class)param.getTypeReference().type;
                    break;
                }
            }
        }
        Class wrapper = tempWrap;      
        
        rp = core.getJavaMethod().getResponseParameters();
        int size = 0;
        for( ParameterImpl param : rp ) {
            if (param.isWrapperStyle()) {
                WrapperParameter wrapParam = (WrapperParameter)param;
                size += wrapParam.getWrapperChildren().size();
                if (core.getJavaMethod().getBinding().getStyle() == Style.DOCUMENT) {
                    // doc/wrapper - wrapper bean is in async signature
                    // Add 2 so that it is considered as async bean case
                    size += 2;
                }
            } else {
                ++size;
            }
        }
        
        List<AsyncBuilder> builders = new ArrayList<AsyncBuilder>();
        if (size == 0) {
            // no mapping
        } else if (size == 1) {
            ParameterImpl single = null;
            for( ParameterImpl param : rp ) {
                if (param.isWrapperStyle()) {
                    WrapperParameter wrapParam = (WrapperParameter)param;
                    for(ParameterImpl p : wrapParam.getWrapperChildren()) {
                        single = p;
                        break;
                    }
                    if (single != null)
                        break;
                } else {
                    single = param;
                    break;
                }
            }
            assert single != null;
            builders.add(new AsyncBuilder.Filler(single));
        } else {
            for( ParameterImpl param : rp ) {
                switch(param.getOutBinding().kind) {
                case BODY:
                    if(param.isWrapperStyle()) {
                        if(param.getParent().getBinding().isRpcLit())
                            builders.add(new AsyncBuilder.DocLit(wrapper, (WrapperParameter)param));
                        else
                            builders.add(new AsyncBuilder.DocLit(wrapper, (WrapperParameter)param));
                    } else {
                        builders.add(new AsyncBuilder.Bare(wrapper, param));
                    }
                    break;
                case HEADER:
                    builders.add(new AsyncBuilder.Bare(wrapper, param));
                    break;
                case ATTACHMENT:
                    builders.add(new AsyncBuilder.Bare(wrapper, param));
                    break;
                case UNBOUND:
                    /*
                    builders.add(new AsyncBuilder.NullSetter(setter,
                        ResponseBuilder.getVMUninitializedValue(param.getTypeReference().type)));
                     */
                    break;
                default:
                    throw new AssertionError();
                }
            }

        }
        switch(size) {      // Use size, since Composite is creating async bean
        case 0:
            asyncBuilder = AsyncBuilder.NONE;
            break;
        case 1:
            asyncBuilder = builders.get(0);
            break;
        default:
            asyncBuilder = new AsyncBuilder.Composite(builders, wrapper);
        }
       
    }

    protected final Response<Object> doInvoke(Object proxy, Object[] args, AsyncHandler handler) {
        
        AsyncMethodHandler.Invoker invoker = new Invoker(proxy, args);
        ResponseImpl<Object> ft = new ResponseImpl<Object>(invoker,handler);
        invoker.setReceiver(ft);

        owner.getExecutor().execute(ft);
        return ft;
    }

    private class Invoker implements Callable<Object> {
        private final Object proxy;
        private final Object[] args;
        // need to take a copy. required by the spec
        private final RequestContext snapshot = owner.requestContext.copy();
        /**
         * Because of the object instantiation order,
         * we can't take this as a constructor parameter.
         */
        private ResponseContextReceiver receiver;

        public Invoker(Object proxy, Object[] args) {
            this.proxy = proxy;
            this.args = args;
        }

        public Object call() throws Exception {
            assert receiver!=null;
            try {
                // TODO: Calling the sync method has this overhead
                Object[] newArgs;
                Method method = core.getJavaMethod().getMethod();
                int noOfArgs = method.getParameterTypes().length;
                newArgs = new Object[noOfArgs];
                for(int i=0; i < noOfArgs; i++) {
                    if (method.getParameterTypes()[i].isAssignableFrom(Holder.class)) {
                        Holder holder = new Holder();
                        if (i < args.length) {
                            holder.value = args[i];
                        }
                        newArgs[i] = holder;
                    } else {
                        newArgs[i] = args[i];
                    }
                }
                Object returnValue = core.invoke(proxy,newArgs,snapshot,receiver);
                return asyncBuilder.fillAsyncBean(newArgs, returnValue, null);
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    if (t instanceof WebServiceException) {
                        throw (WebServiceException)t;
                    }
                }  else if (t instanceof Exception) {
                    throw (Exception)t;
                }
                throw new WebServiceException(t);
            }
        }

        void setReceiver(ResponseContextReceiver receiver) {
            this.receiver = receiver;
        }
    }
}
