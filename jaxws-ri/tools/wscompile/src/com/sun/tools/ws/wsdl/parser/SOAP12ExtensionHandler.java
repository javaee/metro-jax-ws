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
package com.sun.tools.ws.wsdl.parser;

import com.sun.tools.ws.wsdl.document.soap.SOAP12Binding;
import com.sun.tools.ws.wsdl.document.soap.SOAP12Constants;
import com.sun.tools.ws.wsdl.document.soap.SOAPBinding;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;
import java.util.Map;


public class SOAP12ExtensionHandler extends SOAPExtensionHandler {
    public SOAP12ExtensionHandler(Map<String, AbstractExtensionHandler> extensionHandlerMap) {
        super(extensionHandlerMap);
    }

    /* 
     * @see SOAPExtensionHandler#getNamespaceURI()
     */
    @Override
    public String getNamespaceURI() {
        return Constants.NS_WSDL_SOAP12;
    }

    /* 
     * @see SOAPExtensionHandler#getAddressQName()
     */
    @Override
    protected QName getAddressQName() {
        return SOAP12Constants.QNAME_ADDRESS;
    }

    /* 
     * @see SOAPExtensionHandler#getBindingQName()
     */
    @Override
    protected QName getBindingQName() {
        return SOAP12Constants.QNAME_BINDING;
    }

    @Override protected SOAPBinding getSOAPBinding(Locator location) {
        return new SOAP12Binding(location);
    }

    /*
     * @see SOAPExtensionHandler#getBodyQName()
     */
    @Override
    protected QName getBodyQName() {
        return SOAP12Constants.QNAME_BODY;
    }

    /* 
     * @see SOAPExtensionHandler#getFaultQName()
     */
    @Override
    protected QName getFaultQName() {
        return SOAP12Constants.QNAME_FAULT;
    }

    /* 
     * @see SOAPExtensionHandler#getHeaderfaultQName()
     */
    @Override
    protected QName getHeaderfaultQName() {
        return SOAP12Constants.QNAME_HEADERFAULT;
    }

    /* 
     * @see SOAPExtensionHandler#getHeaderQName()
     */
    @Override
    protected QName getHeaderQName() {
        return SOAP12Constants.QNAME_HEADER;
    }

    /* 
     * @see SOAPExtensionHandler#getOperationQName()
     */
    @Override
    protected QName getOperationQName() {
        return SOAP12Constants.QNAME_OPERATION;
    }
}
