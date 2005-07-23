/*
 * $Id: ExtensionHandlerBase.java,v 1.3 2005-07-23 04:11:06 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.parser;

import org.w3c.dom.Element;

import com.sun.tools.ws.wsdl.document.WSDLConstants;
import com.sun.tools.ws.wsdl.document.mime.MIMEConstants;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.ParserContext;
/**
 * A base class for WSDL extension handlers.
 *
 * @author WS Development Team
 */
public abstract class ExtensionHandlerBase extends ExtensionHandler {

    protected ExtensionHandlerBase() {
    }

    public boolean doHandleExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        if (parent.getElementName().equals(WSDLConstants.QNAME_DEFINITIONS)) {
            return handleDefinitionsExtension(context, parent, e);
        } else if (parent.getElementName().equals(WSDLConstants.QNAME_TYPES)) {
            return handleTypesExtension(context, parent, e);
        } else if (parent.getElementName().equals(WSDLConstants.QNAME_PORT_TYPE)) {
            return handlePortTypeExtension(context, parent, e);
        } else if (
            parent.getElementName().equals(WSDLConstants.QNAME_BINDING)) {
            return handleBindingExtension(context, parent, e);
        } else if (
            parent.getElementName().equals(WSDLConstants.QNAME_OPERATION)) {
            return handleOperationExtension(context, parent, e);
        } else if (parent.getElementName().equals(WSDLConstants.QNAME_INPUT)) {
            return handleInputExtension(context, parent, e);
        } else if (
            parent.getElementName().equals(WSDLConstants.QNAME_OUTPUT)) {
            return handleOutputExtension(context, parent, e);
        } else if (parent.getElementName().equals(WSDLConstants.QNAME_FAULT)) {
            return handleFaultExtension(context, parent, e);
        } else if (
            parent.getElementName().equals(WSDLConstants.QNAME_SERVICE)) {
            return handleServiceExtension(context, parent, e);
        } else if (parent.getElementName().equals(WSDLConstants.QNAME_PORT)) {
            return handlePortExtension(context, parent, e);
        } else if (parent.getElementName().equals(MIMEConstants.QNAME_PART)) {
            return handleMIMEPartExtension(context, parent, e);
        } else {
            return false;
        }
    }

    /**
     * @param context
     * @param parent
     * @param e
     * @return true if the PortTypeExtension should be handled
     */
    protected abstract boolean handlePortTypeExtension(
        ParserContext context,
        Extensible parent,
        Element e);

    protected abstract boolean handleDefinitionsExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handleTypesExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handleBindingExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handleOperationExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handleInputExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handleOutputExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handleFaultExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handleServiceExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handlePortExtension(
        ParserContext context,
        Extensible parent,
        Element e);
    protected abstract boolean handleMIMEPartExtension(
        ParserContext context,
        Extensible parent,
        Element e);
}
