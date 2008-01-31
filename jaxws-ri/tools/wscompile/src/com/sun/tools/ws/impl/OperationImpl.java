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
package com.sun.tools.ws.impl;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.tools.ws.api.JavaVisitor;
import com.sun.tools.ws.api.Operation;
import com.sun.tools.ws.api.Parameter;
import org.jvnet.wom.api.WSDLBoundOutput;
import org.jvnet.wom.api.WSDLBoundPortType;
import org.jvnet.wom.api.WSDLOperation;
import org.jvnet.wom.api.WSDLPart;
import org.jvnet.wom.api.binding.wsdl11.soap.SOAPBinding;
import org.jvnet.wom.api.binding.wsdl11.soap.SOAPBody;
import org.jvnet.wom.api.binding.wsdl11.soap.SOAPOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class OperationImpl implements Operation {
    private JMethod jmethod;
    private final JDefinedClass owner;
    private final WSDLOperation wsdlOperation;
    private final ModelerContext context;
    private final List<Parameter> params = new ArrayList<Parameter>();

    private final List<WSDLPart> inParts;
    private final List<WSDLPart> outParts;
    private final List<WSDLPart> parameterList = new ArrayList<WSDLPart>();
    private WSDLPart returnType;
    private SOAPBinding.Style style;
    private SOAPBody.Use use;

    public OperationImpl(WSDLOperation wsdlOperation, JDefinedClass owner, ModelerContext context) {
        this.owner = owner;
        this.wsdlOperation = wsdlOperation;
        this.context = context;

        inParts = new ArrayList<WSDLPart>(wsdlOperation.getInput().getParts());
        outParts = wsdlOperation.getOutput() == null ? null : new ArrayList<WSDLPart>(wsdlOperation.getOutput().getParts());
        WSDLBoundPortType binding = context.getWsdlSet().boundPortType(wsdlOperation.getPortType().getName());
        SOAPBinding sb = binding.getFirstExtension(SOAPBinding.class);
        SOAPOperation so = wsdlOperation.getFirstExtension(SOAPOperation.class);
        style = so.getStyle() != null ? so.getStyle() : sb.getStyle();
        SOAPBody body = wsdlOperation.getInput().getFirstExtension(SOAPBody.class);
        if (body != null) {
            use = body.getUse() == null ? SOAPBody.Use.literal : body.getUse();
        }
        processParameterOrder();
    }

    private boolean isDocumentLiteral() {
        return style == SOAPBinding.Style.Document && use == SOAPBody.Use.literal;
    }

    private boolean isRpcLiteral() {
        return style == SOAPBinding.Style.Rpc && use == SOAPBody.Use.literal;
    }

    private boolean isRpcEncoded() {
        return style == SOAPBinding.Style.Rpc && use == SOAPBody.Use.encoded;
    }

    private boolean isWrapperStyle;

    public boolean isWrapperStyle() {
        return isWrapperStyle;
    }

    /**
     * A WSDL operation qualifies for wrapper style mapping only if the following criteria are met:
     * <p/>
     * (i) The operation’s input and output messages (if present) each contain only a single part
     * (ii) The input message part refers to a global element declaration whose localname is equal to the operation
     * name
     * (iii) The output message (if present) part refers to a global element declaration
     * (iv) The elements referred to by the input and output message (if present) parts (henceforth referred to as
     * wrapper elements) are both complex types defined using the xsd:sequence compositor
     * (v) The wrapper elements only contain child elements, they MUST not contain other structures such
     * as wildcards (element or attribute), xsd:choice, substitution groups (element references are not
     * permitted) or attributes; furthermore, they MUST not be nillable.
     * <p/>
     * Conformance (Default mapping mode): Operations that do not meet the criteria aboveMUST be mapped
     * using non-wrapper style.
     *
     * @return true if wrapper style, false otherwise
     */
    private boolean computeWrapperStyle() {
        //check binding customization, if it is disabled then we determine it is non-wraper style
        // or bare
        BindingInfo bi = wsdlOperation.getFirstExtension(BindingInfo.class);
        if (bi != null && !bi.isEnableWrapperStyle()) {
            return false;
        }

        //then check portType wrapper style binding
        bi = wsdlOperation.getPortType().getFirstExtension(BindingInfo.class);
        if (bi != null && !bi.isEnableWrapperStyle()) {
            return false;
        }

        //then global or wsdl:definition level
        bi = wsdlOperation.getOwnerWSDLModel().getFirstExtension(BindingInfo.class);
        if (bi != null && !bi.isEnableWrapperStyle()) {
            return false;
        }


        if (wsdlOperation.getInput().getParts().size() != 1 || wsdlOperation.getOutput().getParts().size() != 1)
            return false;

        WSDLPart inPart = wsdlOperation.getInput().getParts().iterator().next();
        WSDLPart outPart = wsdlOperation.getOutput() == null ? null : wsdlOperation.getOutput().getParts().iterator().next();
        WSDLPart.WSDLPartDescriptor desc = inPart.getDescriptor();

        if ((desc.type() != WSDLPart.WSDLPartDescriptor.Kind.ELEMENT) &&
                wsdlOperation.getName().getLocalPart().equals(inPart.getName()))
            return false;

        if (outPart != null && outPart.getDescriptor().type() != WSDLPart.WSDLPartDescriptor.Kind.ELEMENT)
            return false;

        WSDLBoundPortType binding = context.getWsdlSet().boundPortType(wsdlOperation.getPortType().getName());
        if (binding != null) {
            WSDLPart.Binding pb = binding.get(wsdlOperation.getName()).getInput().getPartBinding(inPart.getName().getLocalPart());
            if (!pb.isBody() || !pb.isUnknown())
                return false;
            WSDLBoundOutput bo = binding.get(wsdlOperation.getName()).getOutput();
            if (bo != null && outPart != null) {
                pb = bo.getPartBinding(outPart.getName().getLocalPart());
                if (!pb.isBody() || !pb.isUnknown())
                    return false;
            }
        }

        //TODO: check JAXB wrapper style determination
        return false;
    }

    private boolean isOneWay() {
        return outParts == null;
    }

    private void processParameterOrder() {
        List<String> paramOrder = wsdlOperation.getParameterOrder();
        if (paramOrder.isEmpty()) {
            for (WSDLPart part : inParts) {
                parameterList.add(part);
            }
            if (!isOneWay() && !outParts.isEmpty()) {
                if (outParts.size() == 1) {
                    returnType = outParts.get(0);
                } else {

                }
            }
        }
    }

    public <V, P> V accept(JavaVisitor<V, P> visitor, P param) {
        return visitor.operation(this, param);
    }

    public String name() {
        return jmethod.name();
    }

    public Collection<Parameter> parameters() {
        return null;
    }

    public String returnType() {
        return jmethod.type().fullName();
    }

    public WSDLOperation wsdlOperation() {
        return wsdlOperation;
    }

    public JMethod codeModel() {
        return jmethod;
    }
}
