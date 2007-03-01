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
package com.sun.xml.ws.message;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Sniffs the root element name and its attributes from SAX events.
 *
 * @author Kohsuke Kawaguchi
 */
public final class RootElementSniffer extends DefaultHandler {
    private String nsUri = "##error";
    private String localName = "##error";
    private Attributes atts;

    private final boolean parseAttributes;

    public RootElementSniffer(boolean parseAttributes) {
        this.parseAttributes = parseAttributes;
    }

    public RootElementSniffer() {
        this(true);
    }

    public void startElement(String uri, String localName, String qName, Attributes a) throws SAXException {
        this.nsUri = uri;
        this.localName = localName;
        
        if(parseAttributes) {
            if(a.getLength()==0)    // often there's no attribute
                this.atts = EMPTY_ATTRIBUTES;
            else
                this.atts = new AttributesImpl(a);
        }

        // no need to parse any further.
        throw aSAXException;
    }

    public String getNsUri() {
        return nsUri;
    }

    public String getLocalName() {
        return localName;
    }

    public Attributes getAttributes() {
        return atts;
    }

    private static final SAXException aSAXException = new SAXException();
    private static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
}
