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
package handler.messagehandler.client;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.message.jaxb.JAXBMessage;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.ws.handler.MessageContext;
import java.util.Set;

public class TestHandler implements MessageHandler<MessageHandlerContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(MessageHandlerContext context) {
       JAXBContext jc = ((com.sun.xml.ws.model.AbstractSEIModelImpl)(context.getSEIModel())).getBindingContext().getJAXBContext();
       Message in_message = context.getMessage();
       try {
            JAXBElement obj = in_message.readPayloadAsJAXB(jc.createUnmarshaller());
            if((Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
                handler.messagehandler.client.Hello hello= (handler.messagehandler.client.Hello) obj.getValue();
                hello.setX(hello.getX()+1);
                obj.setValue(hello);
                System.out.println(hello);
            } else {
                handler.messagehandler.client.HelloResponse helloResponse= (handler.messagehandler.client.HelloResponse) obj.getValue();
                helloResponse.setReturn(helloResponse.getReturn()+1);
                obj.setValue(helloResponse);
                System.out.println(helloResponse);
            }
           //Message newMessage = Messages.create(jc.createMarshaller(),obj,context.getWSBinding().getSOAPVersion());
           Message newMessage = Messages.create(jc,obj,context.getWSBinding().getSOAPVersion());
           context.setMessage(newMessage);
       } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean handleFault(MessageHandlerContext context) {
        return true;
    }

    public void close(MessageContext context) {}

}
