/*
 * $Id: JAXBBridgeInfo.java,v 1.1 2005-05-23 22:28:40 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
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
