/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

import com.sun.xml.ws.api.databinding.ClientCallBridge;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.ResponseContextReceiver;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.CheckedExceptionImpl;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;
import org.jvnet.ws.databinding.JavaCallInfo;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Holder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link MethodHandler} that handles synchronous method invocations.
 *
 * <p>
 * This class mainly performs the following two tasks:
 * <ol>
 *  <li>Accepts Object[] that represents arguments for a Java method,
 *      and creates {@link JAXBMessage} that represents a request message.
 *  <li>Takes a {@link Message] that represents a response,
 *      and extracts the return value (and updates {@link Holder}s.)
 * </ol>
 *
 * <h2>Creating {@link JAXBMessage}</h2>
 * <p>
 * At the construction time, we prepare {@link BodyBuilder} and {@link MessageFiller}s
 * that know how to move arguments into a {@link Message}.
 * Some arguments go to the payload, some go to headers, still others go to attachments.
 *
 * @author Kohsuke Kawaguchi
 */
final class SyncMethodHandler extends MethodHandler {
//    private ResponseBuilder responseBuilder;
    
    SyncMethodHandler(SEIStub owner, Method m) {
        super(owner, m);
//        responseBuilder = buildResponseBuilder(method, ValueSetterFactory.SYNC);
    }
    
//    SyncMethodHandler(SEIStub owner, JavaMethodImpl method) {
//        super(owner, method);
//        responseBuilder = buildResponseBuilder(method, ValueSetterFactory.SYNC);
//    }

    Object invoke(Object proxy, Object[] args) throws Throwable {
        return invoke(proxy,args,owner.requestContext,owner);
    }

    /**
     * Invokes synchronously, but with the given {@link RequestContext}
     * and {@link ResponseContextReceiver}.
     *
     * @param rc
     *      This {@link RequestContext} is used for invoking this method.
     *      We take this as a separate parameter because of the async invocation
     *      handling, which requires a separate copy.
     */
    Object invoke(Object proxy, Object[] args, RequestContext rc, ResponseContextReceiver receiver) throws Throwable {
    	JavaCallInfo call = owner.databinding.createJavaCallInfo(method, args);
//      Packet req = new Packet(createRequestMessage(args));
        Packet req = (Packet) owner.databinding.serializeRequest(call);
        // process the message
        Packet reply = owner.doProcess(req,rc,receiver);

        Message msg = reply.getMessage();
        if(msg ==null)
            // no reply. must have been one-way
            return null;

        try {
//        	return dbHandler.readResponse(reply, call).getReturnValue();
            call = owner.databinding.deserializeResponse(reply, call);
            if (call.getException() != null) {
                throw call.getException();
            } else {
                return call.getReturnValue();
            }
//            if(msg.isFault()) {
//                SOAPFaultBuilder faultBuilder = SOAPFaultBuilder.create(msg);
//                throw faultBuilder.createException(checkedExceptions);
//            } else {
//                return responseBuilder.readResponse(msg,args);
//            }
        } catch (JAXBException e) {
            throw new DeserializationException("failed.to.read.response",e);
        } catch (XMLStreamException e) {
            throw new DeserializationException("failed.to.read.response",e);
        } finally {
        	if (reply.transportBackChannel != null)
        		reply.transportBackChannel.close();
        }
    }

    ValueGetterFactory getValueGetterFactory() {
        return ValueGetterFactory.SYNC;
    }

}
