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
package com.sun.xml.ws.handler;
import java.util.List;
import com.sun.xml.ws.spi.runtime.MessageContext;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;
import com.sun.xml.ws.spi.runtime.Invoker;
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
    
    /*
     * Returns the invocation method
     */
    public Method getMethod() {
        return handlerCtxt.getMessageInfo().getMethod();
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
        InternalMessage im = handlerCtxt.getInternalMessage();
        return (im == null)?null:im.getBody();
    }
    
    /*
     * Returns InternalMessage's HeaderBlock values
     */
    public List getHeaders() {
        InternalMessage im = handlerCtxt.getInternalMessage();
        return (im == null)?null:im.getHeaders();
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
        return (InternalSoapEncoder)handlerCtxt.getMessageInfo().getEncoder();
    }
    
    public String getBindingId(MessageContext ctxt) {
        return null;
    }
    
    public Method getMethod(MessageContext ctxt) {
        return null;
    }
    
    public void setCanonicalization(MessageContext ctxt, String algorithm) {
        
    }
    
    public Invoker getInvoker(MessageContext ctxt) {
        return null;
    }

}
    