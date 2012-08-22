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

package com.sun.xml.ws.util.xml;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.sax.SAXResult;

/**
 * A JAXP {@link javax.xml.transform.Result} implementation that produces
 * a result on the specified {@link javax.xml.stream.XMLStreamWriter} or
 * {@link javax.xml.stream.XMLEventWriter}.
 *
 * <p>
 * Please note that you may need to call flush() on the underlying
 * XMLStreamWriter or XMLEventWriter after the transform is complete.
 * <p>
 *
 * The fact that JAXBResult derives from SAXResult is an implementation
 * detail. Thus in general applications are strongly discouraged from
 * accessing methods defined on SAXResult.
 *
 * <p>
 * In particular it shall never attempt to call the following methods:
 *
 * <ul>
 *    <li>setHandler</li>
 *    <li>setLexicalHandler</li>
 *    <li>setSystemId</li>
 * </ul>
 *
 * <p>
 * Example:
 *
 * <pre>
    // create a DOMSource
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(...);
    Source domSource = new DOMSource(doc);

    // create a StAXResult
    XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
    Result staxResult = new StAXResult(writer);

    // run the transform
    TransformerFactory.newInstance().newTransformer().transform(domSource, staxResult);
 * </pre>
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
public class StAXResult extends SAXResult {

    /**
     * Create a new {@link javax.xml.transform.Result} that produces
     * a result on the specified {@link javax.xml.stream.XMLStreamWriter}
     *
     * @param writer the XMLStreamWriter
     * @throws IllegalArgumentException iff the writer is null
     */
    public StAXResult(XMLStreamWriter writer) {
        if( writer == null ) {
            throw new IllegalArgumentException();
        }

        super.setHandler(new ContentHandlerToXMLStreamWriter( writer ));
    }
}
