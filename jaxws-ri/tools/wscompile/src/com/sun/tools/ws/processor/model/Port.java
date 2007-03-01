/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.processor.model.java.JavaInterface;
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
}
