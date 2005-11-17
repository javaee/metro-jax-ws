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
