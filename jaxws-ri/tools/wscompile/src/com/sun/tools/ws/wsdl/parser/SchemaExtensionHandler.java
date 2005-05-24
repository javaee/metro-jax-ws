/*
 * $Id: SchemaExtensionHandler.java,v 1.1 2005-05-24 14:07:30 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.parser;

import java.io.IOException;

import org.w3c.dom.Element;

import com.sun.tools.ws.wsdl.document.schema.Schema;
import com.sun.tools.ws.wsdl.document.schema.SchemaConstants;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.ParserContext;
import com.sun.tools.ws.wsdl.framework.WriterContext;
import com.sun.tools.ws.util.xml.XmlUtil;

/**
 * The XML Schema extension handler for WSDL.
 *
 * @author JAX-RPC Development Team
 */
public class SchemaExtensionHandler extends ExtensionHandler {

    public SchemaExtensionHandler() {
    }

    public String getNamespaceURI() {
        return Constants.NS_XSD;
    }

    public boolean doHandleExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (XmlUtil.matchesTagNS(e, SchemaConstants.QNAME_SCHEMA)) {
            SchemaParser parser = new SchemaParser();
            parent.addExtension(parser.parseSchema(context, e, null));
            return true;
        } else {
            return false;
        }
    }

    public void doHandleExtension(WriterContext context, Extension extension)
        throws IOException {
        if (extension instanceof Schema) {
            SchemaWriter writer = new SchemaWriter();
            writer.writeSchema(context, (Schema) extension);
        } else {
            // unknown extension
            throw new IllegalArgumentException();
        }
    }
}
