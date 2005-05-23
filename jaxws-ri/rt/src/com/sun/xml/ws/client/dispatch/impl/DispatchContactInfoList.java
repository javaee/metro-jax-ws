/*
 * $Id: DispatchContactInfoList.java,v 1.1 2005-05-23 22:13:45 bbissett Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl;


import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.xml.ws.client.ContactInfoListIteratorBase;
import com.sun.xml.ws.client.dispatch.impl.encoding.DispatchXMLDecoder;
import com.sun.xml.ws.client.dispatch.impl.encoding.DispatchXMLEncoder;
import com.sun.xml.ws.client.dispatch.impl.protocol.MessageDispatcherHelper;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;

import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;

/**
 * Author: JAXRPC Development Team
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
            new DispatchXMLEncoder(),
            new DispatchXMLDecoder()));
        /*arrayList.add(new DispatchContactInfo(null,
                new MessageDispatcherHelper(new DispatchEncoderDecoderUtil()),
                new SOAPFastEncoder(new DispatchEncoderDecoderUtil()),
                new SOAPFastDecoder(new DispatchEncoderDecoderUtil())));
          */
        return new ContactInfoListIteratorBase(arrayList);
    }
}
