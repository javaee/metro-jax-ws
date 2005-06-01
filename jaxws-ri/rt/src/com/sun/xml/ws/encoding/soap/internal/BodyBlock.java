/*
 * $Id: BodyBlock.java,v 1.2 2005-06-01 00:51:34 jitu Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/
package com.sun.xml.ws.encoding.soap.internal;

import javax.xml.transform.Source;

import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;

/**
 * @author JAX-RPC RI Development Team
 */
public class BodyBlock {
     
    private Object value;
    
    public BodyBlock(Object value) {
        this.value = value;
    }
       
    public BodyBlock(JAXBBeanInfo beanInfo) {
        this.value = beanInfo;
    }
    
    public BodyBlock(JAXBBridgeInfo bridgeInfo) {
        this.value = bridgeInfo;
    }
    
    public BodyBlock(Source source) {
    	setSource(source);
    }
    
    public BodyBlock(SOAPFaultInfo faultInfo) {
    	setFaultInfo(faultInfo);
    }
    
    public BodyBlock(RpcLitPayload rpcLoad) {
        this.value = rpcLoad;
    }
    
    public void setSource(Source source) {
        this.value = source;
    }
    
    public void setFaultInfo(SOAPFaultInfo faultInfo) {
        this.value = faultInfo;
    }
    
    public Object getValue() {
        return value;
    }
	 
}
