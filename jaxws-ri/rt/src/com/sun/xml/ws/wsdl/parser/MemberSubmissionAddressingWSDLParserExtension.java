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

package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLFeaturedObject;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.model.wsdl.WSDLBoundPortTypeImpl;
import com.sun.xml.ws.model.wsdl.WSDLOperationImpl;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 * Member Submission WS-Addressing Runtime WSDL parser extension
 *
 * @author Arun Gupta
 */
public class MemberSubmissionAddressingWSDLParserExtension extends W3CAddressingWSDLParserExtension {
    @Override
    public boolean bindingElements(WSDLBoundPortType binding, XMLStreamReader reader) {
        return addressibleElement(reader, binding);
    }

    @Override
    public boolean portElements(WSDLPort port, XMLStreamReader reader) {
        return addressibleElement(reader, port);
    }

    private boolean addressibleElement(XMLStreamReader reader, WSDLFeaturedObject binding) {
        QName ua = reader.getName();
        if (ua.equals(AddressingVersion.MEMBER.wsdlExtensionTag)) {
            String required = reader.getAttributeValue(WSDLConstants.NS_WSDL, "required");
            binding.addFeature(new MemberSubmissionAddressingFeature(Boolean.parseBoolean(required)));
            XMLStreamReaderUtil.skipElement(reader);
            return true;        // UsingAddressing is consumed
        }

        return false;
    }

    @Override
    public boolean bindingOperationElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return false;
    }

    @Override
    public boolean portTypeOperationInput(WSDLOperation o, XMLStreamReader reader) {
        WSDLOperationImpl impl = (WSDLOperationImpl)o;

        String action = ParserUtil.getAttribute(reader, AddressingVersion.MEMBER.wsdlActionTag);
        if (action != null) {
            impl.getInput().setAction(action);
            impl.getInput().setDefaultAction(false);
        }

        return false;
    }

    @Override
    public boolean portTypeOperationOutput(WSDLOperation o, XMLStreamReader reader) {
        WSDLOperationImpl impl = (WSDLOperationImpl)o;

        String action = ParserUtil.getAttribute(reader, AddressingVersion.MEMBER.wsdlActionTag);
        if (action != null) {
            impl.getOutput().setAction(action);
        }

        return false;
    }

    @Override
    public boolean portTypeOperationFault(WSDLOperation o, XMLStreamReader reader) {
        WSDLOperationImpl impl = (WSDLOperationImpl)o;

        String action = ParserUtil.getAttribute(reader, AddressingVersion.MEMBER.wsdlActionTag);
        if (action != null) {
            String name = ParserUtil.getMandatoryNonEmptyAttribute(reader, "name");
            impl.getFaultActionMap().put(name, action);
        }

        return false;
    }

    @Override
    protected void patchAnonymousDefault(WSDLBoundPortTypeImpl binding) {
    }

    @Override
    protected String getNamespaceURI() {
        return AddressingVersion.MEMBER.wsdlNsUri;
    }
}
