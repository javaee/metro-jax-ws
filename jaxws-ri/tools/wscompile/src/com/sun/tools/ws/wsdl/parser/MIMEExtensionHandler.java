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
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sun.tools.ws.wsdl.document.WSDLConstants;
import com.sun.tools.ws.wsdl.document.mime.MIMEConstants;
import com.sun.tools.ws.wsdl.document.mime.MIMEContent;
import com.sun.tools.ws.wsdl.document.mime.MIMEMultipartRelated;
import com.sun.tools.ws.wsdl.document.mime.MIMEPart;
import com.sun.tools.ws.wsdl.document.mime.MIMEXml;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.ParserContext;
import com.sun.tools.ws.wsdl.framework.WriterContext;
import com.sun.tools.ws.util.xml.XmlUtil;

/**
 * The MIME extension handler for WSDL.
 *
 * @author WS Development Team
 */
public class MIMEExtensionHandler extends ExtensionHandler {

    public MIMEExtensionHandler() {
    }

    public String getNamespaceURI() {
        return Constants.NS_WSDL_MIME;
    }

    public boolean doHandleExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (parent.getElementName().equals(WSDLConstants.QNAME_OUTPUT)) {
            return handleInputOutputExtension(context, parent, e);
        } else if (parent.getElementName().equals(WSDLConstants.QNAME_INPUT)) {
            return handleInputOutputExtension(context, parent, e);
        } else if (parent.getElementName().equals(MIMEConstants.QNAME_PART)) {
            return handleMIMEPartExtension(context, parent, e);
        } else {
            context.fireIgnoringExtension(
                new QName(e.getNamespaceURI(), e.getLocalName()),
                parent.getElementName());
            return false;
        }
    }

    protected boolean handleInputOutputExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (XmlUtil.matchesTagNS(e, MIMEConstants.QNAME_MULTIPART_RELATED)) {
            context.push();
            context.registerNamespaces(e);

            MIMEMultipartRelated mpr = new MIMEMultipartRelated();

            for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if (XmlUtil.matchesTagNS(e2, MIMEConstants.QNAME_PART)) {
                    context.push();
                    context.registerNamespaces(e2);

                    MIMEPart part = new MIMEPart();

                    String name =
                        XmlUtil.getAttributeOrNull(e2, Constants.ATTR_NAME);
                    if (name != null) {
                        part.setName(name);
                    }

                    for (Iterator iter2 = XmlUtil.getAllChildren(e2);
                        iter2.hasNext();
                        ) {
                        Element e3 = Util.nextElement(iter2);
                        if (e3 == null)
                            break;

                        ExtensionHandler h =
                            (ExtensionHandler) _extensionHandlers.get(
                                e3.getNamespaceURI());
                        boolean handled = false;
                        if (h != null) {
                            handled = h.doHandleExtension(context, part, e3);
                        }

                        if (!handled) {
                            String required =
                                XmlUtil.getAttributeNSOrNull(
                                    e3,
                                    Constants.ATTR_REQUIRED,
                                    Constants.NS_WSDL);
                            if (required != null
                                && required.equals(Constants.TRUE)) {
                                Util.fail(
                                    "parsing.requiredExtensibilityElement",
                                    e3.getTagName(),
                                    e3.getNamespaceURI());
                            } else {
                                context.fireIgnoringExtension(
                                    new QName(
                                        e3.getNamespaceURI(),
                                        e3.getLocalName()),
                                    part.getElementName());
                            }
                        }
                    }

                    mpr.add(part);
                    context.pop();
                    context.fireDoneParsingEntity(
                        MIMEConstants.QNAME_PART,
                        part);
                } else {
                    Util.fail(
                        "parsing.invalidElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                }
            }

            parent.addExtension(mpr);
            context.pop();
            context.fireDoneParsingEntity(
                MIMEConstants.QNAME_MULTIPART_RELATED,
                mpr);
            return true;
        } else if (XmlUtil.matchesTagNS(e, MIMEConstants.QNAME_CONTENT)) {
            MIMEContent content = parseMIMEContent(context, e);
            parent.addExtension(content);
            return true;
        } else if (XmlUtil.matchesTagNS(e, MIMEConstants.QNAME_MIME_XML)) {
            MIMEXml mimeXml = parseMIMEXml(context, e);
            parent.addExtension(mimeXml);
            return true;
        } else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false; // keep compiler happy
        }
    }

    protected boolean handleMIMEPartExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (XmlUtil.matchesTagNS(e, MIMEConstants.QNAME_CONTENT)) {
            MIMEContent content = parseMIMEContent(context, e);
            parent.addExtension(content);
            return true;
        } else if (XmlUtil.matchesTagNS(e, MIMEConstants.QNAME_MIME_XML)) {
            MIMEXml mimeXml = parseMIMEXml(context, e);
            parent.addExtension(mimeXml);
            return true;
        } else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false; // keep compiler happy
        }
    }

    protected MIMEContent parseMIMEContent(ParserContext context, Element e) {
        context.push();
        context.registerNamespaces(e);

        MIMEContent content = new MIMEContent();

        String part = XmlUtil.getAttributeOrNull(e, Constants.ATTR_PART);
        if (part != null) {
            content.setPart(part);
        }

        String type = XmlUtil.getAttributeOrNull(e, Constants.ATTR_TYPE);
        if (type != null) {
            content.setType(type);
        }

        context.pop();
        context.fireDoneParsingEntity(MIMEConstants.QNAME_CONTENT, content);
        return content;
    }

    protected MIMEXml parseMIMEXml(ParserContext context, Element e) {
        context.push();
        context.registerNamespaces(e);

        MIMEXml mimeXml = new MIMEXml();

        String part = XmlUtil.getAttributeOrNull(e, Constants.ATTR_PART);
        if (part != null) {
            mimeXml.setPart(part);
        }

        context.pop();
        context.fireDoneParsingEntity(MIMEConstants.QNAME_MIME_XML, mimeXml);
        return mimeXml;
    }

    public void doHandleExtension(WriterContext context, Extension extension)
        throws IOException {
        // NOTE - this ugliness can be avoided by moving all the XML parsing/writing code
        // into the document classes themselves
        if (extension instanceof MIMEContent) {
            MIMEContent content = (MIMEContent) extension;
            context.writeStartTag(content.getElementName());
            context.writeAttribute(Constants.ATTR_PART, content.getPart());
            context.writeAttribute(Constants.ATTR_TYPE, content.getType());
            context.writeEndTag(content.getElementName());
        } else if (extension instanceof MIMEXml) {
            MIMEXml mimeXml = (MIMEXml) extension;
            context.writeStartTag(mimeXml.getElementName());
            context.writeAttribute(Constants.ATTR_PART, mimeXml.getPart());
            context.writeEndTag(mimeXml.getElementName());
        } else if (extension instanceof MIMEMultipartRelated) {
            MIMEMultipartRelated mpr = (MIMEMultipartRelated) extension;
            context.writeStartTag(mpr.getElementName());
            for (Iterator iter = mpr.getParts(); iter.hasNext();) {
                MIMEPart part = (MIMEPart) iter.next();
                context.writeStartTag(part.getElementName());
                for (Iterator iter2 = part.extensions(); iter2.hasNext();) {
                    Extension e = (Extension) iter2.next();
                    ExtensionHandler h =
                        (ExtensionHandler) _extensionHandlers.get(
                            e.getElementName().getNamespaceURI());
                    if (h != null) {
                        h.doHandleExtension(context, e);
                    }
                }
                context.writeEndTag(part.getElementName());
            }
            context.writeEndTag(mpr.getElementName());
        } else {
            throw new IllegalArgumentException();
        }
    }
}
