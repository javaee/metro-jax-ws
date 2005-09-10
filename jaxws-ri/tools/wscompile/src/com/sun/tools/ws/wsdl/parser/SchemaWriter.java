/*
 * $Id: SchemaWriter.java,v 1.3 2005-09-10 19:50:12 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.ws.wsdl.parser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.document.schema.Schema;
import com.sun.tools.ws.wsdl.document.schema.SchemaAttribute;
import com.sun.tools.ws.wsdl.document.schema.SchemaDocument;
import com.sun.tools.ws.wsdl.document.schema.SchemaElement;
import com.sun.tools.ws.wsdl.framework.WriterContext;

/**
 * A writer for XML Schema fragments within a WSDL document.
 *
 * @author WS Development Team
 */
public class SchemaWriter {

    public SchemaWriter() {
    }

    public void write(SchemaDocument document, OutputStream os)
        throws IOException {
        WriterContext context = new WriterContext(os);
        writeSchema(context, document.getSchema());
        context.flush();
    }

    public void writeSchema(WriterContext context, Schema schema)
        throws IOException {
        context.push();
        try {
            writeTopSchemaElement(context, schema);
        } catch (Exception e) {
        } finally {
            context.pop();
        }
    }

    protected void writeTopSchemaElement(WriterContext context, Schema schema)
        throws IOException {
        SchemaElement schemaElement = schema.getContent();
        QName name = schemaElement.getQName();

        // make sure that all namespaces we expect are actually declared
        for (Iterator iter = schema.prefixes(); iter.hasNext();) {
            String prefix = (String) iter.next();
            String expectedURI = schema.getURIForPrefix(prefix);
            if (!expectedURI.equals(context.getNamespaceURI(prefix))) {
                context.declarePrefix(prefix, expectedURI);
            }
        }

        for (Iterator iter = schemaElement.prefixes(); iter.hasNext();) {
            String prefix = (String) iter.next();
            String uri = schemaElement.getURIForPrefix(prefix);
            context.declarePrefix(prefix, uri);
        }

        context.writeStartTag(name);

        for (Iterator iter = schemaElement.attributes(); iter.hasNext();) {
            SchemaAttribute attribute = (SchemaAttribute) iter.next();
            if (attribute.getNamespaceURI() == null) {
                context.writeAttribute(
                    attribute.getLocalName(),
                    attribute.getValue(context));
            } else {
                context.writeAttribute(
                    context.getQNameString(attribute.getQName()),
                    attribute.getValue(context));
            }
        }

        context.writeAllPendingNamespaceDeclarations();

        for (Iterator iter = schemaElement.children(); iter.hasNext();) {
            SchemaElement child = (SchemaElement) iter.next();
            writeSchemaElement(context, child);
        }

        context.writeEndTag(name);
    }

    protected void writeSchemaElement(
        WriterContext context,
        SchemaElement schemaElement)
        throws IOException {
        QName name = schemaElement.getQName();

        if (schemaElement.declaresPrefixes()) {
            context.push();
        }

        context.writeStartTag(name);

        if (schemaElement.declaresPrefixes()) {
            for (Iterator iter = schemaElement.prefixes(); iter.hasNext();) {
                String prefix = (String) iter.next();
                String uri = schemaElement.getURIForPrefix(prefix);
                context.writeNamespaceDeclaration(prefix, uri);
                context.declarePrefix(prefix, uri);
            }
        }

        for (Iterator iter = schemaElement.attributes(); iter.hasNext();) {
            SchemaAttribute attribute = (SchemaAttribute) iter.next();
            if (attribute.getNamespaceURI() == null) {
                context.writeAttribute(
                    attribute.getLocalName(),
                    attribute.getValue(context));
            } else {
                context.writeAttribute(
                    context.getQNameString(attribute.getQName()),
                    attribute.getValue(context));
            }
        }

        for (Iterator iter = schemaElement.children(); iter.hasNext();) {
            SchemaElement child = (SchemaElement) iter.next();
            writeSchemaElement(context, child);
        }

        context.writeEndTag(name);

        if (schemaElement.declaresPrefixes()) {
            context.pop();
        }
    }
}
