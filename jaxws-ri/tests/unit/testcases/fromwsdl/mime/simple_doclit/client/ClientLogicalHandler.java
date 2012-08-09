/*
 * $Id: ClientLogicalHandler.java,v 1.1 2005/12/02 19:50:38 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package fromwsdl.mime.simple_doclit.client;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.util.Set;
import java.util.Map;

import javax.xml.namespace.QName;

import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;

import javax.activation.DataHandler;

/**
 * Used to test message context properties on the client side.
 */
public class ClientLogicalHandler implements LogicalHandler<LogicalMessageContext> {

    /*
     * The method that does the testing. If a context property is
     * missing or invalid, throw a RuntimeException and it will
     * cause the test to fail.
     */
    public boolean handleMessage(LogicalMessageContext context) {
        LogicalMessage msg = context.getMessage();
//        try {
//            SOAPBody sb = sm.getSOAPBody();
//
//            Node n = sb.getFirstChild();
//            if(n != null){
//                if(!n.getLocalName().equals("picType") ||
//                        !n.getNamespaceURI().equals("http://www.ws-i.org/SampleApplications/SupplyChainManagement/2003-07/Catalog.xsd")){
//                    return true;
//                }
//            }else{
//                return true;
//            }
//        } catch (SOAPException e) {
//            throw new WebServiceException(e);
//        }
        if ((Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            System.out.println("Client handler processing echoImageWithInfo() request!");
            Map<String, DataHandler> attachs = (Map<String, DataHandler>) context.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
            if(attachs.size() != 1)
                throw new WebServiceException("Expected 1 attacment, received :"+attachs.size());
        } else {
            System.out.println("Client handler processing echoImageWithInfo() response!");
            Map<String, DataHandler> attachs = (Map<String, DataHandler>) context.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);
            if(attachs.size() != 1)
                throw new WebServiceException("Expected 1 attacment, received :"+attachs.size());
        }
        return true;
    }
    
    
    // empty methods below here //
    public boolean handleFault(LogicalMessageContext context) {
        return true;
    }
    
    public void close(MessageContext context) {}
    
    
}
