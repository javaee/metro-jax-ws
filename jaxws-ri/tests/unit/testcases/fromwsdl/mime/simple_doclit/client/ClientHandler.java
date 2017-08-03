/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package fromwsdl.mime.simple_doclit.client;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.util.Set;
import java.util.Map;

import javax.xml.namespace.QName;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.activation.DataHandler;

/**
 * Used to test message context properties on the client side.
 */
public class ClientHandler implements SOAPHandler<SOAPMessageContext> {

    /*
     * The method that does the testing. If a context property is
     * missing or invalid, throw a RuntimeException and it will
     * cause the test to fail.
     */
    public boolean handleMessage(SOAPMessageContext context) {
        SOAPMessage sm = context.getMessage();
        try {
            SOAPBody sb = sm.getSOAPBody();

            Node n = sb.getFirstChild();
            if(n != null){
                if(!n.getLocalName().equals("picType") ||
                        !n.getNamespaceURI().equals("http://www.ws-i.org/SampleApplications/SupplyChainManagement/2003-07/Catalog.xsd")){
                    return true;
                }
            }else{
                return true;
            }
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
        if ((Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            System.out.println("Client handler processing echoImageWithInfo() request!");
            Map<String, DataHandler> attachs = (Map<String, DataHandler>) context.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
            if(attachs.size() != 2)
                throw new WebServiceException("Expected 2 attacment, received :"+attachs.size());
        } else {
            System.out.println("Client handler processing echoImageWithInfo() response!");
            Map<String, DataHandler> attachs = (Map<String, DataHandler>) context.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);
            if(attachs.size() != 3)
                throw new WebServiceException("Expected 3 attacment, received :"+attachs.size());
        }
        return true;
    }
    
    
    // empty methods below here //
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }
    
    public void close(MessageContext context) {}
    
    public Set<QName> getHeaders() {
        return null;
    }
    
    
}
