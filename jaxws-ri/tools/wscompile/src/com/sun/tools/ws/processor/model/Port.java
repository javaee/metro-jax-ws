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

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.wsdl.document.PortType;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.wsdl.framework.Entity;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WS Development Team
 */
public class Port extends ModelObject {

    public Port(Entity entity) {
        super(entity);
    }

    public Port(QName name, Entity entity) {
        super(entity);
        _name = name;
    }

    public QName getName() {
        return _name;
    }

    public void setName(QName n) {
        _name = n;
    }

    public void addOperation(Operation operation) {
        _operations.add(operation);
        operationsByName.put(operation.getUniqueName(), operation);
    }

    public Operation getOperationByUniqueName(String name) {
        if (operationsByName.size() != _operations.size()) {
            initializeOperationsByName();
        }
        return operationsByName.get(name);
    }

    private void initializeOperationsByName() {
        operationsByName = new HashMap<String, Operation>();
        if (_operations != null) {
            for (Operation operation : _operations) {
                if (operation.getUniqueName() != null &&
                        operationsByName.containsKey(operation.getUniqueName())) {

                    throw new ModelException("model.uniqueness");
                }
                operationsByName.put(operation.getUniqueName(), operation);
            }
        }
    }

    /* serialization */
    public List<Operation> getOperations() {
        return _operations;
    }

    /* serialization */
    public void setOperations(List<Operation> l) {
        _operations = l;
    }

    public JavaInterface getJavaInterface() {
        return _javaInterface;
    }

    public void setJavaInterface(JavaInterface i) {
        _javaInterface = i;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String s) {
        _address = s;
    }

    public String getServiceImplName() {
        return _serviceImplName;
    }

    public void setServiceImplName(String name) {
        _serviceImplName = name;
    }

    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    public boolean isProvider() {
        JavaInterface intf = getJavaInterface();
        if (intf != null) {
            String sei = intf.getName();
            if (sei.equals(javax.xml.ws.Provider.class.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * XYZ_Service.getABC() method name
     *
     * @return Returns the portGetterName.
     */
    public String getPortGetter() {
        return portGetter;
    }

    /**
     * @param portGetterName The portGetterName to set.
     */
    public void setPortGetter(String portGetterName) {
        this.portGetter = portGetterName;
    }

    public SOAPStyle getStyle() {
        return _style;
    }

    public void setStyle(SOAPStyle s) {
        _style = s;
    }

    public boolean isWrapped() {
        return _isWrapped;
    }

    public void setWrapped(boolean isWrapped) {
        _isWrapped = isWrapped;
    }

    private SOAPStyle _style = null;
    private boolean _isWrapped = true;

    private String portGetter;
    private QName _name;
    private List<Operation> _operations = new ArrayList<Operation>();
    private JavaInterface _javaInterface;
    private String _address;
    private String _serviceImplName;
    private Map<String, Operation> operationsByName = new HashMap<String, Operation>();
    public Map<QName, PortType> portTypes = new HashMap<QName, PortType>();
}
