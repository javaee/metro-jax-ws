/*
 * $Id: ContactInfoListImpl.java,v 1.6 2005-08-04 02:32:20 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLEncoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLEncoder;
import com.sun.xml.ws.protocol.soap.client.SOAPMessageDispatcher;

import javax.xml.ws.soap.SOAPBinding;
import java.util.ArrayList;

public class ContactInfoListImpl implements ContactInfoList {

    /* (non-Javadoc)
     * @see com.sun.pept.ept.ContactInfoList#iterator()
     */
    public ContactInfoListIterator iterator() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new ContactInfoBase(null,
            new SOAPMessageDispatcher(),
            new SOAPXMLEncoder(),
            new SOAPXMLDecoder(), SOAPBinding.SOAP11HTTP_BINDING));
        arrayList.add(new ContactInfoBase(null,
            new SOAPMessageDispatcher(),
            new SOAP12XMLEncoder(),
            new SOAP12XMLDecoder(), SOAPBinding.SOAP12HTTP_BINDING));
        return new ContactInfoListIteratorBase(arrayList);
    }

}
