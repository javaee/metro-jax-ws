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

import com.sun.istack.SAXParseException2;
import com.sun.tools.ws.resources.WsdlMessages;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * XMLFilter that finds references to other schema files from
 * SAX events.
 *
 * This implementation is a base implementation for typical case
 * where we just need to look for a particular attribute which
 * contains an URL to another schema file.
 *
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 *  Vivek Pandey
 */
public abstract class AbstractReferenceFinderImpl extends XMLFilterImpl {
    protected final DOMForest parent;

    protected AbstractReferenceFinderImpl( DOMForest _parent ) {
        this.parent = _parent;
    }

    /**
     * IF the given element contains a reference to an external resource,
     * return its URL.
     *
     * @param nsURI
     *      Namespace URI of the current element
     * @param localName
     *      Local name of the current element
     * @return
     *      It's OK to return a relative URL.
     */
    protected abstract String findExternalResource( String nsURI, String localName, Attributes atts);

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
        super.startElement(namespaceURI, localName, qName, atts);

        String relativeRef = findExternalResource(namespaceURI,localName,atts);
        if(relativeRef==null)   return; // non found

        try {
            // absolutize URL.
            assert locator != null;
            String lsi = locator.getSystemId();
            String ref;
            if (lsi.startsWith("jar:")) {
                    int bangIdx = lsi.indexOf('!');
                    if (bangIdx > 0) {
                            ref = new URL(new URL(lsi), relativeRef).toString();
                    } else
                            ref = relativeRef;
            } else
                    ref = new URI(lsi).resolve(new URI(relativeRef)).toString();

            // then parse this schema as well,
            // but don't mark this document as a root.
            parent.parse(ref,false);
        } catch( URISyntaxException e ) {
            SAXParseException spe = new SAXParseException2(
                    WsdlMessages.ABSTRACT_REFERENCE_FINDER_IMPL_UNABLE_TO_PARSE(relativeRef,e.getMessage()),
                locator, e );

            fatalError(spe);
            throw spe;
        } catch( IOException e ) {
            SAXParseException spe = new SAXParseException2(
                WsdlMessages.ABSTRACT_REFERENCE_FINDER_IMPL_UNABLE_TO_PARSE(relativeRef,e.getMessage()),
                locator, e );

            fatalError(spe);
            throw spe;
        }
    }

    private Locator locator;

    @Override
    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }
}
