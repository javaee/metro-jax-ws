/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.tools.ws.resources.WsdlMessages;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBindingsConstants;
import org.xml.sax.*;
import org.xml.sax.helpers.LocatorImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Checks the jaxb:version attribute on a XML Schema document.
 *
 * jaxws:version is optional, if absent its value is assumed to be "2.0" and if present its value must be
 * "2.0" or more.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 *     Vivek Pandey
 */
public class VersionChecker extends XMLFilterImpl {

    /**
     * We store the value of the version attribute in this variable
     * when we hit the root element.
     */
    private String version = null ;

    /** Will be set to true once we hit the root element. */
    private boolean seenRoot = false;

    /** Will be set to true once we hit a binding declaration. */
    private boolean seenBindings = false;

    private Locator locator;

    /**
     * Stores the location of the start tag of the root tag.
     */
    private Locator rootTagStart;

    public VersionChecker( XMLReader parent ) {
        setParent(parent);
    }

    public VersionChecker( ContentHandler handler, ErrorHandler eh, EntityResolver er ) {
        setContentHandler(handler);
        if(eh!=null)    setErrorHandler(eh);
        if(er!=null)    setEntityResolver(er);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {

        super.startElement(namespaceURI, localName, qName, atts);

        if(!seenRoot) {
            // if this is the root element
            seenRoot = true;
            rootTagStart = new LocatorImpl(locator);

            version = atts.getValue(JAXWSBindingsConstants.NS_JAXWS_BINDINGS,"version");
            if( namespaceURI.equals(JAXWSBindingsConstants.NS_JAXWS_BINDINGS) ) {
                String version2 = atts.getValue("","version");
                if( version!=null && version2!=null ) {
                    // we have both @version and @jaxb:version. error.
                    SAXParseException e = new SAXParseException(
                        WsdlMessages.INTERNALIZER_TWO_VERSION_ATTRIBUTES(), locator);
                    getErrorHandler().error(e);
                }
                //According to JAXWS 2.0 spec, if version attribute is missing its assumed to be "2.0"
                if( version==null)
                    version = (version2!=null)?version2:"2.0";
            }

        }

        if( JAXWSBindingsConstants.NS_JAXWS_BINDINGS.equals(namespaceURI)){
            seenBindings = true;
            if(version == null)
                version = "2.0";            
        }

    }

    public void endDocument() throws SAXException {
        super.endDocument();

        if( seenBindings && version==null ) {
            // if we see a binding declaration but not version attribute
            SAXParseException e = new SAXParseException(WsdlMessages.INTERNALIZER_VERSION_NOT_PRESENT(), rootTagStart);
            getErrorHandler().error(e);
        }

        // if present, the value must be >= 2.0
        if( version!=null && !VERSIONS.contains(version) ) {
            SAXParseException e = new SAXParseException(WsdlMessages.INTERNALIZER_INCORRECT_VERSION(), rootTagStart);
            getErrorHandler().error(e);
        }
    }

    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    private static final Set<String> VERSIONS = new HashSet<String>(Arrays.asList("2.0","2.1"));

}

