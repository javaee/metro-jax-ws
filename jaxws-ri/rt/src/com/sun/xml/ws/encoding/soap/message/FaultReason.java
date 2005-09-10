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
 * $Id: FaultReason.java,v 1.3 2005-09-10 19:47:44 kohsuke Exp $
 */

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
