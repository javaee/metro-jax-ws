/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.handler;
import java.util.List;
import com.sun.xml.ws.spi.runtime.MessageContext;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.SOAPEPTFactory;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;
import com.sun.xml.ws.spi.runtime.Invoker;
import com.sun.xml.ws.util.MessageInfoUtil;

import java.lang.reflect.Method;

/**
 * Implementation of SOAPMessageContext. This class is used at runtime
 * to pass to the handlers for processing soap messages.
 *
 * @see MessageContextImpl
 *
 * @author WS Development Team
 */
public class SHDSOAPMessageContext extends SOAPMessageContextImpl implements com.sun.xml.ws.spi.runtime.SOAPMessageContext {

    SOAPHandlerContext handlerCtxt;
    
    public SHDSOAPMessageContext(SOAPHandlerContext handlerCtxt) {
        super(handlerCtxt);
        this.handlerCtxt = handlerCtxt;
    }
    
    /**
     * If there is a SOAPMessage already, use getSOAPMessage(). Ignore all other
     * methods
     */
    public boolean isAlreadySoap() {
        return handlerCtxt.getSOAPMessage() != null;
    }
    
    /*
     * Returns InternalMessage's BodyBlock value
     */
    public Object getBody() {
        return handlerCtxt.getBody();
    }
    
    /*
     * Returns InternalMessage's HeaderBlock values
     */
    public List getHeaders() {
        return handlerCtxt.getHeaders();
    }
    
    /*
     * Use this MessageInfo to pass to InternalSoapEncoder write methods
     */
    public Object getMessageInfo() {
        return handlerCtxt.getMessageInfo();
    }
    
    /*
     * Encoder to marshall all JAXWS objects: RpcLitPayload, JAXBBridgeInfo etc
     */
    public InternalSoapEncoder getEncoder() {
        return (InternalSoapEncoder)((SOAPEPTFactory)handlerCtxt.getMessageInfo().getEPTFactory()).getSOAPEncoder();
    }
    
    public String getBindingId() {
        return handlerCtxt.getBindingId();
    }
    
    public Method getMethod() {
        return handlerCtxt.getMethod();
    }
    
    public void setCanonicalization(String algorithm) {
        handlerCtxt.setCanonicalization(algorithm);
    }
    
    public Invoker getInvoker() {
        return handlerCtxt.getInvoker();
    }

    /**
     * Returns if MTOM is anbled
     *
     * @return true if MTOM is enabled otherwise returns false;
     */
    public boolean isMtomEnabled() {
        JAXWSAttachmentMarshaller am = MessageInfoUtil.getAttachmentMarshaller(handlerCtxt.getMessageInfo());
        return (am != null)?am.isXOPPackage():false;
    }

}
    