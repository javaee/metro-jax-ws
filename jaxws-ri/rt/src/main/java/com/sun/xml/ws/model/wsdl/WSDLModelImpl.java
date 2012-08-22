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

package com.sun.xml.ws.model.wsdl;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.policy.PolicyMap;

import javax.jws.WebParam.Mode;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link WSDLModel}
 *
 * @author Vivek Pandey
 */
public final class WSDLModelImpl extends AbstractExtensibleImpl implements WSDLModel {
    private final Map<QName, WSDLMessageImpl> messages = new HashMap<QName, WSDLMessageImpl>();
    private final Map<QName, WSDLPortTypeImpl> portTypes = new HashMap<QName, WSDLPortTypeImpl>();
    private final Map<QName, WSDLBoundPortTypeImpl> bindings = new HashMap<QName, WSDLBoundPortTypeImpl>();
    private final Map<QName, WSDLServiceImpl> services = new LinkedHashMap<QName, WSDLServiceImpl>();

    private PolicyMap policyMap;
    private final Map<QName,WSDLBoundPortType> unmBindings
        = Collections.<QName,WSDLBoundPortType>unmodifiableMap(bindings);


    public WSDLModelImpl(@NotNull String systemId) {
        super(systemId,-1);
    }

    /**
     * To create {@link WSDLModelImpl} from WSDL that doesn't have a system ID.
     */
    public WSDLModelImpl() {
        super(null,-1);
    }

    public void addMessage(WSDLMessageImpl msg){
        messages.put(msg.getName(), msg);
    }

    public WSDLMessageImpl getMessage(QName name){
        return messages.get(name);
    }

    public void addPortType(WSDLPortTypeImpl pt){
        portTypes.put(pt.getName(), pt);
    }

    public WSDLPortTypeImpl getPortType(QName name){
        return portTypes.get(name);
    }

    public void addBinding(WSDLBoundPortTypeImpl boundPortType){
        assert !bindings.containsValue(boundPortType);
        bindings.put(boundPortType.getName(), boundPortType);
    }

    public WSDLBoundPortTypeImpl getBinding(QName name){
        return bindings.get(name);
    }

    public void addService(WSDLServiceImpl svc){
        services.put(svc.getName(), svc);
    }

    public WSDLServiceImpl getService(QName name){
        return services.get(name);
    }

    public Map<QName, WSDLMessageImpl> getMessages() {
        return messages;
    }

    public @NotNull Map<QName, WSDLPortTypeImpl> getPortTypes() {
        return portTypes;
    }

    public @NotNull Map<QName, WSDLBoundPortType> getBindings() {
        return unmBindings;
    }

    public @NotNull Map<QName, WSDLServiceImpl> getServices(){
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
     * Returns first port QName from first service as per the insertion order
     */
    public QName getFirstPortName(){
        WSDLPort fp = getFirstPort();
        if(fp==null)
            return null;
        else
            return fp.getName();
    }

    private WSDLPort getFirstPort(){
        if(services.isEmpty())
            return null;
        WSDLService service = services.values().iterator().next();
        Iterator<? extends WSDLPort> iter = service.getPorts().iterator();
        WSDLPort port = iter.hasNext()?iter.next():null;
        return port;
    }
    
    /**
    * gets the first port in the wsdl which matches the serviceName and portType
    */
    public WSDLPortImpl getMatchingPort(QName serviceName, QName portType){
        return getService(serviceName).getMatchingPort(portType);
    }

    /**
     *
     * @param serviceName non-null service QName
     * @param portName    non-null port QName
     * @return
     *          WSDLBoundOperation on success otherwise null. throws NPE if any of the parameters null
     */
    public WSDLBoundPortTypeImpl getBinding(QName serviceName, QName portName){
        WSDLServiceImpl service = services.get(serviceName);
        if(service != null){
            WSDLPortImpl port = service.get(portName);
            if(port != null)
                return port.getBinding();
        }
        return null;
    }

    void finalizeRpcLitBinding(WSDLBoundPortTypeImpl boundPortType){
        assert(boundPortType != null);
        QName portTypeName = boundPortType.getPortTypeName();
        if(portTypeName == null)
            return;
        WSDLPortType pt = portTypes.get(portTypeName);
        if(pt == null)
            return;
        for (WSDLBoundOperationImpl bop : boundPortType.getBindingOperations()) {
            WSDLOperation pto = pt.get(bop.getName().getLocalPart());
            WSDLMessage inMsgName = pto.getInput().getMessage();
            if(inMsgName == null)
                continue;
            WSDLMessageImpl inMsg = messages.get(inMsgName.getName());
            int bodyindex = 0;
            if(inMsg != null){
                for(WSDLPartImpl part:inMsg.parts()){
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
            WSDLMessageImpl outMsg = messages.get(outMsgName.getName());
            if(outMsg!= null){
                for(WSDLPartImpl part:outMsg.parts()){
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
        for (WSDLServiceImpl service : services.values()) {
            service.freeze(this);
        }
        for (WSDLBoundPortTypeImpl bp : bindings.values()) {
            bp.freeze();
        }
        // Enforce freeze all the portTypes referenced by this endpoints, see Bug8966673 for detail
        for (WSDLPortTypeImpl pt : portTypes.values()) {
            pt.freeze();
        }        
    }
}
