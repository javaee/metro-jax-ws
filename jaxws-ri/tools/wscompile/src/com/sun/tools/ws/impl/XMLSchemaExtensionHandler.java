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
package com.sun.tools.ws.impl;

import org.jvnet.wom.api.parser.XMLSchemaParser;
import org.jvnet.wom.api.WSDLExtension;
import org.jvnet.wom.Schema;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.XMLFilterImpl;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.TypeAndAnnotation;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Vivek Pandey
 */
public class XMLSchemaExtensionHandler implements XMLSchemaParser {

    private XMLSchema schema = new XMLSchema();

    private final ContentHandler delegatingHandler = new XMLSchemaCH();

    /**
     * Obtained from SchemaCompiler,latter on during parsing
     */
    private ContentHandler schemaHandler;

    private final SchemaCompiler schemaCompiler;

    public XMLSchemaExtensionHandler(SchemaCompiler schemaCompiler) {
        this.schemaCompiler = schemaCompiler;
    }

    public Collection<WSDLExtension> getExtensions() {
        return Collections.<WSDLExtension>singleton(schema);
    }

    public Collection<WSDLExtension> parseAttribute(Attributes atts) {
        return null;
    }

    public ContentHandler getContentHandlerFor(String nsUri, String localName) {
        return ("http://www.w3.org/2001/XMLSchema".equals(nsUri) &&
        "schema".equals(localName))?delegatingHandler:null;
    }

    private S2JJAXBModel model;

    public Schema<Mapping, TypeAndAnnotation> getSchema() {
        return xmlschema;
    }

    public void freeze() {
        model = schemaCompiler.bind();
        xmlschema = new XMLSchema();
    }

    private class XMLSchemaCH extends XMLFilterImpl {
        private int counter=0;

        /** This method must be called first by WOM parser otherwise there is no way to get ContentHandler from
         *  SchemaCompiler
         */
        @Override
        public void setDocumentLocator(Locator locator) {
            schemaHandler = schemaCompiler.getParserHandler(locator.getSystemId()+"#types?schema"+counter++);
            schemaHandler.setDocumentLocator(locator);
            super.setContentHandler(schemaHandler);
        }
    }

    private Schema<Mapping, TypeAndAnnotation> xmlschema;

    private class XMLSchema implements Schema<Mapping, TypeAndAnnotation>{

        public QName getName() {
            return new QName("http://www.w3.org/2001/XMLSchema", "schema");
        }

        public Mapping resolveElement(QName elementName) {
            return model.get(elementName);
        }

        public TypeAndAnnotation resolveType(QName typeName) {
            return model.getJavaType(typeName);
        }
    }


}

