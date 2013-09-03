/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.model.wsdl;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLMessage;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLModel;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLPart;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLPort;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLPortType;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLService;
import com.sun.xml.ws.policy.PolicyMap;

import javax.jws.WebParam.Mode;
import javax.xml.namespace.QName;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link WSDLModel}
 *
 * @author Vivek Pandey
 */
public final class WSDLModelImpl extends AbstractExtensibleImpl implements EditableWSDLModel {
    private final Map<QName, EditableWSDLMessage> messages = new HashMap<QName, EditableWSDLMessage>();
    private final Map<QName, EditableWSDLPortType> portTypes = new HashMap<QName, EditableWSDLPortType>();
    private final Map<QName, EditableWSDLBoundPortType> bindings = new HashMap<QName, EditableWSDLBoundPortType>();
    private final Map<QName, EditableWSDLService> services = new LinkedHashMap<QName, EditableWSDLService>();

    private PolicyMap policyMap;
    private final Map<QName, EditableWSDLBoundPortType> unmBindings
        = Collections.<QName, EditableWSDLBoundPortType>unmodifiableMap(bindings);


    public WSDLModelImpl(@NotNull String systemId) {
        super(systemId,-1);
    }

    /**
     * To create {@link WSDLModelImpl} from WSDL that doesn't have a system ID.
     */
    public WSDLModelImpl() {
        super(null,-1);
    }

    public void addMessage(EditableWSDLMessage msg){
        messages.put(msg.getName(), msg);
    }

    public EditableWSDLMessage getMessage(QName name){
        return messages.get(name);
    }

    public void addPortType(EditableWSDLPortType pt){
        portTypes.put(pt.getName(), pt);
    }

    public EditableWSDLPortType getPortType(QName name){
        return portTypes.get(name);
    }

    public void addBinding(EditableWSDLBoundPortType boundPortType){
        assert !bindings.containsValue(boundPortType);
        bindings.put(boundPortType.getName(), boundPortType);
    }

    public EditableWSDLBoundPortType getBinding(QName name){
        return bindings.get(name);
    }

    public void addService(EditableWSDLService svc){
        services.put(svc.getName(), svc);
    }

    public EditableWSDLService getService(QName name){
        return services.get(name);
    }

    public Map<QName, EditableWSDLMessage> getMessages() {
        return messages;
    }

    public @NotNull Map<QName, EditableWSDLPortType> getPortTypes() {
        return portTypes;
    }

    public @NotNull Map<QName, ? extends EditableWSDLBoundPortType> getBindings() {
        return unmBindings;
    }

    public @NotNull Map<QName, EditableWSDLService> getServices(){
        return services;
    }

    /**
     * Returns the first service QName from insertion order
     */
    public QName getFirstServiceName(){
        if(services.isEmpty())
            return null;
        return services.values().iterator().next().getName();
    }

    /**
     *
     * @param serviceName non-null service QName
     * @param portName    non-null port QName
     * @return
     *          WSDLBoundOperation on success otherwise null. throws NPE if any of the parameters null
     */
    public EditableWSDLBoundPortType getBinding(QName serviceName, QName portName){
        EditableWSDLService service = services.get(serviceName);
        if(service != null){
            EditableWSDLPort port = service.get(portName);
            if(port != null)
                return port.getBinding();
        }
        return null;
    }

    public void finalizeRpcLitBinding(EditableWSDLBoundPortType boundPortType){
        assert(boundPortType != null);
        QName portTypeName = boundPortType.getPortTypeName();
        if(portTypeName == null)
            return;
        WSDLPortType pt = portTypes.get(portTypeName);
        if(pt == null)
            return;
        for (EditableWSDLBoundOperation bop : boundPortType.getBindingOperations()) {
            WSDLOperation pto = pt.get(bop.getName().getLocalPart());
            WSDLMessage inMsgName = pto.getInput().getMessage();
            if(inMsgName == null)
                continue;
            EditableWSDLMessage inMsg = messages.get(inMsgName.getName());
            int bodyindex = 0;
            if(inMsg != null){
                for(EditableWSDLPart part:inMsg.parts()){
                    String name = part.getName();
                    ParameterBinding pb = bop.getInputBinding(name);
                    if(pb.isBody()){
                        part.setIndex(bodyindex++);
                        part.setBinding(pb);
                        bop.addPart(part, Mode.IN);
                    }
                }
            }
            bodyindex=0;
            if(pto.isOneWay())
                continue;
            WSDLMessage outMsgName = pto.getOutput().getMessage();
            if(outMsgName == null)
                continue;
            EditableWSDLMessage outMsg = messages.get(outMsgName.getName());
            if(outMsg!= null){
                for(EditableWSDLPart part:outMsg.parts()){
                    String name = part.getName();
                    ParameterBinding pb = bop.getOutputBinding(name);
                    if(pb.isBody()){
                        part.setIndex(bodyindex++);
                        part.setBinding(pb);
                        bop.addPart(part, Mode.OUT);
                    }
                }
            }
        }
    }

    /**
     * Gives the PolicyMap associated with the WSDLModel
     *
     * @return PolicyMap
     */
    public PolicyMap getPolicyMap() {
        return policyMap;
    }

    /**
     * Set PolicyMap for the WSDLModel.
     * @param policyMap
     */
    public void setPolicyMap(PolicyMap policyMap) {
        this.policyMap = policyMap;
    }
    
    /**
     * Invoked at the end of the model construction to fix up references, etc.
     */
    public void freeze() {
        for (EditableWSDLService service : services.values()) {
            service.freeze(this);
        }
        for (EditableWSDLBoundPortType bp : bindings.values()) {
            bp.freeze();
        }
        // Enforce freeze all the portTypes referenced by this endpoints, see Bug8966673 for detail
        for (EditableWSDLPortType pt : portTypes.values()) {
            pt.freeze();
        }        
    }
}
