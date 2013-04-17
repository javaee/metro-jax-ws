/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package testutil;

import javax.xml.namespace.QName;

import com.sun.xml.txw2.TypedXmlWriter;

import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;

import java.lang.reflect.Method;


public class WSDLGeneratorExtensionImpl extends WSDLGeneratorExtension {
    private void addAttribute(TypedXmlWriter writer, String attrName, String attrValue) {
        writer._attribute(new QName("jaxws_test", attrName), attrValue);
    }

    public void addServiceExtension(TypedXmlWriter service) {
        addAttribute(service, "serviceAttr", "myService");
    }

    public void addPortExtension(TypedXmlWriter port) {
        addAttribute(port, "portAttr", "myPort");
    }

    public void addPortTypeExtension(TypedXmlWriter portType) {
        addAttribute(portType, "portTypeAttr", "myPortType");
    }

    public void addBindingExtension(TypedXmlWriter binding) {
        addAttribute(binding, "bindingAttr", "myBinding");
    }

    public void addOperationExtension(TypedXmlWriter operation, Method method) {
        addAttribute(operation, "operationAttr", "myOperation");
    }

    public void addBindingOperationExtension(TypedXmlWriter operation, Method method) {
        addAttribute(operation, "bindingOperationAttr", "myBindingOperation");
    }

    public void addInputMessageExtension(TypedXmlWriter message, Method method) {
        addAttribute(message, "inputMessageAttr", "myInputMessage");
    }

    public void addOutputMessageExtension(TypedXmlWriter message, Method method) {
        addAttribute(message, "outputMessageAttr", "myOutputMessage");
    }

    public void addOperationInputExtension(TypedXmlWriter input, Method method) {
        addAttribute(input, "operationInputAttr", "myOperationInput");
    }

    public void addOperationOutputExtension(TypedXmlWriter output, Method method) {
        addAttribute(output, "operationOutputAttr", "myOperationOutput");
    }

    public void addBindingOperationInputExtension(TypedXmlWriter input, Method method) {
        addAttribute(input, "bindingOperationInputAttr", "myBindingOperationInput");
    }

    public void addBindingOperationOutputExtension(TypedXmlWriter output, Method method) {
        addAttribute(output, "bindingOperationOutputAttr", "myBindingOperationOutput");
    }

    public void addBindingOperationFaultExtension(TypedXmlWriter fault, Method method) {
        addAttribute(fault, "bindingOperationFaultAttr", "myBindingOperationFault");
    }

    public void addFaultMessageExtension(TypedXmlWriter message, Method method) {
        addAttribute(message, "faultMessageAttr", "myFaultMessage");
    }

    public void addOperationFaultExtension(TypedXmlWriter fault, Method method, CheckedException ce) {
        addAttribute(fault, "operationFaultAttr", "myOperationFault");
    }
}
