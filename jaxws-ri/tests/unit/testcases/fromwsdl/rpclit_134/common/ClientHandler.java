/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2012 Oracle and/or its affiliates. All rights reserved.
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

package fromwsdl.rpclit_134.common;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

/**
 * Used to test message context properties on the client side.
 */
public class ClientHandler implements SOAPHandler<SOAPMessageContext> {

    public boolean handleMessage(SOAPMessageContext context) {
        SOAPMessage sm = context.getMessage();
        try {
            SOAPBody sb = sm.getSOAPBody();

            System.out.println("Inside ClientHandler...");

            Node n = sb.getFirstChild();
            if (n != null) {
                if ((Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
                    if (n.getLocalName().equals("echo3")) {
                        if (!n.getNamespaceURI().equals("http://tempuri.org/wsdl"))
                            throw new WebServiceException("Expected: \"http://tempuri.org/wsdl\", got: " + n.getNamespaceURI());
                        else
                            return true;
                    }
                    if (!n.getNamespaceURI().equals("http://tempuri.org/")) {
                        throw new WebServiceException("Expected: \"http://tempuri.org/\", got: " + n.getNamespaceURI());
                    }

                } else {
                    if (n.getLocalName().equals("echo3Response")) {
                        if (!n.getNamespaceURI().equals("http://example.com/echo3"))
                            throw new WebServiceException("Expected: \"http://example.com/echo3\", got: " + n.getNamespaceURI());
                        else
                            return true;
                    }
                    if (!n.getNamespaceURI().equals("http://example.com/")) {
                        throw new WebServiceException("Expected: \"http://example.com/\", got: " + n.getNamespaceURI());
                    }

                }
            } else {
                return true;
            }
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
        return true;
    }


    // empty methods below here //
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    public void close(MessageContext context) {
    }

    public Set<QName> getHeaders() {
        return null;
    }
}
