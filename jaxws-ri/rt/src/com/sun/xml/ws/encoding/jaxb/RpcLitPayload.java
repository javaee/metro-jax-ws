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
