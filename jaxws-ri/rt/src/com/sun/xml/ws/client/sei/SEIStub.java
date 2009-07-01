/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.MEP;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.*;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.SOAPSEIModel;
import com.sun.xml.ws.wsdl.OperationDispatcher;

import javax.xml.namespace.QName;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Stub} that handles method invocations
 * through a strongly-typed endpoint interface.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SEIStub extends Stub implements InvocationHandler {

    @Deprecated
    public SEIStub(WSServiceDelegate owner, BindingImpl binding, SOAPSEIModel seiModel, Tube master, WSEndpointReference epr) {
        super(owner, master, binding, seiModel.getPort(), seiModel.getPort().getAddress(), epr);
        this.seiModel = seiModel;
        this.soapVersion = binding.getSOAPVersion();
        initMethodHandlers();
    }

    public SEIStub(WSPortInfo portInfo, BindingImpl binding, SOAPSEIModel seiModel, WSEndpointReference epr) {
        super(portInfo, binding, seiModel.getPort().getAddress(),epr);
        this.seiModel = seiModel;
        this.soapVersion = binding.getSOAPVersion();
        initMethodHandlers();
    }

    private void initMethodHandlers() {
        Map<WSDLBoundOperation, JavaMethodImpl> syncs = new HashMap<WSDLBoundOperation, JavaMethodImpl>();

        // fill in methodHandlers.
        // first fill in sychronized versions
        for (JavaMethodImpl m : seiModel.getJavaMethods()) {
            if (!m.getMEP().isAsync) {
                SyncMethodHandler handler = new SyncMethodHandler(this, m);
                syncs.put(m.getOperation(), m);
                methodHandlers.put(m.getMethod(), handler);
            }
        }

        for (JavaMethodImpl jm : seiModel.getJavaMethods()) {
            JavaMethodImpl sync = syncs.get(jm.getOperation());
            if (jm.getMEP() == MEP.ASYNC_CALLBACK) {
                Method m = jm.getMethod();
                CallbackMethodHandler handler = new CallbackMethodHandler(
                        this, jm, sync, m.getParameterTypes().length - 1);
                methodHandlers.put(m, handler);
            }
            if (jm.getMEP() == MEP.ASYNC_POLL) {
                Method m = jm.getMethod();
                PollingMethodHandler handler = new PollingMethodHandler(this, jm, sync);
                methodHandlers.put(m, handler);
            }
        }
    }

    public final SOAPSEIModel seiModel;

    public final SOAPVersion soapVersion;

    /**
     * Nullable when there is no associated WSDL Model
     * @return
     */
    public @Nullable
    OperationDispatcher getOperationDispatcher() {
        if(operationDispatcher == null && wsdlPort != null)
            operationDispatcher = new OperationDispatcher(wsdlPort,binding,seiModel);
        return operationDispatcher;
    }

    /**
     * For each method on the port interface we have
     * a {@link MethodHandler} that processes it.
     */
    private final Map<Method, MethodHandler> methodHandlers = new HashMap<Method, MethodHandler>();

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodHandler handler = methodHandlers.get(method);
        if (handler != null) {
            return handler.invoke(proxy, args);
        } else {
            // we handle the other method invocations by ourselves
            try {
                return method.invoke(this, args);
            } catch (IllegalAccessException e) {
                // impossible
                throw new AssertionError(e);
            } catch (IllegalArgumentException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    public final Packet doProcess(Packet request, RequestContext rc, ResponseContextReceiver receiver) {
        return super.process(request, rc, receiver);
    }

    public final void doProcessAsync(Packet request, RequestContext rc, Fiber.CompletionCallback callback) {
        super.processAsync(request, rc, callback);
    }

    protected final @NotNull QName getPortName() {
        return wsdlPort.getName();
    }


    public void setOutboundHeaders(Object... headers) {
        if(headers==null)
            throw new IllegalArgumentException();
        Header[] hl = new Header[headers.length];
        for( int i=0; i<hl.length; i++ ) {
            if(headers[i]==null)
                throw new IllegalArgumentException();
            hl[i] = Headers.create(seiModel.getJAXBContext(),headers[i]);
        }
        super.setOutboundHeaders(hl);
    }
}
