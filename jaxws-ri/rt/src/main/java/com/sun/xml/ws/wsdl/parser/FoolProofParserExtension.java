/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.wsdl.parser;


import com.sun.xml.ws.api.model.wsdl.*;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

/**
 * {@link WSDLParserExtension} filter that checks if
 * another {@link WSDLParserExtension} is following the contract.
 *
 * <p>
 * This code protects the JAX-WS RI from broken extensions.
 *
 * <p>
 * For now it just checks if {@link XMLStreamReader} is placed
 * at the expected start/end element.
 *
 * @author Kohsuke Kawaguchi
 */
final class FoolProofParserExtension extends DelegatingParserExtension {

    public FoolProofParserExtension(WSDLParserExtension core) {
        super(core);
    }

    private QName pre(XMLStreamReader xsr) {
        return xsr.getName();
    }

    private boolean post(QName tagName, XMLStreamReader xsr, boolean result) {
        if(!tagName.equals(xsr.getName()))
            return foundFool();
        if(result) {
            if(xsr.getEventType()!=XMLStreamConstants.END_ELEMENT)
                foundFool();
        } else {
            if(xsr.getEventType()!=XMLStreamConstants.START_ELEMENT)
                foundFool();
        }
        return result;
    }

    private boolean foundFool() {
        throw new AssertionError("XMLStreamReader is placed at the wrong place after invoking "+core);
    }

    public boolean serviceElements(WSDLService service, XMLStreamReader reader) {
        return post(pre(reader),reader,super.serviceElements(service, reader));
    }

    public boolean portElements(WSDLPort port, XMLStreamReader reader) {
        return post(pre(reader),reader,super.portElements(port, reader));
    }

    public boolean definitionsElements(XMLStreamReader reader) {
        return post(pre(reader),reader,super.definitionsElements(reader));
    }

    public boolean bindingElements(WSDLBoundPortType binding, XMLStreamReader reader) {
        return post(pre(reader),reader,super.bindingElements(binding, reader));
    }

    public boolean portTypeElements(WSDLPortType portType, XMLStreamReader reader) {
        return post(pre(reader),reader,super.portTypeElements(portType, reader));
    }

    public boolean portTypeOperationElements(WSDLOperation operation, XMLStreamReader reader) {
        return post(pre(reader),reader,super.portTypeOperationElements(operation, reader));
    }

    public boolean bindingOperationElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return post(pre(reader),reader,super.bindingOperationElements(operation, reader));
    }

    public boolean messageElements(WSDLMessage msg, XMLStreamReader reader) {
        return post(pre(reader),reader,super.messageElements(msg, reader));
    }

    public boolean portTypeOperationInputElements(WSDLInput input, XMLStreamReader reader) {
        return post(pre(reader),reader,super.portTypeOperationInputElements(input, reader));
    }

    public boolean portTypeOperationOutputElements(WSDLOutput output, XMLStreamReader reader) {
        return post(pre(reader),reader,super.portTypeOperationOutputElements(output, reader));
    }

    public boolean portTypeOperationFaultElements(WSDLFault fault, XMLStreamReader reader) {
        return post(pre(reader),reader,super.portTypeOperationFaultElements(fault, reader));
    }

    public boolean bindingOperationInputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return super.bindingOperationInputElements(operation, reader);
    }

    public boolean bindingOperationOutputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return post(pre(reader),reader,super.bindingOperationOutputElements(operation, reader));
    }

    public boolean bindingOperationFaultElements(WSDLBoundFault fault, XMLStreamReader reader) {
        return post(pre(reader),reader,super.bindingOperationFaultElements(fault, reader));
    }
}
