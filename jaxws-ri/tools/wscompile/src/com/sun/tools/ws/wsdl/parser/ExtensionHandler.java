/*
 * $Id: ExtensionHandler.java,v 1.2 2005-07-18 18:14:22 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.parser;

import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Element;

import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.ParserContext;
import com.sun.tools.ws.wsdl.framework.WriterContext;

/**
 * A handler for extensions elements definined in one namespace.
 *
 * @author WS Development Team
 */
public abstract class ExtensionHandler {

    protected ExtensionHandler() {
    }

    public abstract String getNamespaceURI();

    public void setExtensionHandlers(Map m) {
        _extensionHandlers = m;
    }

    public boolean doHandleExtension(
        ParserContext context,
        Extensible parent,
        Element e) {
        return false;
    }

    public void doHandleExtension(WriterContext context, Extension extension)
        throws IOException {
    }

    protected Map _extensionHandlers;
}
