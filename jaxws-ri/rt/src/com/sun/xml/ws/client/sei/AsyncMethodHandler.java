/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.client.sei;

//import com.sun.tools.ws.wsdl.document.soap.SOAPBinding;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.databinding.ClientCallBridge;
import com.sun.xml.ws.api.databinding.JavaCallInfo;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.client.AsyncInvoker;
import com.sun.xml.ws.client.AsyncResponseImpl;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.ResponseContext;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;

import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Common part between {@link CallbackMethodHandler} and {@link PollingMethodHandler}.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
abstract class AsyncMethodHandler extends MethodHandler {

    AsyncMethodHandler(SEIStub owner, Method m) {
    	super(owner, m);
    }

//    private final ResponseBuilder responseBuilder;
//    /**
//     * Async bean class that has setters for all out parameters
//     */
//    private final @Nullable Class asyncBeanClass;
//
//    AsyncMethodHandler(SEIStub owner, JavaMethodImpl jm, JavaMethodImpl sync) {
//        super(owner, sync);
//
//        List<ParameterImpl> rp = sync.getResponseParameters();
//        int size = 0;
//        for( ParameterImpl param : rp ) {
//            if (param.isWrapperStyle()) {
//                WrapperParameter wrapParam = (WrapperParameter)param;
//                size += wrapParam.getWrapperChildren().size();
//                if (sync.getBinding().getStyle() == Style.DOCUMENT) {
//                    // doc/asyncBeanClass - asyncBeanClass bean is in async signature
//                    // Add 2 or more so that it is considered as async bean case
//                    size += 2;
//                }
//            } else {
//                ++size;
//            }
//        }
//
//        Class tempWrap = null;
//        if (size > 1) {
//            rp = jm.getResponseParameters();
//            for(ParameterImpl param : rp) {
//                if (param.isWrapperStyle()) {
//                    WrapperParameter wrapParam = (WrapperParameter)param;
//                    if (sync.getBinding().getStyle() == Style.DOCUMENT) {
//                        // doc/asyncBeanClass style
//                        tempWrap = (Class)wrapParam.getTypeReference().type;
//                        break;
//                    }
//                    for(ParameterImpl p : wrapParam.getWrapperChildren()) {
//                        if (p.getIndex() == -1) {
//                            tempWrap = (Class)p.getTypeReference().type;
//                            break;
//                        }
//                    }
//                    if (tempWrap != null) {
//                        break;
//                    }
//                } else {
//                    if (param.getIndex() == -1) {
//                        tempWrap = (Class)param.getTypeReference().type;
//                        break;
//                    }
//                }
//            }
//        }
//        asyncBeanClass = tempWrap;
//        
//        switch(size) {
//            case 0 :
//                responseBuilder = buildResponseBuilder(sync, ValueSetterFactory.NONE);
//                break;
//            case 1 :
//                responseBuilder = buildResponseBuilder(sync, ValueSetterFactory.SINGLE);
//                break;
//            default :
//                responseBuilder = buildResponseBuilder(sync, new ValueSetterFactory.AsyncBeanValueSetterFactory(asyncBeanClass));
//        }
//       
//    }

    protected final Response<Object> doInvoke(Object proxy, Object[] args, AsyncHandler handler) {
        
        AsyncInvoker invoker = new SEIAsyncInvoker(proxy, args);
        AsyncResponseImpl<Object> ft = new AsyncResponseImpl<Object>(invoker,handler);
        invoker.setReceiver(ft);
        ft.run();
        return ft;
    }

    private class SEIAsyncInvoker extends AsyncInvoker {
        // snapshot the context now. this is necessary to avoid concurrency issue,
        // and is required by the spec
        private final RequestContext rc = owner.requestContext.copy();
        private final Object[] args;

        SEIAsyncInvoker(Object proxy, Object[] args) {
            this.args = args;
        }

        public void do_run () {    	
        	JavaCallInfo call = new JavaCallInfo(method, args);
//            Packet req = new Packet(createRequestMessage(args));
//            Packet req = dbHandler.createRequestPacket(call);
            Packet req = owner.databinding.serializeRequest(call);

            Fiber.CompletionCallback callback = new Fiber.CompletionCallback() {

                public void onCompletion(@NotNull Packet response) {
                    responseImpl.setResponseContext(new ResponseContext(response));
                    Message msg = response.getMessage();
                    if (msg == null) {
                        return;
                    }
                    try {
                    	Object[] rargs = new Object[1];
                    	JavaCallInfo call = new JavaCallInfo(method, rargs);
                        call = owner.databinding.deserializeResponse(response, call);
                        if (call.getException() != null) {
                            throw call.getException();
                        } else {
                            responseImpl.set(rargs[0], null);
                        }
//                    	dbHandler.readResponse(response, call);
//                    	responseImpl.set(rargs[0], null);
//                        if(msg.isFault()) {
//                            SOAPFaultBuilder faultBuilder = SOAPFaultBuilder.create(msg);
//                            throw faultBuilder.createException(checkedExceptions);
//                        } else {
//                            Object[] rargs = new Object[1];
//                            if (asyncBeanClass != null) {
//                                rargs[0] = asyncBeanClass.newInstance();
//                            }
//                            responseBuilder.readResponse(msg, rargs);
//                            responseImpl.set(rargs[0], null);
//                        }
                   } catch (Throwable t) {
                        if (t instanceof RuntimeException) {
                            if (t instanceof WebServiceException) {
                                responseImpl.set(null, t);
                                return;
                            }
                        }  else if (t instanceof Exception) {
                            responseImpl.set(null, t);
                            return;
                        }
                        //its RuntimeException or some other exception resulting from user error, wrap it in
                        // WebServiceException
                        responseImpl.set(null, new WebServiceException(t));
                    }
                }
                

                public void onCompletion(@NotNull Throwable error) {
                    if (error instanceof WebServiceException) {
                        responseImpl.set(null, error);
                    } else {
                        //its RuntimeException or some other exception resulting from user error, wrap it in
                        // WebServiceException
                        responseImpl.set(null, new WebServiceException(error));
                    }
                }
            };
            owner.doProcessAsync(req, rc, callback);
        }
    }

    ValueGetterFactory getValueGetterFactory() {
        return ValueGetterFactory.ASYNC;
    }

}
