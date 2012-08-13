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

package com.sun.xml.ws.model.wsdl;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.util.QNameMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementaiton of {@link WSDLOperation}
 *
 * @author Vivek Pandey
 */
public final class WSDLOperationImpl extends AbstractExtensibleImpl implements WSDLOperation {
    private final QName name;
    private String parameterOrder;
    private WSDLInputImpl input;
    private WSDLOutputImpl output;
    private final List<WSDLFaultImpl> faults;
    private final QNameMap<WSDLFaultImpl> faultMap;
    protected Iterable<WSDLMessageImpl> messages;
    private final WSDLPortType owner;

    public WSDLOperationImpl(XMLStreamReader xsr,WSDLPortTypeImpl owner, QName name) {
        super(xsr);
        this.name = name;
        this.faults = new ArrayList<WSDLFaultImpl>();
        this.faultMap = new QNameMap<WSDLFaultImpl>();
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

    public WSDLInputImpl getInput() {
        return input;
    }

    public void setInput(WSDLInputImpl input) {
        this.input = input;
    }

    public WSDLOutputImpl getOutput() {
        return output;
    }

    public boolean isOneWay() {
        return output == null;
    }

    public void setOutput(WSDLOutputImpl output) {
        this.output = output;
    }

    public Iterable<WSDLFaultImpl> getFaults() {
        return faults;
    }

    public WSDLFault getFault(QName faultDetailName) {
        WSDLFaultImpl fault = faultMap.get(faultDetailName);
        if(fault != null)
            return fault;

        for(WSDLFaultImpl fi:faults){
            assert fi.getMessage().parts().iterator().hasNext();
            WSDLPartImpl part = fi.getMessage().parts().iterator().next();
            if(part.getDescriptor().name().equals(faultDetailName)){
                faultMap.put(faultDetailName, fi);
                return fi;
            }
        }
        return null;
    }
    
    WSDLPortType getOwner() {
        return owner;
    }

    @NotNull
    public QName getPortTypeName() {
        return owner.getName();
    }

    public void addFault(WSDLFaultImpl fault) {
        faults.add(fault);
    }

    public void freez(WSDLModelImpl root) {
        assert input != null;
        input.freeze(root);
        if(output != null)
            output.freeze(root);
        for(WSDLFaultImpl fault : faults){
            fault.freeze(root);
        }
    }
}
