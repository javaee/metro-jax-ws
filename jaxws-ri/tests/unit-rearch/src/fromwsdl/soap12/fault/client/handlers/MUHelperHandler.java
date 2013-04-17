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

package fromwsdl.soap12.fault.client.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Simple handler to add headers to an outgoing message.
 */
public class MUHelperHandler implements SOAPHandler<SOAPMessageContext> {

    private List<HelperHandlerHeaderHolder> holders;
    private List<QName> expectedHeaders;
    
    public MUHelperHandler() {
        clearAll();
    }
    
    public void clearAll() {
        holders = new ArrayList<HelperHandlerHeaderHolder>();
        expectedHeaders = null;
    }
    
    /*
     * This qname will be set on the outgoing message with
     * MU set to true, targeted at the role.
     */
    public void setMUHeader(QName qname, String role) {
        holders.add(new HelperHandlerHeaderHolder(qname, role));
    }

    /*
     * This tells the handler to check the headers in the
     * response. The response should have header elements
     * containing the NotUnderstood QNames.
     */
    public void setExpectedHeaders(List<QName> expectedHeaders) {
        this.expectedHeaders = expectedHeaders;
    }
    
    /*
     * A getter in case the test needs to check.
     */
    public List<QName> getExpectedHeaders() {
        return expectedHeaders;
    }
    
    /*
     * The real work happens here.
     */
    public boolean handleMessage(SOAPMessageContext smc) {
        if (smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY) == Boolean.FALSE) {
            if (expectedHeaders != null) {
                return checkHeader(smc);
            }
            return true;
        }
        
        try {
            SOAPMessage message = smc.getMessage();
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            SOAPHeader header = envelope.getHeader();
            if (header == null) { // should be null originally
                header = envelope.addHeader();
            }
            SOAPHeaderElement element = null;
            for (HelperHandlerHeaderHolder holder : holders) {
                element = header.addHeaderElement(holder.getHeader());
                element.setActor(holder.getRole());
                element.setMustUnderstand(true);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Check the headers for expected NotUnderstood QNames.
     */
    private boolean checkHeader(SOAPMessageContext smc) {
        try {
            SOAPMessage message = smc.getMessage();
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            Name attName = envelope.createName("qname");
            SOAPHeader header = envelope.getHeader();
            if (header == null) {
                throw new RuntimeException("no headers in response message");
            }
            
            List<QName> badHeaders = new ArrayList<QName>();
            Iterator iter = header.examineAllHeaderElements();
            while (iter.hasNext()) {
                SOAPHeaderElement she = (SOAPHeaderElement) iter.next();
                String attValue = she.getAttributeValue(attName);
                int sep = attValue.indexOf(":");
                String prefix = attValue.substring(0, sep);
                String local = attValue.substring(sep + 1, attValue.length());
                String uri = she.getNamespaceURI(prefix);
                badHeaders.add(new QName(uri, local));
            }
            
            compareQNames(expectedHeaders, badHeaders);
            
            return true;
        } catch (SOAPException se) {
            throw new RuntimeException(se);
        }
    }
    
    /*
     * If one is missing, try to send a useful message.
     */
    private void compareQNames(List<QName> expected, List<QName> target) {
        if (expected.size() != target.size()) {
            System.err.println("expecting " + expected.size() +
                " headers returned, received " + target.size());
            throw new RuntimeException("received wrong number of headers");
        }
        for (QName header : expected) {
            if (!target.contains(header)) {
                throw new RuntimeException("header " + header +
                    " not found in returned headers");
            }
        }
    }
    
    /***** other handler methods stubbed out *****/
    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext messageContext) {}

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    // the name says it all
    static class HelperHandlerHeaderHolder {
        private QName headerToAdd;
        private String roleToTarget;
        
        public HelperHandlerHeaderHolder(QName header, String role) {
            headerToAdd = header;
            roleToTarget = role;
        }
        
        public QName getHeader() {
            return headerToAdd;
        }
        
        public String getRole() {
            return roleToTarget;
        }
    }
}
