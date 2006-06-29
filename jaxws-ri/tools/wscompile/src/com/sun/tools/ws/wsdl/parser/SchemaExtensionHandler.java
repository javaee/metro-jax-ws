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
 * @author WS Development Team
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
