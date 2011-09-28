/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.handler;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.saaj.SAAJFactory;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.message.saaj.SAAJMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link SOAPMessageContext}. This class is used at runtime
 * to pass to the handlers for processing soap messages.
 *
 * @see MessageContextImpl
 *
 * @author WS Development Team
 */
public class SOAPMessageContextImpl extends MessageUpdatableContext implements SOAPMessageContext {

    private Set<String> roles;
    private SOAPMessage soapMsg = null;
    private WSBinding binding;

    public SOAPMessageContextImpl(WSBinding binding, Packet packet,Set<String> roles) {
        super(packet);
        this.binding = binding;
        this.roles = roles;
    }

    public SOAPMessage getMessage() {
        if(soapMsg == null) {
            try {
            	Message m = packet.getMessage();
            	soapMsg = m != null ? m.readAsSOAPMessage() : null;
            } catch (SOAPException e) {
                throw new WebServiceException(e);
            }
        }
        return soapMsg;
    }

    public void setMessage(SOAPMessage soapMsg) {
        try {
            this.soapMsg = soapMsg;
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }
    
    void setPacketMessage(Message newMessage){
        if(newMessage != null) {
            packet.setMessage(newMessage);
            soapMsg = null;
        }
    }
    
    protected void updateMessage() {
        //Check if SOAPMessage has changed, if so construct new one,
        // Packet are handled through MessageContext
        if(soapMsg != null) {
            packet.setMessage(SAAJFactory.create(soapMsg));
            soapMsg = null;
        }
    }

    public Object[] getHeaders(QName header, JAXBContext jaxbContext, boolean allRoles) {
        SOAPVersion soapVersion = binding.getSOAPVersion();

        List<Object> beanList = new ArrayList<Object>();
        try {
            Iterator<Header> itr = packet.getMessage().getHeaders().getHeaders(header,false);
            if(allRoles) {
                while(itr.hasNext()) {
                    beanList.add(itr.next().readAsJAXB(jaxbContext.createUnmarshaller()));
                }
            } else {
                while(itr.hasNext()) {
                    Header soapHeader = itr.next();
                    //Check if the role is one of the roles on this Binding
                    String role = soapHeader.getRole(soapVersion);
                    if(getRoles().contains(role)) {
                        beanList.add(soapHeader.readAsJAXB(jaxbContext.createUnmarshaller()));
                    }
                }
            }
            return beanList.toArray();
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    public Set<String> getRoles() {
        return roles;
    } 
}
