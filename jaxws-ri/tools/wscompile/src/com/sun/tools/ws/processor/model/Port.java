/*
 * $Id: Port.java,v 1.6 2005-09-10 19:49:38 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.ws.processor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.config.HandlerChainInfo;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.wsdl.document.soap.SOAPUse;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.wsdl.document.soap.SOAPUse;

/**
 *
 * @author WS Development Team
 */
public class Port extends ModelObject {

    public Port() {}

    public Port(QName name) {
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
        return (Operation)operationsByName.get(name);
    }

    private void initializeOperationsByName() {
        operationsByName = new HashMap();
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

    public HandlerChainInfo getClientHandlerChainInfo() {
        if (_clientHandlerChainInfo == null) {
            _clientHandlerChainInfo  = new HandlerChainInfo();
        }
        return _clientHandlerChainInfo;
    }

    public void setClientHandlerChainInfo(HandlerChainInfo i) {
        _clientHandlerChainInfo = i;
    }

    public HandlerChainInfo getServerHandlerChainInfo() {
        if (_serverHandlerChainInfo == null) {
            _serverHandlerChainInfo  = new HandlerChainInfo();
        }
        return _serverHandlerChainInfo;
    }

    public void setServerHandlerChainInfo(HandlerChainInfo i) {
        _serverHandlerChainInfo = i;
    }

    public SOAPVersion getSOAPVersion() {
        return _soapVersion;
    }

    public void setSOAPVersion(SOAPVersion soapVersion) {
        _soapVersion = soapVersion;
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
    private List<Operation> _operations = new ArrayList();
    private JavaInterface _javaInterface;
    private String _address;
    private String _serviceImplName;
    private Map operationsByName = new HashMap();
    private HandlerChainInfo _clientHandlerChainInfo;
    private HandlerChainInfo _serverHandlerChainInfo;
    private SOAPVersion _soapVersion = SOAPVersion.SOAP_11;
}
