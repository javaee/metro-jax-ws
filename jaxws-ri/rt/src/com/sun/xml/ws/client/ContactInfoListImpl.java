/*
 * $Id: ContactInfoListImpl.java,v 1.2 2005-06-02 17:53:09 vivekp Exp $
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
