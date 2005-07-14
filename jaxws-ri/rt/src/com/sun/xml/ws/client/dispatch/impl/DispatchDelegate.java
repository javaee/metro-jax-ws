/*
 * $Id: DispatchDelegate.java,v 1.4 2005-07-14 02:01:22 arungupta Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.client.dispatch.impl;

import com.sun.pept.ept.ContactInfo;
import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.encoding.soap.internal.DelegateBase;
import com.sun.xml.ws.client.*;

import javax.xml.ws.BindingProvider;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.xml.ws.binding.soap.BindingImpl;

/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchDelegate extends DelegateBase {

    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());

    public DispatchDelegate() {
    }

    public DispatchDelegate(ContactInfoList contactInfoList) {
        this.contactInfoList = contactInfoList;
    }

    public void send(MessageStruct messageStruct) {
        MessageInfo messageInfo = (MessageInfo) messageStruct;

        ContextMap properties = (ContextMap)
                messageInfo.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        BindingProvider dispatch = (BindingProvider)properties.get(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);

        if (!contactInfoList.iterator().hasNext())
            throw new RuntimeException("can't pickup message encoder/decoder, no ContactInfo!");


        BindingImpl bi = (BindingImpl)dispatch.getBinding();
        String bindingId = bi.getBindingId();
        ContactInfo contactInfo = getContactInfo(contactInfoList, bindingId);
        messageInfo.setEPTFactory(contactInfo);
        messageInfo.setConnection(contactInfo.getConnection(messageInfo));

        MessageDispatcher messageDispatcher =
            contactInfo.getMessageDispatcher(messageInfo);
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
    /*private ContactInfo getContactInfo(ContactInfoListIterator iterator, MessageStruct messageStruct) {
        if (!iterator.hasNext())
            throw new RuntimeException("no next");

        ContactInfo contactInfo = iterator.next();
        //Todo: use Map
        //if fast encoding go to next
         if (isFastEncoding((RequestContext)
                 messageStruct.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY))) {
             if (iterator.hasNext())
                 contactInfo = iterator.next();
             else {
                 if (logger.isLoggable(Level.INFO))     //needs localicalization
                     logger.info("Defaulting to XML Encoding. ");
                 setDefaultEncoding((RequestContext)
                         messageStruct.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY));
             }
         } else
             setDefaultEncoding((RequestContext)
                     messageStruct.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY));

        return contactInfo;
    }
 */
    //TODO: kw-map

     private boolean isFastEncoding(RequestContext requestContext) {
         //fast can only be used with jaxb objects
         if (requestContext.get(BindingProviderProperties.JAXB_CONTEXT_PROPERTY) == null)
             return false;

         String xmlFastEncoding =
                 (String) requestContext.get(BindingProviderProperties.XMLFAST_ENCODING_PROPERTY);
         String acceptEncoding =
                 (String) requestContext.get(BindingProviderProperties.ACCEPT_ENCODING_PROPERTY);
         return xmlFastEncoding != null && BindingProviderProperties.FAST_ENCODING_VALUE.equalsIgnoreCase(xmlFastEncoding) &&
                 (acceptEncoding != null && BindingProviderProperties.FAST_ENCODING_VALUE.equalsIgnoreCase(acceptEncoding));

     }

     private void setDefaultEncoding(RequestContext requestContext) {
         requestContext.put(BindingProviderProperties.XMLFAST_ENCODING_PROPERTY,
                 BindingProviderProperties.XML_ENCODING_VALUE);
         requestContext.put(BindingProviderProperties.ACCEPT_ENCODING_PROPERTY,
                 BindingProviderProperties.XML_ENCODING_VALUE);

     }

}
