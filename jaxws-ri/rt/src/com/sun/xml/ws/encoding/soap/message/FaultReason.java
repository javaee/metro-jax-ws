package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;

/**
 * $Id: FaultReason.java,v 1.2 2005-07-26 23:43:44 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * SOAP 1.2 Fault Reason
 *  <soapenv:Reason>
 *      <soapenv:Text xml:lang="en">...</soapenv:Text>
 *  </soapenv:Reason>
 *
 * @author Vivek Pandey
 */
public class FaultReason {
    private List<FaultReasonText> texts;

    public FaultReason(FaultReasonText... texts) {
        assert(texts == null);
        this.texts = Arrays.asList(texts);
    }

    public FaultReason(List<FaultReasonText> textList) {
        texts = textList;
    }

    public List<FaultReasonText> getFaultReasonTexts(){
        return texts;
    }

    void write(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
            SOAP12Constants.QNAME_FAULT_REASON.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);
        for(FaultReasonText text:texts){
            text.write(writer);
        }
        writer.writeEndElement();
    }
}
