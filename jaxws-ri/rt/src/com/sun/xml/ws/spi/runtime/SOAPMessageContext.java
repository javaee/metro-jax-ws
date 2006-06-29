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
package com.sun.xml.ws.spi.runtime;
import java.util.List;

/**
 * This enhances API's SOAPMessageContext and provides internal representation
 * of SOAPMessage so that it can be encoded optimally
 */
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
