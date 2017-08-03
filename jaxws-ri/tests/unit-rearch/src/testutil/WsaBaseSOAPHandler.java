/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package testutil;

import java.util.Iterator;
import java.util.Set;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;

/**
 * @author Arun Gupta
 */
public abstract class WsaBaseSOAPHandler implements SOAPHandler<SOAPMessageContext> {

    String name;
//    protected SOAPBody soapBody;

    public WsaBaseSOAPHandler() {
    }

    public boolean handleMessage(SOAPMessageContext context) {
        boolean outbound = (Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            String oper = getOperationName(getSOAPBody(context));
            if (outbound) {
                context.put("op.name", oper);
            } else {
                if (getSOAPBody(context) != null && getSOAPBody(context).getFault() != null) {
                    String detailName = getSOAPBody(context).getFault().getDetail().getFirstChild().getLocalName();
                    checkFaultActions((String)context.get("op.name"), detailName, getAction(context));
                } else {
                    checkInboundActions(oper, getAction(context));
                }
            }
        } catch (SOAPException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return handleMessage(context);
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext messageContext) {
    }

    protected SOAPBody getSOAPBody(SOAPMessageContext context) throws SOAPException {
        return context.getMessage().getSOAPBody();
    }

    protected String getAction(SOAPMessageContext context) throws SOAPException {
        SOAPMessage message = context.getMessage();
        SOAPHeader header = message.getSOAPHeader();
        Iterator iter = header.getChildElements(getActionQName());
        if (!iter.hasNext())
            throw new WebServiceException("wsa:Action header is missing in the message");

        Node node = (Node)iter.next();
        String action = node.getFirstChild().getNodeValue();
        return action;
    }

    protected String getOperationName(SOAPBody soapBody) throws SOAPException {
        if (soapBody == null)
            return null;

        if (soapBody.getFirstChild() == null)
            return null;

        return soapBody.getFirstChild().getLocalName();
    }

    protected void checkFaultActions(String requestName, String detailName, String action) {
    }

    public QName getActionQName() {
        return W3CAddressingConstants.actionTag;
    }

    protected abstract void checkInboundActions(String oper, String action);
}
