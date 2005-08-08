/*
 * $Id: DispatchContactInfoList.java,v 1.7 2005-08-08 19:13:02 arungupta Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl;

import java.util.ArrayList;

import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.xml.ws.client.ContactInfoListIteratorBase;
import com.sun.xml.ws.client.dispatch.impl.protocol.MessageDispatcherHelper;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLEncoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLEncoder;
import com.sun.xml.ws.encoding.xml.XMLDecoder;
import com.sun.xml.ws.encoding.xml.XMLEncoder;
import com.sun.xml.ws.protocol.xml.client.XMLMessageDispatcher;

/**
 * @author: WS Development Team
 */
public class DispatchContactInfoList implements ContactInfoList {

    public ContactInfoListIterator iterator() {
        ArrayList<Object> arrayList = new ArrayList<Object>();

        //todo:currently do not need DispatchContactInfo- remove later if not
        //needed
        arrayList.add(new DispatchContactInfo(null,
            new MessageDispatcherHelper(),
            new SOAPXMLEncoder(),
            new SOAPXMLDecoder(),SOAPBinding.SOAP11HTTP_BINDING));
        arrayList.add(new DispatchContactInfo(null,
            new MessageDispatcherHelper(),
            new SOAP12XMLEncoder(),
            new SOAP12XMLDecoder(), SOAPBinding.SOAP12HTTP_BINDING));
        arrayList.add(new DispatchContactInfo(null,
                new XMLMessageDispatcher(),
                new XMLEncoder(),
                new XMLDecoder(), HTTPBinding.HTTP_BINDING));
        /*arrayList.add(new DispatchContactInfo(null,
                new MessageDispatcherHelper(new DispatchEncoderDecoderUtil()),
                new SOAPFastEncoder(new DispatchEncoderDecoderUtil()),
                new SOAPFastDecoder(new DispatchEncoderDecoderUtil())));
          */
        return new ContactInfoListIteratorBase(arrayList);
    }
}
