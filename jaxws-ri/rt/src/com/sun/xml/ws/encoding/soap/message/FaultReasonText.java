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
package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.Locale;

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
