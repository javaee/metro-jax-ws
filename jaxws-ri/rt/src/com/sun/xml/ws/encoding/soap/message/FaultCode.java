/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
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
public class FaultCode {
    private FaultCodeEnum value;
    private FaultSubcode subcode;

    public FaultCode(FaultCodeEnum value, FaultSubcode subcode) {
        this.value = value;
        this.subcode = subcode;
    }

    public FaultCode(FaultCodeEnum value, Iterator<QName> subcodes) {
        this.value = value;
        if(subcodes.hasNext()){
            subcode = new FaultSubcode(subcodes.next(), subcodes);
        }
    }

    public FaultCodeEnum getValue() {
        return value;
    }

    public FaultSubcode getSubcode() {
        return subcode;
    }

    void write(XMLStreamWriter writer) throws XMLStreamException {
        // <soapenv:Code>
        writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
            SOAP12Constants.QNAME_FAULT_CODE.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);

        // <soapenv:Value>
        writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
            SOAP12Constants.QNAME_FAULT_VALUE.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);

        writer.writeCharacters(value.getPrefix()+":"+value.getLocalPart());
        writer.writeEndElement(); // </soapenv:Value>

        // <soapenv:Subcode>...</soapenv:Subcode>
        if(subcode != null)
            subcode.write(writer);
        writer.writeEndElement(); // </soapenv:Code>
    }
}
