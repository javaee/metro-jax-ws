/*
 * $Id: PortInfo.java,v 1.1 2005-05-23 22:26:36 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * @author JAX-RPC Development Team
 */
package com.sun.xml.ws.client;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class PortInfo {
    //Map<String, OperationInfo> operationMap;
    //String targetEndpoint;
    //String defaultNamespace;
    //QName name;
    //QName portTypeName;
    //java.net.URI bindingId;

    //stores port information from the examined
    //wsdl - stores a map of operations found in the
    //wsdl
    public PortInfo(QName name) {
        //super(name);
        //operationMap = null;//new HashMap<String, OperationInfo>();
    }

    /*public OperationInfo createOperationForName(String operationName) {
       // OperationInfo operation =
      //          operationMap.get(operationName);
        if (operation == null) {
            operation = null;//new OperationInfo(operationName);
            operation.setNamespace(defaultNamespace);
            operationMap.put(operationName, operation);
        }
        return operation;
    }
    */
    public boolean isOperationKnown(String operationName) {
        //return operationMap.get(operationName) != null;
        return false;
    }

    public Iterator<Object> getOperations() {
        //public Iterator<OperationInfo> getOperations() {
        // return operationMap.values().iterator();
        return null;
    }

    public int getOperationCount() {
        return 0;
        //return operationMap.values().size();
    }
}
