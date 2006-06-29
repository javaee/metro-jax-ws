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

import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;

/**
 * SOAP 1.2 soapenv:Code
 *
 * <soapenv:Fault>
 *  <soapenv:Code>
 *      <soapenv:Value>soapenv:Sender</soapenv:Value>
 *      <soapenv:Subcode>
 *          <soapenv:Value>ns1:incorectRequest</Value>
 *          <soapenv:Subcode>
 *          ...
 *      </soapenv:Subcode>
 *  </soapenv:Code>
 * </soapenv:Fault>
 *
 * @author Vivek Pandey
 */
public class FaultSubcode {
    private QName value;
    private FaultSubcode subcode;

    public FaultSubcode(QName value, FaultSubcode subcode) {
        this.value = value;
        this.subcode = subcode;
    }

    public FaultSubcode(QName value, Iterator<QName> subcodes) {
        this.value = value;
        if(subcodes.hasNext()){
            subcode = new FaultSubcode(subcodes.next(), subcodes);
        }
    }

    public QName getValue() {
        return value;
    }

    public FaultSubcode getSubcode() {
        return subcode;
    }

    public FaultSubcode setSubCode(FaultSubcode sc){
        this.subcode = sc;
        return subcode;
    }

    void write(XMLStreamWriter writer) throws XMLStreamException {
        // <soapenv:Subcode>
        if(value == null)
            return;
        writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
            SOAP12Constants.QNAME_FAULT_SUBCODE.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);

        // <soapenv:Value>
        writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
            SOAP12Constants.QNAME_FAULT_VALUE.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);

        writer.setPrefix(value.getPrefix(), value.getNamespaceURI());
        if(value.getPrefix().equals(""))
            writer.writeCharacters(value.getLocalPart());
        else
            writer.writeCharacters(value.getPrefix()+":"+value.getLocalPart());
        writer.writeEndElement(); // </soapenv:Value>
        if(subcode != null)
            subcode.write(writer);
        writer.writeEndElement(); // </soapenv:Subcode>
    }
}
