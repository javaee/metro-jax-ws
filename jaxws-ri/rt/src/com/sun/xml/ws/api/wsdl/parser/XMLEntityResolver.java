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

package com.sun.xml.ws.api.wsdl.parser;

import com.sun.xml.ws.api.server.SDDocumentSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.net.URL;

/**
 * Resolves a reference to {@link XMLStreamReader}.
 *
 * This is kinda like {@link EntityResolver} but works
 * at the XML infoset level.
 *
 * @author Kohsuke Kawaguchi
 */
public interface XMLEntityResolver {
    /**
     * See {@link EntityResolver#resolveEntity(String, String)} for the contract.
     */
    Parser resolveEntity(String publicId,String systemId)
        throws SAXException, IOException, XMLStreamException;

    public static final class Parser {
        /**
         * System ID of the document being parsed.
         */
        public final URL systemId;
        /**
         * The parser instance parsing the infoset.
         */
        public final XMLStreamReader parser;

        public Parser(URL systemId, XMLStreamReader parser) {
            assert parser!=null;
            this.systemId = systemId;
            this.parser = parser;
        }

        /**
         * Creates a {@link Parser} that reads from {@link SDDocumentSource}.
         */
        public Parser(SDDocumentSource doc) throws IOException, XMLStreamException {
            this.systemId = doc.getSystemId();
            this.parser = doc.read();
        }

    }
}
