/*
 * $Id: HTTPExtensionHandler.java,v 1.4 2005-09-10 19:50:11 kohsuke Exp $
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

import org.w3c.dom.Element;

import com.sun.tools.ws.wsdl.document.http.HTTPAddress;
import com.sun.tools.ws.wsdl.document.http.HTTPBinding;
import com.sun.tools.ws.wsdl.document.http.HTTPConstants;
import com.sun.tools.ws.wsdl.document.http.HTTPOperation;
import com.sun.tools.ws.wsdl.document.http.HTTPUrlEncoded;
import com.sun.tools.ws.wsdl.document.http.HTTPUrlReplacement;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.ParserContext;
import com.sun.tools.ws.wsdl.framework.WriterContext;
import com.sun.tools.ws.util.xml.XmlUtil;

/**
 * The HTTP extension handler for WSDL.
 *
 * @author WS Development Team
 */
public class HTTPExtensionHandler extends ExtensionHandlerBase {

    public HTTPExtensionHandler() {
    }

    public String getNamespaceURI() {
        return Constants.NS_WSDL_HTTP;
    }

    protected boolean handleDefinitionsExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        Util.fail(
            "parsing.invalidExtensionElement",
            e.getTagName(),
            e.getNamespaceURI());
        return false; // keep compiler happy
    }

    protected boolean handleTypesExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        Util.fail(
            "parsing.invalidExtensionElement",
            e.getTagName(),
            e.getNamespaceURI());
        return false; // keep compiler happy
    }

    protected boolean handleBindingExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (XmlUtil.matchesTagNS(e, HTTPConstants.QNAME_BINDING)) {
            context.push();
            context.registerNamespaces(e);

            HTTPBinding binding = new HTTPBinding();

            String verb = Util.getRequiredAttribute(e, Constants.ATTR_VERB);
            binding.setVerb(verb);

            parent.addExtension(binding);
            context.pop();
            context.fireDoneParsingEntity(HTTPConstants.QNAME_BINDING, binding);
            return true;
        } else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false; // keep compiler happy
        }
    }

    protected boolean handleOperationExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (XmlUtil.matchesTagNS(e, HTTPConstants.QNAME_OPERATION)) {
            context.push();
            context.registerNamespaces(e);

            HTTPOperation operation = new HTTPOperation();

            String location =
                Util.getRequiredAttribute(e, Constants.ATTR_LOCATION);
            operation.setLocation(location);

            parent.addExtension(operation);
            context.pop();
            context.fireDoneParsingEntity(
                HTTPConstants.QNAME_OPERATION,
                operation);
            return true;
        } else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false; // keep compiler happy
        }
    }

    protected boolean handleInputExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (XmlUtil.matchesTagNS(e, HTTPConstants.QNAME_URL_ENCODED)) {
            parent.addExtension(new HTTPUrlEncoded());
            return true;
        } else if (
            XmlUtil.matchesTagNS(e, HTTPConstants.QNAME_URL_REPLACEMENT)) {
            parent.addExtension(new HTTPUrlReplacement());
            return true;
        } else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false; // keep compiler happy
        }
    }

    protected boolean handleOutputExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        Util.fail(
            "parsing.invalidExtensionElement",
            e.getTagName(),
            e.getNamespaceURI());
        return false; // keep compiler happy
    }

    protected boolean handleFaultExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        Util.fail(
            "parsing.invalidExtensionElement",
            e.getTagName(),
            e.getNamespaceURI());
        return false; // keep compiler happy
    }

    protected boolean handleServiceExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        Util.fail(
            "parsing.invalidExtensionElement",
            e.getTagName(),
            e.getNamespaceURI());
        return false; // keep compiler happy
    }

    protected boolean handlePortExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (XmlUtil.matchesTagNS(e, HTTPConstants.QNAME_ADDRESS)) {
            context.push();
            context.registerNamespaces(e);

            HTTPAddress address = new HTTPAddress();

            String location =
                Util.getRequiredAttribute(e, Constants.ATTR_LOCATION);
            address.setLocation(location);

            parent.addExtension(address);
            context.pop();
            context.fireDoneParsingEntity(HTTPConstants.QNAME_ADDRESS, address);
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
        Util.fail(
            "parsing.invalidExtensionElement",
            e.getTagName(),
            e.getNamespaceURI());
        return false; // keep compiler happy
    }

    public void doHandleExtension(WriterContext context, Extension extension)
        throws IOException {
        if (extension instanceof HTTPAddress) {
            HTTPAddress address = (HTTPAddress) extension;
            context.writeStartTag(address.getElementName());
            context.writeAttribute(
                Constants.ATTR_LOCATION,
                address.getLocation());
            context.writeEndTag(address.getElementName());
        } else if (extension instanceof HTTPBinding) {
            HTTPBinding binding = (HTTPBinding) extension;
            context.writeStartTag(binding.getElementName());
            context.writeAttribute(Constants.ATTR_VERB, binding.getVerb());
            context.writeEndTag(binding.getElementName());
        } else if (extension instanceof HTTPOperation) {
            HTTPOperation operation = (HTTPOperation) extension;
            context.writeStartTag(operation.getElementName());
            context.writeAttribute(
                Constants.ATTR_LOCATION,
                operation.getLocation());
            context.writeEndTag(operation.getElementName());
        } else if (extension instanceof HTTPUrlEncoded) {
            context.writeStartTag(extension.getElementName());
            context.writeEndTag(extension.getElementName());
        } else if (extension instanceof HTTPUrlReplacement) {
            context.writeStartTag(extension.getElementName());
            context.writeEndTag(extension.getElementName());
        } else {
            throw new IllegalArgumentException();
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handlePortTypeExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handlePortTypeExtension(ParserContext context, Extensible parent, Element e) {
        Util.fail(
            "parsing.invalidExtensionElement",
            e.getTagName(),
            e.getNamespaceURI());
        return false; // keep compiler happy
    }
}
