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

package com.sun.tools.ws.wsdl.parser;

import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

import com.sun.xml.xsom.parser.XMLParser;

/**
 * {@link XMLParser} implementation that
 * parses XML from a DOM forest instead of parsing it from
 * its original location.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @author Vivek Pandey
 */
public class DOMForestParser implements XMLParser {

    /**
     * DOM forest to be "parsed".
     */
    private final DOMForest forest;

    /**
     * Scanner object will do the actual SAX events generation.
     */
    private final DOMForestScanner scanner;

    private final XMLParser fallbackParser;

    /**
     * @param fallbackParser This parser will be used when DOMForestParser needs to parse
     *                       documents that are not in the forest.
     */
    public DOMForestParser(DOMForest forest, XMLParser fallbackParser) {
        this.forest = forest;
        this.scanner = new DOMForestScanner(forest);
        this.fallbackParser = fallbackParser;
    }


    public void parse(InputSource source, ContentHandler handler,  EntityResolver entityResolver, ErrorHandler errHandler) throws SAXException, IOException {

    }

    public void parse(InputSource source, ContentHandler handler, ErrorHandler errorHandler, EntityResolver entityResolver)

            throws SAXException, IOException {
        String systemId = source.getSystemId();
        Document dom = forest.get(systemId);

        if (dom == null) {
            // if no DOM tree is built for it,
            // let the fall back parser parse the original document.
            //
            // for example, XSOM parses datatypes.xsd (XML Schema part 2)
            // but this will never be built into the forest.
            fallbackParser.parse(source, handler, errorHandler, entityResolver);
            return;
        }

        scanner.scan(dom, handler);

    }
}
