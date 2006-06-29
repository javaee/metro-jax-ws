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
package com.sun.xml.ws.encoding.soap.server;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.MessageInfoUtil;
import javax.xml.transform.Source;
import javax.xml.ws.soap.SOAPBinding;

public class ProviderSED implements InternalEncoder {

    public void toMessageInfo(Object internalMessage, MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

    /*
     * Sets Source in InternalMessage's BodyBlock. If there is an exception
     * in MessageInfo, it is set as fault in BodyBlock
     *
     */
    public InternalMessage toInternalMessage(MessageInfo messageInfo) {
        InternalMessage internalMessage = new InternalMessage();
        switch(messageInfo.getResponseType()) {
            case MessageStruct.NORMAL_RESPONSE :
                Object obj = messageInfo.getResponse();
                if (obj instanceof Source) {
                    BodyBlock bodyBlock = new BodyBlock((Source)obj);
                    internalMessage.setBody(bodyBlock);
                } else {
                    throw new UnsupportedOperationException();
                }
                break;

            case MessageStruct.CHECKED_EXCEPTION_RESPONSE :
                // invoke() doesn't throw any checked exception
                // Fallthrough

            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE :
                RuntimeContext rtContext = MessageInfoUtil.getRuntimeContext(messageInfo);
                BindingImpl bindingImpl = 
                    (BindingImpl)rtContext.getRuntimeEndpointInfo().getBinding();
                String bindingId = bindingImpl.getBindingId();
                if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)) {
                    SOAPRuntimeModel.createFaultInBody(messageInfo.getResponse(),
                            null, null, internalMessage);
                } else if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
                    SOAPRuntimeModel.createSOAP12FaultInBody(messageInfo.getResponse(),
                            null, null, null, internalMessage);
                }
                break;
        }
        return internalMessage;
    }

}
