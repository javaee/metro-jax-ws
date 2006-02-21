/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.encoding.soap.internal;

import com.sun.xml.ws.pept.Delegate;
import com.sun.xml.ws.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.pept.encoding.Decoder;
import com.sun.xml.ws.pept.encoding.Encoder;
import com.sun.xml.ws.pept.ept.ContactInfo;
import com.sun.xml.ws.pept.ept.ContactInfoList;
import com.sun.xml.ws.pept.ept.ContactInfoListIterator;
import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.client.ContextMap;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.ContactInfoBase;
import com.sun.xml.ws.client.ContactInfoListImpl;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.client.WSServiceDelegate;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service;
import java.util.Iterator;

/**
 * @author WS Development Team
 */
public class DelegateBase implements Delegate {
    protected ContactInfoList contactInfoList;
    protected WSServiceDelegate service;

    public DelegateBase() {
    }

    public DelegateBase(ContactInfoList contactInfoList) {
        this.contactInfoList = contactInfoList;
    }

    public DelegateBase(ContactInfoList cil, WSServiceDelegate service) {
       this(cil);
       this.service = service;
    }

    public MessageStruct getMessageStruct() {
        return new MessageInfoBase();
    }

    public void send(MessageStruct messageStruct) {
        MessageInfo messageInfo = (MessageInfo) messageStruct;

        // ContactInfoListIterator iterator = contactInfoList.iterator();
        if (!contactInfoList.iterator().hasNext())
            throw new RuntimeException("can't pickup message encoder/decoder, no ContactInfo!");

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