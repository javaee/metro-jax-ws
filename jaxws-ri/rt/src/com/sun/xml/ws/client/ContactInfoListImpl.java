/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.pept.ept.ContactInfoList;
import com.sun.xml.ws.pept.ept.ContactInfoListIterator;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAP12XMLEncoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLDecoder;
import com.sun.xml.ws.encoding.soap.client.SOAPXMLEncoder;
import com.sun.xml.ws.protocol.soap.client.SOAPMessageDispatcher;
import com.sun.xml.ws.protocol.xml.client.XMLMessageDispatcher;

import javax.xml.ws.soap.SOAPBinding;
import java.util.ArrayList;

/**
 * @author WS Development Team
 * List of {@link com.sun.pept.ept.ContactInfo}s
 */
public class ContactInfoListImpl implements ContactInfoList {
    private static final ArrayList arrayList = new ArrayList();
    static {
        arrayList.add(new ContactInfoBase(null,
            new SOAPMessageDispatcher(),
            new SOAPXMLEncoder(),
            new SOAPXMLDecoder(), SOAPBinding.SOAP11HTTP_BINDING));
        arrayList.add(new ContactInfoBase(null,
            new SOAPMessageDispatcher(),
            new SOAP12XMLEncoder(),
            new SOAP12XMLDecoder(), SOAPBinding.SOAP12HTTP_BINDING));
    }

    /**
     * Iterator over the list of {@link com.sun.pept.ept.ContactInfo}s
     * @see com.sun.pept.ept.ContactInfoList#iterator()
     */
    public ContactInfoListIterator iterator() {
        return new ContactInfoListIteratorBase(arrayList);
    }

}
