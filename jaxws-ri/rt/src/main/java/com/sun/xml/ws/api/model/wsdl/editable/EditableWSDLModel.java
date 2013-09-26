/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.model.wsdl.editable;

import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.PolicyMap;

public interface EditableWSDLModel extends WSDLModel {

    @Override
    EditableWSDLPortType getPortType(@NotNull QName name);

    /**
     * Add Binding
     *
     * @param portType Bound port type
     */
    void addBinding(EditableWSDLBoundPortType portType);

    @Override
    EditableWSDLBoundPortType getBinding(@NotNull QName name);

    @Override
    EditableWSDLBoundPortType getBinding(@NotNull QName serviceName, @NotNull QName portName);

    @Override
    EditableWSDLService getService(@NotNull QName name);

    @Override
    @NotNull
    Map<QName, ? extends EditableWSDLMessage> getMessages();

    /**
     * Add message
     *
     * @param msg Message
     */
    public void addMessage(EditableWSDLMessage msg);

    @Override
    @NotNull
    Map<QName, ? extends EditableWSDLPortType> getPortTypes();

    /**
     * Add port type
     *
     * @param pt Port type
     */
    public void addPortType(EditableWSDLPortType pt);

    @Override
    @NotNull
    Map<QName, ? extends EditableWSDLBoundPortType> getBindings();

    @Override
    @NotNull
    Map<QName, ? extends EditableWSDLService> getServices();

    /**
     * Add service
     *
     * @param svc Service
     */
    public void addService(EditableWSDLService svc);

    @Override
    public EditableWSDLMessage getMessage(QName name);

    /**
     * @param policyMap
     * @deprecated
     */
    public void setPolicyMap(PolicyMap policyMap);

    /**
     * Finalize rpc-lit binding
     *
     * @param portType Binding
     */
    public void finalizeRpcLitBinding(EditableWSDLBoundPortType portType);

    /**
     * Freezes WSDL model to prevent further modification
     */
    public void freeze();

}
