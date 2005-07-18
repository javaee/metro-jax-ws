/*
 * $Id: WSDLUtil.java,v 1.2 2005-07-18 18:14:24 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.sun.tools.ws.wsdl.document.Definitions;
import com.sun.tools.ws.wsdl.document.Import;
import com.sun.tools.ws.wsdl.document.WSDLDocument;
import com.sun.tools.ws.wsdl.document.schema.Schema;
import com.sun.tools.ws.wsdl.document.schema.SchemaConstants;
import com.sun.tools.ws.wsdl.document.schema.SchemaElement;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.ParserContext;
import com.sun.tools.ws.wsdl.framework.WriterContext;
import com.sun.tools.ws.util.xml.XmlUtil;

/**
 * WSDL Utilities.
 *
 * @author WS Development Team
 */
public class WSDLUtil {
    public WSDLUtil() {
    }

    /**
     * Collect all relative imports from a web service's main wsdl document.
     *
     *@param wsdlRelativeImports outupt param in which wsdl relative imports
     * will be added
     *
     *@param schemaRelativeImports outupt param in which schema relative
     * imports will be added
     */
    public void getRelativeImports(
        URL wsdlURL,
        Collection wsdlRelativeImports,
        Collection schemaRelativeImports)
        throws IOException {

        // Parse the wsdl document to find all import statements
        InputStream wsdlInputStream =
            new BufferedInputStream(wsdlURL.openStream());
        InputSource wsdlDocumentSource = new InputSource(wsdlInputStream);
        WSDLParserOverride wsdlParser = new WSDLParserOverride();
        // We only want to grab the import statements in the initial
        // wsdl document. No need to fully resolve them.
        wsdlParser.setFollowImports(false);
        WSDLDocument wsdlDoc = wsdlParser.parse(wsdlDocumentSource);

        for (Iterator iter = wsdlDoc.getDefinitions().imports();
            iter.hasNext();
            ) {
            Import next = (Import) iter.next();
            String location = next.getLocation();
            // If it's a relative import
            if ((location.indexOf(":") == -1)) {
                wsdlRelativeImports.add(next);
            }
        }

        Collection schemaImports = wsdlParser.getSchemaImports();
        for (Iterator iter = schemaImports.iterator(); iter.hasNext();) {
            Import next = (Import) iter.next();
            String location = next.getLocation();
            // If it's a relative import
            if ((location.indexOf(":") == -1)) {
                schemaRelativeImports.add(next);
            }
        }

        wsdlInputStream.close();

        return;
    }

    /**
     * Subclass of WSDLParser that skips processing of imports.  Only
     * needed temporarily until jaxrpc code uses value of setFollowImports()
     */
    private static class WSDLParserOverride extends WSDLParser {

        private SchemaExtensionHandlerOverride schemaHandler;

        public WSDLParserOverride() {
            super();
            schemaHandler = new SchemaExtensionHandlerOverride();
            // Override the schema handler
            register(schemaHandler);
        }

        public Collection getSchemaImports() {
            return schemaHandler.getImports();
        }

        protected Definitions parseDefinitions(
            ParserContext context,
            InputSource source,
            String expectedTargetNamespaceURI) {
            Definitions definitions =
                parseDefinitionsNoImport(
                    context,
                    source,
                    expectedTargetNamespaceURI);
            return definitions;
        }
    }

    private static class SchemaExtensionHandlerOverride
        extends ExtensionHandler {

        private SchemaParserOverride parser;

        public SchemaExtensionHandlerOverride() {
            parser = new SchemaParserOverride();
        }

        public Collection getImports() {
            return parser.getImports();
        }

        public String getNamespaceURI() {
            return Constants.NS_XSD;
        }

        public boolean doHandleExtension(
            ParserContext context,
            Extensible parent,
            Element e) {
            if (XmlUtil.matchesTagNS(e, SchemaConstants.QNAME_SCHEMA)) {
                parent.addExtension(parser.parseSchema(context, e, null));
                return true;
            } else {
                return false;
            }
        }

        public void doHandleExtension(
            WriterContext context,
            Extension extension)
            throws IOException {
            throw new IllegalArgumentException("unsupported operation");
        }
    }

    private static class SchemaParserOverride extends SchemaParser {
        private Collection imports = new HashSet();

        public Collection getImports() {
            return imports;
        }

        protected void processImports(
            ParserContext context,
            InputSource src,
            Schema schema) {
            for (Iterator iter = schema.getContent().children();
                iter.hasNext();
                ) {
                SchemaElement child = (SchemaElement) iter.next();
                if (child.getQName().equals(SchemaConstants.QNAME_IMPORT)) {
                    String location =
                        child.getValueOfAttributeOrNull(
                            Constants.ATTR_SCHEMA_LOCATION);
                    String namespace =
                        child.getValueOfAttributeOrNull(
                            Constants.ATTR_NAMESPACE);
                    if ((location != null) && (namespace != null)) {
                        Import schemaImport = new Import();
                        schemaImport.setLocation(location);
                        schemaImport.setNamespace(namespace);
                        imports.add(schemaImport);
                    }
                }
            }
        }
    }
}
