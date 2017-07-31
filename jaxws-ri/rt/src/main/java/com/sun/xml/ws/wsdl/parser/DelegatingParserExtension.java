/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import com.sun.xml.ws.api.model.wsdl.editable.*;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtensionContext;

import javax.xml.stream.XMLStreamReader;

/**
 * Delegate to another {@link WSDLParserExtension}
 * useful for the base class for filtering. 
 *
 * @author Kohsuke Kawaguchi
 */
class DelegatingParserExtension extends WSDLParserExtension {
    protected final WSDLParserExtension core;

    public DelegatingParserExtension(WSDLParserExtension core) {
        this.core = core;
    }

    public void start(WSDLParserExtensionContext context) {
        core.start(context);
    }

    public void serviceAttributes(EditableWSDLService service, XMLStreamReader reader) {
        core.serviceAttributes(service, reader);
    }

    public boolean serviceElements(EditableWSDLService service, XMLStreamReader reader) {
        return core.serviceElements(service, reader);
    }

    public void portAttributes(EditableWSDLPort port, XMLStreamReader reader) {
        core.portAttributes(port, reader);
    }

    public boolean portElements(EditableWSDLPort port, XMLStreamReader reader) {
        return core.portElements(port, reader);
    }

    public boolean portTypeOperationInput(EditableWSDLOperation op, XMLStreamReader reader) {
        return core.portTypeOperationInput(op, reader);
    }

    public boolean portTypeOperationOutput(EditableWSDLOperation op, XMLStreamReader reader) {
        return core.portTypeOperationOutput(op, reader);
    }

    public boolean portTypeOperationFault(EditableWSDLOperation op, XMLStreamReader reader) {
        return core.portTypeOperationFault(op, reader);
    }

    public boolean definitionsElements(XMLStreamReader reader) {
        return core.definitionsElements(reader);
    }

    public boolean bindingElements(EditableWSDLBoundPortType binding, XMLStreamReader reader) {
        return core.bindingElements(binding, reader);
    }

    public void bindingAttributes(EditableWSDLBoundPortType binding, XMLStreamReader reader) {
        core.bindingAttributes(binding, reader);
    }

    public boolean portTypeElements(EditableWSDLPortType portType, XMLStreamReader reader) {
        return core.portTypeElements(portType, reader);
    }

    public void portTypeAttributes(EditableWSDLPortType portType, XMLStreamReader reader) {
        core.portTypeAttributes(portType, reader);
    }

    public boolean portTypeOperationElements(EditableWSDLOperation operation, XMLStreamReader reader) {
        return core.portTypeOperationElements(operation, reader);
    }

    public void portTypeOperationAttributes(EditableWSDLOperation operation, XMLStreamReader reader) {
        core.portTypeOperationAttributes(operation, reader);
    }

    public boolean bindingOperationElements(EditableWSDLBoundOperation operation, XMLStreamReader reader) {
        return core.bindingOperationElements(operation, reader);
    }

    public void bindingOperationAttributes(EditableWSDLBoundOperation operation, XMLStreamReader reader) {
        core.bindingOperationAttributes(operation, reader);
    }

    public boolean messageElements(EditableWSDLMessage msg, XMLStreamReader reader) {
        return core.messageElements(msg, reader);
    }

    public void messageAttributes(EditableWSDLMessage msg, XMLStreamReader reader) {
        core.messageAttributes(msg, reader);
    }

    public boolean portTypeOperationInputElements(EditableWSDLInput input, XMLStreamReader reader) {
        return core.portTypeOperationInputElements(input, reader);
    }

    public void portTypeOperationInputAttributes(EditableWSDLInput input, XMLStreamReader reader) {
        core.portTypeOperationInputAttributes(input, reader);
    }

    public boolean portTypeOperationOutputElements(EditableWSDLOutput output, XMLStreamReader reader) {
        return core.portTypeOperationOutputElements(output, reader);
    }

    public void portTypeOperationOutputAttributes(EditableWSDLOutput output, XMLStreamReader reader) {
        core.portTypeOperationOutputAttributes(output, reader);
    }

    public boolean portTypeOperationFaultElements(EditableWSDLFault fault, XMLStreamReader reader) {
        return core.portTypeOperationFaultElements(fault, reader);
    }

    public void portTypeOperationFaultAttributes(EditableWSDLFault fault, XMLStreamReader reader) {
        core.portTypeOperationFaultAttributes(fault, reader);
    }

    public boolean bindingOperationInputElements(EditableWSDLBoundOperation operation, XMLStreamReader reader) {
        return core.bindingOperationInputElements(operation, reader);
    }

    public void bindingOperationInputAttributes(EditableWSDLBoundOperation operation, XMLStreamReader reader) {
        core.bindingOperationInputAttributes(operation, reader);
    }

    public boolean bindingOperationOutputElements(EditableWSDLBoundOperation operation, XMLStreamReader reader) {
        return core.bindingOperationOutputElements(operation, reader);
    }

    public void bindingOperationOutputAttributes(EditableWSDLBoundOperation operation, XMLStreamReader reader) {
        core.bindingOperationOutputAttributes(operation, reader);
    }

    public boolean bindingOperationFaultElements(EditableWSDLBoundFault fault, XMLStreamReader reader) {
        return core.bindingOperationFaultElements(fault, reader);
    }

    public void bindingOperationFaultAttributes(EditableWSDLBoundFault fault, XMLStreamReader reader) {
        core.bindingOperationFaultAttributes(fault, reader);
    }

    public void finished(WSDLParserExtensionContext context) {
        core.finished(context);
    }

    public void postFinished(WSDLParserExtensionContext context) {
        core.postFinished(context);
    }
}
