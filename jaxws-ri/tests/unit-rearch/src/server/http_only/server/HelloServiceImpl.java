/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package server.http_only.server;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import javax.jws.WebMethod;
import javax.jws.WebService;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;

/**
 * hmmm
 */
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
public class HelloServiceImpl {
    
    @Resource private WebServiceContext wsc;
    private Set<String> clients;

    public HelloServiceImpl() {
        clients = new HashSet<String>();
    }
    
    @WebMethod
    public void introduce() {
        String id = getClientId();
        System.out.println("** storing session id: " + id);
        clients.add(id);
    }
    
    @WebMethod
    public boolean rememberMe() {
        String id = getClientId();
        System.out.println("** looking up id: " + id);
        return clients.contains(id);
    }

    @WebMethod
    public void testServletProperties() {
		MessageContext ctxt = wsc.getMessageContext();
        if (ctxt.get(MessageContext.SERVLET_REQUEST) == null
            || ctxt.get(MessageContext.SERVLET_RESPONSE) == null
            || ctxt.get(MessageContext.SERVLET_CONTEXT) == null) {
            throw new WebServiceException("MessageContext is not populated.");
        }
    }

    @WebMethod
    public void testHttpProperties() {
		MessageContext ctxt = wsc.getMessageContext();
        if (ctxt.get(MessageContext.HTTP_REQUEST_HEADERS) == null
            || ctxt.get(MessageContext.HTTP_REQUEST_METHOD) == null
            || !ctxt.get(MessageContext.HTTP_REQUEST_METHOD).equals("POST")) {
            throw new WebServiceException("MessageContext is not populated.");
        }
    }
    
    private String getClientId() {
        HttpServletRequest req = (HttpServletRequest)
            wsc.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        HttpSession session = req.getSession();
        return session.getId();
    }
    
}
