/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.message.stream;

import com.sun.istack.FinalArrayList;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.message.Util;

import javax.xml.soap.SOAPConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * {@link StreamHeader} for SOAP 1.1.
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings({"StringEquality"})
public class StreamHeader11 extends StreamHeader {
    protected static final String SOAP_1_1_MUST_UNDERSTAND = "mustUnderstand";

    protected static final String SOAP_1_1_ROLE = "actor";
    
    public StreamHeader11(XMLStreamReader reader, XMLStreamBuffer mark) {
        super(reader, mark);
    }

    public StreamHeader11(XMLStreamReader reader) throws XMLStreamException {
        super(reader);
    }

    protected final FinalArrayList<Attribute> processHeaderAttributes(XMLStreamReader reader) {
        FinalArrayList<Attribute> atts = null;

        _role = SOAPConstants.URI_SOAP_ACTOR_NEXT;

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            final String localName = reader.getAttributeLocalName(i);
            final String namespaceURI = reader.getAttributeNamespace(i);
            final String value = reader.getAttributeValue(i);

            if (SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(namespaceURI)) {
                if (SOAP_1_1_MUST_UNDERSTAND.equals(localName)) {
                    _isMustUnderstand = Util.parseBool(value);
                } else if (SOAP_1_1_ROLE.equals(localName)) {
                    if (value != null && value.length() > 0) {
                        _role = value;
                    }
                }
            }

            if(atts==null) {
                atts = new FinalArrayList<Attribute>();
            }
            atts.add(new Attribute(namespaceURI,localName,value));
        }

        return atts;
    }
}
