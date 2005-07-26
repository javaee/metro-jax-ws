package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.Iterator;

/**
 * $Id: FaultSubcode.java,v 1.2 2005-07-26 23:43:44 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

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
