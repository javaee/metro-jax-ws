/*
 * $Id: BodyBlock.java,v 1.4 2005-07-18 16:52:15 kohlert Exp $
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
import com.sun.xml.ws.encoding.soap.message.SOAP12FaultInfo;

/**
 * @author WS Development Team
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

    public BodyBlock(SOAP12FaultInfo faultInfo) {
    	value = faultInfo;
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

    /**
     * There is no need to have so many setter to set to an Object. Just setValue is all that we need?
     * @param value
     */
    public void setValue(Object value){
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
	 
}
