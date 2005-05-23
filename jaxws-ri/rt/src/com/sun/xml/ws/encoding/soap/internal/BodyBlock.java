/*
 * $Id: BodyBlock.java,v 1.1 2005-05-23 22:30:16 bbissett Exp $
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
public class BodyBlock extends MessageBlock {
    
    // TODO remove unnecessary constructors
    // TODO cleanup
    
    public BodyBlock(Object beanInfo) {
        this._name = null;
        this._value = beanInfo;
    }
    
    public BodyBlock(JAXBBeanInfo beanInfo) {
        this._name = null;
        this._value = beanInfo;
    }
    
    public BodyBlock(JAXBBridgeInfo bridgeInfo) {
        this._name = null;
        this._value = bridgeInfo;
    }
    
    public BodyBlock(Source source) {
    	setSource(source);
    }
    
    public BodyBlock(SOAPFaultInfo faultInfo) {
    	setFaultInfo(faultInfo);
    }
    
    public BodyBlock(RpcLitPayload rpcLoad) {
        this._name = rpcLoad.getOperation();
        this._value = rpcLoad;
    }
    
    /*
     * @deprecated 
     */
    public void setSource(Source source) {
        this._value = source;
        this._name = null;
    }
    
    /*
     * @deprecated 
     */
    public void setFaultInfo(SOAPFaultInfo faultInfo) {
        this._name = SOAPConstants.QNAME_SOAP_FAULT;
        this._value = faultInfo;
    }
	
    /*
     * @deprecated 
     */
	public BodyBlock() {
		super();
	}
   
}
