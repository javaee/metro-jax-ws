/*
 * $Id: ContactInfoListImpl.java,v 1.4 2005-07-15 19:13:28 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.xml.ws.encoding.EncoderDecoderBase;
import com.sun.xml.ws.encoding.soap.ClientEncoderDecoder;

import javax.xml.ws.soap.SOAPBinding;
import java.util.ArrayList;
import com.sun.xml.ws.protocol.soap.client.SOAPMessageDispatcher;

public class ContactInfoListImpl implements ContactInfoList {

    /* (non-Javadoc)
     * @see com.sun.pept.ept.ContactInfoList#iterator()
     */
    public ContactInfoListIterator iterator() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new ContactInfoBase(null,
            new SOAPMessageDispatcher(),
            new com.sun.xml.ws.encoding.soap.SOAPXMLEncoder(),
            new com.sun.xml.ws.encoding.soap.SOAPXMLDecoder(), SOAPBinding.SOAP11HTTP_BINDING));
        arrayList.add(new ContactInfoBase(null,
            new SOAPMessageDispatcher(),
            new com.sun.xml.ws.encoding.soap.SOAP12XMLEncoder(),
            new com.sun.xml.ws.encoding.soap.SOAP12XMLDecoder(), SOAPBinding.SOAP12HTTP_BINDING));
        return new ContactInfoListIteratorBase(arrayList);
    }

}
