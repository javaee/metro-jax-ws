/*
 * $Id: RpcLitPayload.java,v 1.1 2005-05-23 22:28:41 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.encoding.jaxb;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

// TODO : remove RpcLitParameter once we use only dynamic runtime

public class RpcLitPayload {
    private QName operation;
    private List<RpcLitParameter> parameters;
    private List<JAXBBridgeInfo> bridgeParameters;
    
    public RpcLitPayload(QName operation) {
    	this.operation = operation;
    	this.parameters = new ArrayList<RpcLitParameter>();
        this.bridgeParameters = new ArrayList<JAXBBridgeInfo>();
    }
    
    public RpcLitPayload(QName operation, List<RpcLitParameter> parameters) {
        this.operation = operation;
        this.parameters = parameters;
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
    
    public List<RpcLitParameter> getParameters() {
        return parameters;
    }
    
    public List<JAXBBridgeInfo> getBridgeParameters() {
        return bridgeParameters;
    }
    
    public static RpcLitPayload copy(RpcLitPayload payload) {
        RpcLitPayload newPayload = new RpcLitPayload(payload.getOperation());
        
        for(RpcLitParameter param: payload.getParameters()) {
            RpcLitParameter newParam = new RpcLitParameter(param.getName(),
                    param.getType());
            newPayload.addParameter(newParam);
        }
        
        for(JAXBBridgeInfo param: payload.getBridgeParameters()) {
            JAXBBridgeInfo newParam = JAXBBridgeInfo.copy(param);
            newPayload.addParameter(newParam);
        }
        return newPayload;
    }
    
    public RpcLitParameter getParameterByName(String name){
    	for(RpcLitParameter param : parameters) {
    		if (param.getName().getLocalPart().equals(name)) {
    			return param;
            }
    	}
    	return null;
    }
    
    public JAXBBridgeInfo getBridgeParameterByName(String name){
    	for(JAXBBridgeInfo param : bridgeParameters) {
    		if (param.getName().getLocalPart().equals(name)) {
    			return param;
            }
    	}
    	return null;
    }
    
    public void addParameter(RpcLitParameter parameter) {
    	parameters.add(parameter);    	
    }
    
    public void addParameter(JAXBBridgeInfo parameter) {
    	bridgeParameters.add(parameter);    	
    }
}
