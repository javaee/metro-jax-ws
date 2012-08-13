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
import com.sun.tools.ws.api.wsdl.TWSDLExtensionHandler;
import com.sun.tools.ws.api.wsdl.TWSDLParserContext;
import com.sun.tools.ws.util.xml.XmlUtil;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken;

import org.w3c.dom.Element;

/**
 * Policies are evaluated at runtime. This class makes sure that wscompile/wsimport
 * ignores all policy elements at tooltime.
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 * @author Fabian Ritzmann
 */

public class Policy12ExtensionHandler extends TWSDLExtensionHandler {
       
    @Override
    public String getNamespaceURI() {
        return NamespaceVersion.v1_2.toString();
    }
    
    @Override
    public boolean handlePortTypeExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }
    
    @Override
    public boolean handleDefinitionsExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }

    @Override
    public boolean handleBindingExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }
    
    @Override
    public boolean handleOperationExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    

    @Override
    public boolean handleInputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    

    @Override
    public boolean handleOutputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    

    @Override
    public boolean handleFaultExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    

    @Override
    public boolean handleServiceExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    
    
    @Override
    public boolean handlePortExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }

    /**
     * Only skip the element if it is a <wsp:Policy/>, <wsp:PolicyReference/> or
     * <wsp:UsingPolicy> element.
     */
    private boolean handleExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return XmlUtil.matchesTagNS(e, NamespaceVersion.v1_2.asQName(XmlToken.Policy))
               || XmlUtil.matchesTagNS(e,NamespaceVersion.v1_2.asQName(XmlToken.PolicyReference))
               || XmlUtil.matchesTagNS(e, NamespaceVersion.v1_2.asQName(XmlToken.UsingPolicy));
    }

}
