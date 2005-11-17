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
