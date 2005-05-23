/*
 * $Id: ContactInfoListImpl.java,v 1.1 2005-05-23 22:26:35 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.xml.ws.encoding.EncoderDecoderBase;
import com.sun.xml.ws.encoding.soap.ClientEncoderDecoder;

import java.util.ArrayList;

public class ContactInfoListImpl implements ContactInfoList {

    /*
     *  ContactInfoListBase#getClientEncoderDecoderUtil
     */
    public EncoderDecoderBase getClientEncoderDecoderUtil() {
        EncoderDecoderBase ced = new ClientEncoderDecoder();
        return ced;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.ept.ContactInfoList#iterator()
     */
    public ContactInfoListIterator iterator() {
        ArrayList arrayList = new ArrayList();
        EncoderDecoderBase encoderDecoderUtil = getClientEncoderDecoderUtil();
        arrayList.add(new ContactInfoBase(null,
            new SOAPMessageDispatcher(),
            new SOAPXMLEncoder(),
            new SOAPXMLDecoder()));
        return new ContactInfoListIteratorBase(arrayList);
    }

}
