package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

/**
 * $Id: FaultReasonText.java,v 1.1 2005-06-04 01:48:13 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * <soapenv:Text xmlns:lang="en">
 * ...
 * </soapenv:Text>
 *
 * @author Vivek Pandey
 */
public class FaultReasonText {
    private String language = "en";
    private String value;

    public FaultReasonText(String language, String value) {
        if(language != null && !language.equals(""))
            this.language = language;
        this.value = value;
    }

    public String getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }

    void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAP12Constants.QNAME_FAULT_REASON_TEXT.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);

        //for now we only generate "en" language
        writer.writeAttribute("xml", SOAP12NamespaceConstants.XML_NS, "lang", language);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }
}
