/*
 * $Id: SOAP12NotUnderstoodHeaderBlock.java,v 1.1 2005-11-02 21:23:12 bbissett Exp $
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
package com.sun.xml.ws.encoding.soap.internal;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;

/**
 * SOAP 1.2 version of NotUnderstoodHeaderBlock.
 *
 * @author WS Development Team
 */
public class SOAP12NotUnderstoodHeaderBlock extends HeaderBlock {
    
    private QName nuHeader;
    
    // super(null) is a hack in this case
    public SOAP12NotUnderstoodHeaderBlock(QName header) {
        super(null);
        nuHeader = header;
    }
    
    public QName getName() {
        return new QName(SOAP12NamespaceConstants.ENVELOPE,
            SOAP12NamespaceConstants.TAG_NOT_UNDERSTOOD);
    }
    
    public void write(XMLStreamWriter writer) {
        try {
            String prefix = "t"; // should not have been used before <header>
            writer.writeStartElement(
                SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAP12NamespaceConstants.TAG_NOT_UNDERSTOOD,
                SOAP12NamespaceConstants.ENVELOPE);
            writer.writeAttribute(
                SOAP12NamespaceConstants.ATTR_NOT_UNDERSTOOD_QNAME,
                prefix + ":" + nuHeader.getLocalPart());
            writer.writeNamespace(prefix, nuHeader.getNamespaceURI());
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

}
