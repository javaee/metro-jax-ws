/**
 * $Id: SOAPMessageContext.java,v 1.3 2005-08-24 03:24:49 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;
import com.sun.pept.ept.MessageInfo;
import java.util.List;

/**
 * This class is implemented by
 * com.sun.xml.rpc.soap.message.SOAPMessageContext
 */
public interface SOAPMessageContext
    extends javax.xml.ws.handler.soap.SOAPMessageContext, MessageContext {
    
    /**
     * If there is a SOAPMessage already, use getSOAPMessage(). Ignore all other
     * methods
     */
    public boolean isAlreadySoap();
    
    /*
     * Returns InternalMessage's BodyBlock value
     */
    public Object getBody();
    
    /*
     * Returns InternalMessage's HeaderBlock values
     */
    public List getHeaders();
    
    /*
     * Use this MessageInfo to pass to InternalSoapEncoder write methods
     */
    public MessageInfo getMessageInfo();
    
    /*
     * Encoder to marshall all JAXWS objects: RpcLitPayload, JAXBBridgeInfo etc
     */
    public InternalSoapEncoder getEncoder();
    
}
