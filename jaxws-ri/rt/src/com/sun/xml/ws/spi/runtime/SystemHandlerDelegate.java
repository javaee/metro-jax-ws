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
package com.sun.xml.ws.spi.runtime;


/**
 * A SystemHandlerDelegate is used to inject system level functionality into a 
 * message processing runtime. The methods of this interface are invoked by 
 * the client and enpoint message dispatchers of the message processing
 * runtime.
 *
 * @author WS Development Team
 */

public interface SystemHandlerDelegate {

   /**
    * Called by both client and endpoint message dispatchers to activate
    * injected request message processing.
    * When called by a client side message dispatcher, this method must be 
    * called just before the message (associated with the MessageContext) 
    * is sent. When called by the message dispatcher at an endpoint, this 
    * method must be called before MustUnderstand processing on the 
    * associated message.
    *
    * @param messageContext when called by a SOAPBinding the argument
    * must be an instanceof com.sun.xml.ws.spi.runtime.SOAPMessageContext, and
    * when called by a SOAPBinding at an endpoint, the argument must
    * be an instanceof com.sun.xml.ws.spi.runtime.SOAPMessageContext and the 
    * Invoker (on the context) must be available for use on the server by the 
    * delegate. An argument SOAPMessageContext passed to this method by an endpoint
    * dispatcher, must have values assigned for the following MessageContext 
    * properties.
    * <ul>
    * <li>MessageContext.SERVLET_REQUEST
    * <li>MessageContext.SERVLET_RESPONSE
    * <li>MessageContext.SERVLET_SESSION
    * <li>MessageContext.SERVLET_CONTEXT
    * </ul>
    * @return true if processing by the delegate was such that the caller
    * should continue with its normal message processing. Returns false when 
    * the delegate has established, in the MessageContext, 
    * the response message to be sent. When this method returns 
    * false, the calling message dispatcher must return the response message 
    * without performing MustUnderstand processing and without invoking the 
    * endpoint. Only delegates called by endpoint side message dispatchers 
    * may return false
    *
    * @throws java.lang.Exception when the processing by the delegate failed
    * without yielding a response message; in which case, the caller shall
    * determine how to process the error.
    *
    */
    public boolean processRequest(MessageContext messageContext) throws Exception;

   /**
    * Called by both client and endpoint message dispatchers to activate
    * injected response message processing.
    * When called by the message dispatcher at the client, this method must be 
    * called before MustUnderstand processing on the received message 
    * (associated with the MessageContext). When called by the message 
    * dispatcher at an endpoint, this method must be called after the 
    * endpoint has been invoked, and just before the associated response 
    * message is sent. In the special case where invocation of the endpoint
    * caused an Exception to be thrown, this method must not be called.
    *
    * @param messageContext when called by a SOAPBinding the argument
    * must be an instanceof com.sun.xml.ws.spi.runtime.SOAPMessageContext.
    *
    * @throws java.lang.Exception when the processing by the delegate failed.
    * In this case, the caller must not send the response message but shall 
    * otherwise determine how to process the error.
    */
    public void processResponse(MessageContext messageContext) throws Exception;
    
   /**
    * This method must be called by an endpoint message dispatcher after 
    * MustUnderstand processing and before endpoint invocation.
    *
    * @param messageContext when called by a SOAPBinding the argument
    * must be an instanceof com.sun.xml.ws.spi.runtime.SOAPMessageContext, and
    * must have values assigned for the following MessageContext 
    * properties.
    * <ul>
    * <li>MessageContext.SERVLET_REQUEST
    * <li>MessageContext.SERVLET_RESPONSE
    * <li>MessageContext.SERVLET_SESSION
    * <li>MessageContext.SERVLET_CONTEXT
    * </ul>
     */
    public void preInvokeEndpointHook(MessageContext messageContext);
}
