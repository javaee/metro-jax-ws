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

import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLInput;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
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

    public void serviceAttributes(WSDLService service, XMLStreamReader reader) {
        core.serviceAttributes(service, reader);
    }

    public boolean serviceElements(WSDLService service, XMLStreamReader reader) {
        return core.serviceElements(service, reader);
    }

    public void portAttributes(WSDLPort port, XMLStreamReader reader) {
        core.portAttributes(port, reader);
    }

    public boolean portElements(WSDLPort port, XMLStreamReader reader) {
        return core.portElements(port, reader);
    }

    public boolean portTypeOperationInput(WSDLOperation op, XMLStreamReader reader) {
        return core.portTypeOperationInput(op, reader);
    }

    public boolean portTypeOperationOutput(WSDLOperation op, XMLStreamReader reader) {
        return core.portTypeOperationOutput(op, reader);
    }

    public boolean portTypeOperationFault(WSDLOperation op, XMLStreamReader reader) {
        return core.portTypeOperationFault(op, reader);
    }

    public boolean definitionsElements(XMLStreamReader reader) {
        return core.definitionsElements(reader);
    }

    public boolean bindingElements(WSDLBoundPortType binding, XMLStreamReader reader) {
        return core.bindingElements(binding, reader);
    }

    public void bindingAttributes(WSDLBoundPortType binding, XMLStreamReader reader) {
        core.bindingAttributes(binding, reader);
    }

    public boolean portTypeElements(WSDLPortType portType, XMLStreamReader reader) {
        return core.portTypeElements(portType, reader);
    }

    public void portTypeAttributes(WSDLPortType portType, XMLStreamReader reader) {
        core.portTypeAttributes(portType, reader);
    }

    public boolean portTypeOperationElements(WSDLOperation operation, XMLStreamReader reader) {
        return core.portTypeOperationElements(operation, reader);
    }

    public void portTypeOperationAttributes(WSDLOperation operation, XMLStreamReader reader) {
        core.portTypeOperationAttributes(operation, reader);
    }

    public boolean bindingOperationElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return core.bindingOperationElements(operation, reader);
    }

    public void bindingOperationAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        core.bindingOperationAttributes(operation, reader);
    }

    public boolean messageElements(WSDLMessage msg, XMLStreamReader reader) {
        return core.messageElements(msg, reader);
    }

    public void messageAttributes(WSDLMessage msg, XMLStreamReader reader) {
        core.messageAttributes(msg, reader);
    }

    public boolean portTypeOperationInputElements(WSDLInput input, XMLStreamReader reader) {
        return core.portTypeOperationInputElements(input, reader);
    }

    public void portTypeOperationInputAttributes(WSDLInput input, XMLStreamReader reader) {
        core.portTypeOperationInputAttributes(input, reader);
    }

    public boolean portTypeOperationOutputElements(WSDLOutput output, XMLStreamReader reader) {
        return core.portTypeOperationOutputElements(output, reader);
    }

    public void portTypeOperationOutputAttributes(WSDLOutput output, XMLStreamReader reader) {
        core.portTypeOperationOutputAttributes(output, reader);
    }

    public boolean portTypeOperationFaultElements(WSDLFault fault, XMLStreamReader reader) {
        return core.portTypeOperationFaultElements(fault, reader);
    }

    public void portTypeOperationFaultAttributes(WSDLFault fault, XMLStreamReader reader) {
        core.portTypeOperationFaultAttributes(fault, reader);
    }

    public boolean bindingOperationInputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return core.bindingOperationInputElements(operation, reader);
    }

    public void bindingOperationInputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        core.bindingOperationInputAttributes(operation, reader);
    }

    public boolean bindingOperationOutputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return core.bindingOperationOutputElements(operation, reader);
    }

    public void bindingOperationOutputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        core.bindingOperationOutputAttributes(operation, reader);
    }

    public boolean bindingOperationFaultElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return core.bindingOperationFaultElements(operation, reader);
    }

    public void bindingOperationFaultAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        core.bindingOperationFaultAttributes(operation, reader);
    }

    public void finished(WSDLParserExtensionContext context) {
        core.finished(context);
    }

    public void postFinished(WSDLParserExtensionContext context) {
        core.postFinished(context);
    }
}
