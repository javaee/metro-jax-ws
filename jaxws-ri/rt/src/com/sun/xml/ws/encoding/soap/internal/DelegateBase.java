/*
 * $Id: DelegateBase.java,v 1.4 2005-07-14 02:01:23 arungupta Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap.internal;

import com.sun.pept.Delegate;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.ept.ContactInfo;
import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.client.ContextMap;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.binding.soap.BindingImpl;
import com.sun.xml.ws.client.ContactInfoBase;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.util.Iterator;

/**
 * @author JAX-RPC RI Development Team
 */
public class DelegateBase implements Delegate {
    protected ContactInfoList contactInfoList;

    public DelegateBase() {
    }

    public DelegateBase(ContactInfoList contactInfoList) {
        this.contactInfoList = contactInfoList;
    }

    public MessageStruct getMessageStruct() {
        return new MessageInfoBase();
    }

    public void send(MessageStruct messageStruct) {
        MessageInfo messageInfo = (MessageInfo) messageStruct;

        // ContactInfoListIterator iterator = contactInfoList.iterator();
        if (!contactInfoList.iterator().hasNext())
            throw new RuntimeException("can't pickup message encoder/decoder, no ContactInfo!");

        //TODO: use new prop MAP-kw
        ContextMap properties = (ContextMap)
                messageInfo.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        BindingProvider stub = (BindingProvider)properties.get(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);

        BindingImpl bi = (BindingImpl)stub.getBinding();
        String bindingId = bi.getBindingId();
        ContactInfo contactInfo = getContactInfo(contactInfoList, bindingId);

        messageInfo.setEPTFactory(contactInfo);
        MessageDispatcher messageDispatcher = contactInfo.getMessageDispatcher(messageInfo);
        messageDispatcher.send(messageInfo);
    }

    private ContactInfo getContactInfo(ContactInfoList cil, String bindingId){
        ContactInfoListIterator iter = cil.iterator();
        while(iter.hasNext()){
            ContactInfoBase cib = (ContactInfoBase)iter.next();
            if(cib.getBindingId().equals(bindingId))
                return cib;
        }
        //return the first one
        return cil.iterator().next();
    }
}