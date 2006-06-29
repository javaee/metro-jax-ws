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

import com.sun.net.httpserver.HttpExchange;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.util.ByteArrayDataSource;

import static com.sun.xml.ws.handler.HandlerChainCaller.IGNORE_FAULT_PROPERTY;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.activation.DataHandler;


/**
 * Utility to manipulate MessageContext properties
 *
 * @author WS Development Team
 */
public class MessageContextUtil {
    
    public static Integer getHttpStatusCode(MessageContext ctxt) {
        return (Integer)ctxt.get(MessageContext.HTTP_RESPONSE_CODE);
    }
    
    public static void setHttpStatusCode(MessageContext ctxt, Integer code) {
        ctxt.put(MessageContext.HTTP_RESPONSE_CODE, code);
        ctxt.setScope(MessageContext.HTTP_RESPONSE_CODE, Scope.APPLICATION);
    }
    
    public static void setQueryString(MessageContext ctxt, String queryString) {
        ctxt.put(MessageContext.QUERY_STRING, queryString);
        ctxt.setScope(MessageContext.QUERY_STRING, Scope.APPLICATION);
    }
    
    public static void setPathInfo(MessageContext ctxt, String pathInfo) {
        ctxt.put(MessageContext.PATH_INFO, pathInfo);
        ctxt.setScope(MessageContext.PATH_INFO, Scope.APPLICATION);
    }
    
    public static void setHttpExchange(MessageContext ctxt, HttpExchange exch) {
        ctxt.put(JAXWSProperties.HTTP_EXCHANGE, exch);
        ctxt.setScope(JAXWSProperties.HTTP_EXCHANGE, Scope.APPLICATION);
    }
    
    public static HttpExchange getHttpExchange(MessageContext ctxt) {
        return (HttpExchange)ctxt.get(JAXWSProperties.HTTP_EXCHANGE);
    }
    
    public static void setHttpRequestMethod(MessageContext ctxt, String method) {
        ctxt.put(MessageContext.HTTP_REQUEST_METHOD, method);
        ctxt.setScope(MessageContext.HTTP_REQUEST_METHOD, Scope.APPLICATION);
    }
    
    public static void setHttpRequestHeaders(MessageContext ctxt,
            Map<String, List<String>> headers) {
        ctxt.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        ctxt.setScope(MessageContext.HTTP_REQUEST_HEADERS, Scope.APPLICATION);
    }
    
    public static void setHttpResponseHeaders(MessageContext ctxt,
            Map<String, List<String>> headers) {
        ctxt.put(MessageContext.HTTP_RESPONSE_HEADERS, headers);
        ctxt.setScope(MessageContext.HTTP_RESPONSE_HEADERS, Scope.APPLICATION);
    }
    
    public static Map<String, List<String>> getHttpResponseHeaders(MessageContext ctxt) {
        return (Map<String, List<String>>)ctxt.get(MessageContext.HTTP_RESPONSE_HEADERS);
    }
    
    public static void setWsdlOperation(MessageContext ctxt, QName name) {
        ctxt.put(MessageContext.WSDL_OPERATION, name);
        ctxt.setScope(MessageContext.WSDL_OPERATION, Scope.APPLICATION);
    }

    private static Map<String, DataHandler> getMessageAttachments(MessageContext ctxt) {
        String property = MessageContext.INBOUND_MESSAGE_ATTACHMENTS;
        Boolean out = (Boolean)ctxt.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (out != null && out) {
            property = MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS;
        }
            
        Object att = ctxt.get(property);
        if(att == null){
            Map<String, DataHandler> attMap = new HashMap<String, DataHandler>();
            ctxt.put(property, attMap);
            ctxt.setScope(property, Scope.APPLICATION);
            return attMap;
        }
        return (Map<String, DataHandler>)att;
    }

    public static void copyInboundMessageAttachments(MessageContext ctxt, Iterator<AttachmentPart> attachments) throws SOAPException {
        Map<String, DataHandler> attachMap = getMessageAttachments(ctxt);
        while(attachments.hasNext()){
            AttachmentPart ap = attachments.next();
            DataHandler dh = new DataHandler(new ByteArrayDataSource(ap.getRawContentBytes(), ap.getContentType()));
            attachMap.put(ap.getContentId(), dh);
        }
    }

    public static void addMessageAttachment(MessageContext ctxt, String cid, DataHandler dh){
        Map<String, DataHandler> attachMap = getMessageAttachments(ctxt);
        attachMap.put(cid, dh);
    }
    
    /*
     * See HandlerChainCaller for full details. When a ProtocolException
     * is caught from the handler chain, this method is used to tell
     * the runtime whether to use the fault in the current message or
     * use the exception and create a new message.
     */
    public static boolean ignoreFaultInMessage(MessageContext context) {
        if (context.get(IGNORE_FAULT_PROPERTY) == null) {
            return false;
        }
        return (Boolean) context.get(IGNORE_FAULT_PROPERTY);
    }
    
}
