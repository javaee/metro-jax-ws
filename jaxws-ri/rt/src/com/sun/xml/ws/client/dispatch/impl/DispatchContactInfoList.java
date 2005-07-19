/*
 * $Id: DispatchContactInfoList.java,v 1.6 2005-07-19 20:41:21 arungupta Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl;


import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.xml.ws.client.*;
import com.sun.xml.ws.client.dispatch.impl.protocol.MessageDispatcherHelper;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.encoding.soap.*;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLEncoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLEncoder;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPBinding;
import java.util.ArrayList;

/**
 * Author: JAXWS Development Team
 */
public class DispatchContactInfoList implements ContactInfoList {

    public ContactInfoListIterator iterator() {
        ArrayList<Object> arrayList = new ArrayList<Object>();

        SOAPMessageContext messageContext = new SOAPMessageContext();
        SOAPMessage soapMessage = messageContext.createMessage();
        messageContext.setMessage(soapMessage);
        //todo:currently do not need DispatchContactInfo- remove later if not
        //needed
        arrayList.add(new DispatchContactInfo(null,
            new MessageDispatcherHelper(),
            new SOAPXMLEncoder(),
            new SOAPXMLDecoder(),SOAPBinding.SOAP11HTTP_BINDING));
        arrayList.add(new DispatchContactInfo(null,
            new MessageDispatcherHelper(),
            new SOAP12XMLEncoder(),
            new com.sun.xml.ws.encoding.soap.client.SOAP12XMLDecoder(), SOAPBinding.SOAP12HTTP_BINDING));
        /*arrayList.add(new DispatchContactInfo(null,
                new MessageDispatcherHelper(new DispatchEncoderDecoderUtil()),
                new SOAPFastEncoder(new DispatchEncoderDecoderUtil()),
                new SOAPFastDecoder(new DispatchEncoderDecoderUtil())));
          */
        return new ContactInfoListIteratorBase(arrayList);
    }
}
