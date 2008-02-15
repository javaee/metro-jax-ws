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

package com.sun.tools.ws.api.wsdl;

import com.sun.tools.ws.wsdl.document.WSDLConstants;
import org.w3c.dom.Element;

/**
 * JAXWS WSDL parser {@link com.sun.tools.ws.wsdl.parser.WSDLParser} will call an {@link TWSDLExtensionHandler} registered
 * with it for the WSDL extensibility elements thats not already defined in the WSDL 1.1 spec, such as SOAP or MIME.
 *
 * @author Vivek Pandey
 * @deprecated This class is deprecated, will be removed in JAX-WS 2.2 RI.
 */
public abstract class TWSDLExtensionHandler {
    /**
     * Gives the namespace of an extensibility element.
     * <p/>
     * For example a soap 1.1 XXExtensionHandler would return <code>""http://schemas.xmlsoap.org/wsdl/soap/"</code>
     */
    public String getNamespaceURI() {
        return null;
    }

    /**
     * This interface is called during WSDL parsing on detecting any wsdl extension.
     * 
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean doHandleExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        if (parent.getWSDLElementName().equals(WSDLConstants.QNAME_DEFINITIONS)) {
            return handleDefinitionsExtension(context, parent, e);
        } else if (parent.getWSDLElementName().equals(WSDLConstants.QNAME_TYPES)) {
            return handleTypesExtension(context, parent, e);
        } else if (parent.getWSDLElementName().equals(WSDLConstants.QNAME_PORT_TYPE)) {
            return handlePortTypeExtension(context, parent, e);
        } else if (
            parent.getWSDLElementName().equals(WSDLConstants.QNAME_BINDING)) {
            return handleBindingExtension(context, parent, e);
        } else if (
            parent.getWSDLElementName().equals(WSDLConstants.QNAME_OPERATION)) {
            return handleOperationExtension(context, parent, e);
        } else if (parent.getWSDLElementName().equals(WSDLConstants.QNAME_INPUT)) {
            return handleInputExtension(context, parent, e);
        } else if (
            parent.getWSDLElementName().equals(WSDLConstants.QNAME_OUTPUT)) {
            return handleOutputExtension(context, parent, e);
        } else if (parent.getWSDLElementName().equals(WSDLConstants.QNAME_FAULT)) {
            return handleFaultExtension(context, parent, e);
        } else if (
            parent.getWSDLElementName().equals(WSDLConstants.QNAME_SERVICE)) {
            return handleServiceExtension(context, parent, e);
        } else if (parent.getWSDLElementName().equals(WSDLConstants.QNAME_PORT)) {
            return handlePortExtension(context, parent, e);
        } else {
            return false;
        }
    }

    /**
     * Callback for <code>wsdl:portType</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handlePortTypeExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:definitions</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handleDefinitionsExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:type</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handleTypesExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:binding</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handleBindingExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:portType/wsdl:operation</code>.
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handleOperationExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:input</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handleInputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:output</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handleOutputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:fault</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handleFaultExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:service</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handleServiceExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }

    /**
     * Callback for <code>wsdl:port</code>
     *
     * @param context Parser context that will be passed on by the wsdl parser
     * @param parent  The Parent element within which the extensibility element is defined
     * @param e       The extensibility elemenet
     * @return false if there was some error during the extension handling otherwise returns true. If returned false
     *         then the WSDL parser can abort if the wsdl extensibility element had <code>required</code> attribute set to true
     */
    public boolean handlePortExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return false;
    }
}
