/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.tools.ws.wsdl.parser;

import com.sun.tools.ws.api.wsdl.TWSDLExtensible;
import com.sun.tools.ws.api.wsdl.TWSDLParserContext;
import com.sun.tools.ws.util.xml.XmlUtil;
import com.sun.tools.ws.wsdl.document.WSDLConstants;
import com.sun.tools.ws.wsdl.document.mime.*;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Map;

/**
 * The MIME extension handler for WSDL.
 *
 * @author WS Development Team
 */
public class MIMEExtensionHandler extends AbstractExtensionHandler {

    public MIMEExtensionHandler(Map<String, AbstractExtensionHandler> extensionHandlerMap) {
        super(extensionHandlerMap);
    }

    public String getNamespaceURI() {
        return Constants.NS_WSDL_MIME;
    }

    @Override
    public boolean doHandleExtension(
        TWSDLParserContext context,
        TWSDLExtensible parent,
        Element e) {
        if (parent.getWSDLElementName().equals(WSDLConstants.QNAME_OUTPUT)) {
            return handleInputOutputExtension(context, parent, e);
        } else if (parent.getWSDLElementName().equals(WSDLConstants.QNAME_INPUT)) {
            return handleInputOutputExtension(context, parent, e);
        } else if (parent.getWSDLElementName().equals(MIMEConstants.QNAME_PART)) {
            return handleMIMEPartExtension(context, parent, e);
        } else {
//            context.fireIgnoringExtension(
//                new QName(e.getNamespaceURI(), e.getLocalName()),
//                parent.getWSDLElementName());
            return false;
        }
    }

    protected boolean handleInputOutputExtension(
        TWSDLParserContext context,
        TWSDLExtensible parent,
        Element e) {
        if (XmlUtil.matchesTagNS(e, MIMEConstants.QNAME_MULTIPART_RELATED)) {
            context.push();
            context.registerNamespaces(e);

            MIMEMultipartRelated mpr = new MIMEMultipartRelated(context.getLocation(e));

            for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if (XmlUtil.matchesTagNS(e2, MIMEConstants.QNAME_PART)) {
                    context.push();
                    context.registerNamespaces(e2);

                    MIMEPart part = new MIMEPart(context.getLocation(e2));

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

                        AbstractExtensionHandler h = getExtensionHandlers().get(e3.getNamespaceURI());
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
//                                context.fireIgnoringExtension(
//                                    new QName(
//                                        e3.getNamespaceURI(),
//                                        e3.getLocalName()),
//                                    part.getElementName());
                            }
                        }
                    }

                    mpr.add(part);
                    context.pop();
//                    context.fireDoneParsingEntity(
//                        MIMEConstants.QNAME_PART,
//                        part);
                } else {
                    Util.fail(
                        "parsing.invalidElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                }
            }

            parent.addExtension(mpr);
            context.pop();
//            context.fireDoneParsingEntity(
//                MIMEConstants.QNAME_MULTIPART_RELATED,
//                mpr);
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

    @Override
    protected boolean handleMIMEPartExtension(
        TWSDLParserContext context,
        TWSDLExtensible parent,
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

    protected MIMEContent parseMIMEContent(TWSDLParserContext context, Element e) {
        context.push();
        context.registerNamespaces(e);

        MIMEContent content = new MIMEContent(context.getLocation(e));

        String part = XmlUtil.getAttributeOrNull(e, Constants.ATTR_PART);
        if (part != null) {
            content.setPart(part);
        }

        String type = XmlUtil.getAttributeOrNull(e, Constants.ATTR_TYPE);
        if (type != null) {
            content.setType(type);
        }

        context.pop();
//        context.fireDoneParsingEntity(MIMEConstants.QNAME_CONTENT, content);
        return content;
    }

    protected MIMEXml parseMIMEXml(TWSDLParserContext context, Element e) {
        context.push();
        context.registerNamespaces(e);

        MIMEXml mimeXml = new MIMEXml(context.getLocation(e));

        String part = XmlUtil.getAttributeOrNull(e, Constants.ATTR_PART);
        if (part != null) {
            mimeXml.setPart(part);
        }

        context.pop();
//        context.fireDoneParsingEntity(MIMEConstants.QNAME_MIME_XML, mimeXml);
        return mimeXml;
    }
}
