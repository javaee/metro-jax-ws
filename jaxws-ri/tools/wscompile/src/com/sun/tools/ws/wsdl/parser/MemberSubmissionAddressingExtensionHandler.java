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
import com.sun.tools.ws.resources.ModelerMessages;
import com.sun.tools.ws.resources.WsdlMessages;
import com.sun.tools.ws.util.xml.XmlUtil;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wsdl.document.Fault;
import com.sun.tools.ws.wsdl.document.Input;
import com.sun.tools.ws.wsdl.document.Output;
import com.sun.xml.ws.addressing.W3CAddressingMetadataConstants;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.Map;

import static com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants.WSA_ACTION_QNAME;

/**
 * @author Arun Gupta
 */
public class MemberSubmissionAddressingExtensionHandler extends W3CAddressingExtensionHandler {

    private ErrorReceiver errReceiver;
    private boolean extensionModeOn;

    public MemberSubmissionAddressingExtensionHandler(Map<String, AbstractExtensionHandler> extensionHandlerMap, ErrorReceiver env, boolean extensionModeOn) {
        super(extensionHandlerMap, env);
        this.errReceiver = env;
        this.extensionModeOn = extensionModeOn;
    }

    @Override
    public String getNamespaceURI() {
        return AddressingVersion.MEMBER.wsdlNsUri;
    }

    protected QName getWSDLExtensionQName() {
        return AddressingVersion.MEMBER.wsdlExtensionTag;
    }

    @Override
    public boolean handlePortExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        // ignore any extension elements
        return false;
    }

    @Override
    public boolean handleInputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        if (extensionModeOn) {
            warn(context.getLocation(e));
            String actionValue = XmlUtil.getAttributeNSOrNull(e, WSA_ACTION_QNAME);
            if (actionValue == null || actionValue.equals("")) {
                return warnEmptyAction(parent, context.getLocation(e));
            }
            ((Input) parent).setAction(actionValue);
            return true;
        } else {
            return fail(context.getLocation(e));
        }
    }

    private boolean fail(Locator location) {
        errReceiver.warning(location,
                ModelerMessages.WSDLMODELER_INVALID_IGNORING_MEMBER_SUBMISSION_ADDRESSING(
                        AddressingVersion.MEMBER.nsUri, W3CAddressingMetadataConstants.WSAM_NAMESPACE_NAME));
        return false;
    }

    private void warn(Locator location) {
        errReceiver.warning(location,
                ModelerMessages.WSDLMODELER_WARNING_MEMBER_SUBMISSION_ADDRESSING_USED(
                        AddressingVersion.MEMBER.nsUri, W3CAddressingMetadataConstants.WSAM_NAMESPACE_NAME));
    }

    @Override
    public boolean handleOutputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        if (extensionModeOn) {
            warn(context.getLocation(e));
            String actionValue = XmlUtil.getAttributeNSOrNull(e, WSA_ACTION_QNAME);
            if (actionValue == null || actionValue.equals("")) {
                return warnEmptyAction(parent, context.getLocation(e));
            }
            ((Output) parent).setAction(actionValue);
            return true;
        } else {
            return fail(context.getLocation(e));
        }
    }

    @Override
    public boolean handleFaultExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        if (extensionModeOn) {
            warn(context.getLocation(e));
            String actionValue = XmlUtil.getAttributeNSOrNull(e, WSA_ACTION_QNAME);
            if (actionValue == null || actionValue.equals("")) {
                errReceiver.warning(context.getLocation(e), WsdlMessages.WARNING_FAULT_EMPTY_ACTION(parent.getNameValue(), parent.getWSDLElementName().getLocalPart(), parent.getParent().getNameValue()));
                return false; // keep compiler happy
            }
            ((Fault) parent).setAction(actionValue);
            return true;
        } else {
            return fail(context.getLocation(e));
        }
    }

    private boolean warnEmptyAction(TWSDLExtensible parent, Locator pos) {
        errReceiver.warning(pos, WsdlMessages.WARNING_INPUT_OUTPUT_EMPTY_ACTION(parent.getWSDLElementName().getLocalPart(), parent.getParent().getNameValue()));
        return false;
    }
}
