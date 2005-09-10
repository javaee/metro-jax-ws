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

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.TypeReference;

import javax.xml.namespace.QName;

public class JAXBBridgeInfo {
    private Bridge bridge;
    private Object value;
    
    public JAXBBridgeInfo(Bridge bridge) {
        this.bridge = bridge;
    }
    
    public JAXBBridgeInfo(Bridge bridge, Object value) {
        this(bridge);
        this.value = value;
    }
    
    public QName getName() {
        return bridge.getTypeReference().tagName;
    }
    
    public TypeReference getType(){
        return bridge.getTypeReference();
    }
    
    public Bridge getBridge() {
        return bridge;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public static JAXBBridgeInfo copy(JAXBBridgeInfo payload) {
        return new JAXBBridgeInfo(payload.getBridge(), payload.getValue());
    }
    
}
