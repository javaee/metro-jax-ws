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

package com.sun.xml.ws.model.wsdl;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLFault;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLInput;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLMessage;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLModel;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLOperation;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLOutput;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLPart;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLPortType;
import com.sun.xml.ws.util.QNameMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementaiton of {@link WSDLOperation}
 *
 * @author Vivek Pandey
 */
public final class WSDLOperationImpl extends AbstractExtensibleImpl implements EditableWSDLOperation {
    private final QName name;
    private String parameterOrder;
    private EditableWSDLInput input;
    private EditableWSDLOutput output;
    private final List<EditableWSDLFault> faults;
    private final QNameMap<EditableWSDLFault> faultMap;
    protected Iterable<EditableWSDLMessage> messages;
    private final EditableWSDLPortType owner;

    public WSDLOperationImpl(XMLStreamReader xsr, EditableWSDLPortType owner, QName name) {
        super(xsr);
        this.name = name;
        this.faults = new ArrayList<EditableWSDLFault>();
        this.faultMap = new QNameMap<EditableWSDLFault>();
        this.owner = owner;
    }

    public QName getName() {
        return name;
    }

    public String getParameterOrder() {
        return parameterOrder;
    }

    public void setParameterOrder(String parameterOrder) {
        this.parameterOrder = parameterOrder;
    }

    public EditableWSDLInput getInput() {
        return input;
    }

    public void setInput(EditableWSDLInput input) {
        this.input = input;
    }

    public EditableWSDLOutput getOutput() {
        return output;
    }

    public boolean isOneWay() {
        return output == null;
    }

    public void setOutput(EditableWSDLOutput output) {
        this.output = output;
    }

    public Iterable<EditableWSDLFault> getFaults() {
        return faults;
    }

    public EditableWSDLFault getFault(QName faultDetailName) {
        EditableWSDLFault fault = faultMap.get(faultDetailName);
        if(fault != null)
            return fault;

        for(EditableWSDLFault fi : faults){
            assert fi.getMessage().parts().iterator().hasNext();
            EditableWSDLPart part = fi.getMessage().parts().iterator().next();
            if(part.getDescriptor().name().equals(faultDetailName)){
                faultMap.put(faultDetailName, fi);
                return fi;
            }
        }
        return null;
    }
    
    @NotNull
    public QName getPortTypeName() {
        return owner.getName();
    }

    public void addFault(EditableWSDLFault fault) {
        faults.add(fault);
    }

    public void freeze(EditableWSDLModel root) {
        assert input != null;
        input.freeze(root);
        if(output != null)
            output.freeze(root);
        for(EditableWSDLFault fault : faults){
            fault.freeze(root);
        }
    }
}
