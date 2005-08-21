/*
 * $Id: RpcLitPayload.java,v 1.3 2005-08-21 19:30:00 vivekp Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.encoding.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

public class RpcLitPayload {
    private QName operation;
    private List<JAXBBridgeInfo> bridgeParameters;
    
    public RpcLitPayload(QName operation) {
    	this.operation = operation;
        this.bridgeParameters = new ArrayList<JAXBBridgeInfo>();
    }
    
    /* Same as the above one. Need to remove the above constructor.
    public RpcLitPayload(QName operation, List<JAXBBridgeInfo> parameters) {
        this.operation = operation;
        this.bridgeParameters = parameters;
    }
     */
    
    public QName getOperation() {
        return operation;
    }
    
    public List<JAXBBridgeInfo> getBridgeParameters() {
        return bridgeParameters;
    }       

    public static RpcLitPayload copy(RpcLitPayload payload) {
        RpcLitPayload newPayload = new RpcLitPayload(payload.getOperation());
        for(JAXBBridgeInfo param: payload.getBridgeParameters()) {
            JAXBBridgeInfo newParam = JAXBBridgeInfo.copy(param);
            newPayload.addParameter(newParam);
        }
        return newPayload;
    }
    
    public JAXBBridgeInfo getBridgeParameterByName(String name){
    	for(JAXBBridgeInfo param : bridgeParameters) {
    		if (param.getName().getLocalPart().equals(name)) {
    			return param;
            }
    	}
    	return null;
    }
    
    public void addParameter(JAXBBridgeInfo parameter) {
    	bridgeParameters.add(parameter);    	
    }
}
