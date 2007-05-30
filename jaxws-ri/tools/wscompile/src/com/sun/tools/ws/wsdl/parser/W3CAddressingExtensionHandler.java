/*
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the "License").  You may not use this file except
 in compliance with the License.
 
 You can obtain a copy of the license at
 https://jwsdp.dev.java.net/CDDLv1.0.html
 See the License for the specific language governing
 permissions and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 HEADER in each file and include the License file at
 https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 add the following below this CDDL HEADER, with the
 fields enclosed by brackets "[]" replaced with your
 own identifying information: Portions Copyright [yyyy]
 [name of copyright owner]
*/
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
/*
 * $Id: W3CAddressingExtensionHandler.java,v 1.4 2007-05-30 01:03:23 ofung Exp $
 */

package com.sun.tools.ws.wsdl.parser;

import com.sun.tools.ws.api.wsdl.TWSDLExtensible;
import com.sun.tools.ws.api.wsdl.TWSDLParserContext;
import com.sun.tools.ws.resources.WsdlMessages;
import com.sun.tools.ws.util.xml.XmlUtil;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wsdl.document.Fault;
import com.sun.tools.ws.wsdl.document.Input;
import com.sun.tools.ws.wsdl.document.Output;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Arun Gupta
 */
public class W3CAddressingExtensionHandler extends AbstractExtensionHandler {
    private ErrorReceiver errReceiver;

    public W3CAddressingExtensionHandler(Map<String, AbstractExtensionHandler> extensionHandlerMap) {
        this(extensionHandlerMap, null);
    }

    public W3CAddressingExtensionHandler(Map<String, AbstractExtensionHandler> extensionHandlerMap, ErrorReceiver errReceiver) {
        super(extensionHandlerMap);
        this.errReceiver = errReceiver;
    }

    @Override
    public String getNamespaceURI() {
        return AddressingVersion.W3C.wsdlNsUri;
    }

    protected QName getActionQName() {
        return AddressingVersion.W3C.wsdlActionTag;
    }

    protected QName getWSDLExtensionQName() {
        return AddressingVersion.W3C.wsdlExtensionTag;
    }

    @Override
    public boolean handleBindingExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        if (XmlUtil.matchesTagNS(e, getWSDLExtensionQName())) {
            context.push();
            context.registerNamespaces(e);

            // TODO: read UsingAddressing extensibility element and store
            // TODO: it as extension in "parent". It may be used to generate
            // TODO: @Action/@FaultAction later.

            context.pop();
            return true;
        }
        return false; // keep compiler happy
    }

    @Override
    public boolean handleInputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        String actionValue = XmlUtil.getAttributeNSOrNull(e, getActionQName());
        if (actionValue == null || actionValue.equals("")) {
            return warnEmptyAction(parent, context.getLocation(e));
        }

        context.push();
        ((Input)parent).setAction(actionValue);
        context.pop();

        return true;
    }

    @Override
    public boolean handleOutputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        String actionValue = XmlUtil.getAttributeNSOrNull(e, getActionQName());
        if (actionValue == null || actionValue.equals("")) {
            return warnEmptyAction(parent,context.getLocation(e));
        }

        context.push();
        ((Output)parent).setAction(actionValue);
        context.pop();

        return true;
    }

    @Override
    public boolean handleFaultExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        String actionValue = XmlUtil.getAttributeNSOrNull(e, getActionQName());
        if (actionValue == null || actionValue.equals("")) {
            errReceiver.warning(context.getLocation(e), WsdlMessages.WARNING_FAULT_EMPTY_ACTION(parent.getNameValue(), parent.getWSDLElementName().getLocalPart(), parent.getParent().getNameValue()));
            return false; // keep compiler happy
        }

        context.push();
        ((Fault)parent).setAction(actionValue);
        context.pop();

        return true;
    }

    @Override
    public boolean handlePortExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleBindingExtension(context, parent, e);
    }

    private boolean warnEmptyAction(TWSDLExtensible parent, Locator pos) {
        errReceiver.warning(pos, WsdlMessages.WARNING_INPUT_OUTPUT_EMPTY_ACTION(parent.getWSDLElementName().getLocalPart(), parent.getParent().getNameValue()));
        return false; // keep compiler happy
    }
}
