package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.Locale;

/**
 * $Id: FaultReasonText.java,v 1.2 2005-07-26 23:43:44 vivekp Exp $
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
    private Locale language = Locale.getDefault();
    private String value;

    public FaultReasonText(String value, Locale lang) {
        if(language != null && !language.equals(""))
            this.language = lang;
        this.value = value;
    }

    public Locale getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }

    void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAP12Constants.QNAME_FAULT_REASON_TEXT.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);

        //for now we only generate "en" language
        writer.writeAttribute("xml", SOAP12NamespaceConstants.XML_NS, "lang", language.getLanguage());
        writer.writeCharacters(value);
        writer.writeEndElement();
    }
}
