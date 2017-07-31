/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.message;

import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;

/**
 * Parses the SOAP message in order to get {@link QName} of a payload element.
 * It parses message until it
 *
 * @author Miroslav Kos (miroslav.kos at oracle.com)
 */
public class PayloadElementSniffer extends DefaultHandler {

    // flag if the last element was SOAP body
    private boolean bodyStarted;

    // payloadQName used as a return value
    private QName payloadQName;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (bodyStarted) {
            payloadQName = new QName(uri, localName);
            // we have what we wanted - let's skip rest of parsing ...
            throw new SAXException("Payload element found, interrupting the parsing process.");
        }

        // check for both SOAP 1.1/1.2
        if (equalsQName(uri, localName, SOAPConstants.QNAME_SOAP_BODY) ||
                equalsQName(uri, localName, SOAP12Constants.QNAME_SOAP_BODY)) {
            bodyStarted = true;
        }

    }

    private boolean equalsQName(String uri, String localName, QName qname) {
        return qname.getLocalPart().equals(localName) &&
                qname.getNamespaceURI().equals(uri);
    }

    public QName getPayloadQName() {
        return payloadQName;
    }
}
