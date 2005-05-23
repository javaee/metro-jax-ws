/**
 * $Id: ProviderSED.java,v 1.1 2005-05-23 22:50:23 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server.provider;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import javax.xml.transform.Source;

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
                // invoke() throws only RemoteException
                // Fallthrough

            case MessageStruct.UNCHECKED_EXCEPTION_RESPONSE :
                SOAPRuntimeModel.createFaultInBody(messageInfo.getResponse(),
                        null, null, internalMessage);
                break;
        }
        return internalMessage;
    }

}
