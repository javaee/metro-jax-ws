/**
 * $Id: SOAPMessageContext.java,v 1.4 2005-08-26 23:40:08 vivekp Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;
import com.sun.pept.ept.MessageInfo;
import java.util.List;

public interface SOAPMessageContext
    extends javax.xml.ws.handler.soap.SOAPMessageContext, MessageContext {
    
    /**
     * If there is a SOAPMessage already, use getSOAPMessage(). Ignore all other methods
     * @return
     */
    public boolean isAlreadySoap();
    
    /**
     * Returns InternalMessage's BodyBlock value
     * @return
     */
    public Object getBody();

    /**
     * Returns InternalMessage's HeaderBlock values
     * @return
     */
    public List getHeaders();

    /**
     * Use this object to pass to InternalSoapEncoder write methods
     * @return object containg information thats used by InternalEncoderDecoder write methods.
     *
     */
    public Object getMessageInfo();
    
    /**
     * Returns to marshall all JAXWS objects: RpcLitPayload, JAXBBridgeInfo etc
     * @return
     */
    public InternalSoapEncoder getEncoder();
    
}
