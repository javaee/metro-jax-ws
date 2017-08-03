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

package wsa.fromwsdl.crinterop.s12.common;

import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPBody;

import testutil.NamespaceContextImpl;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import testutil.W3CAddressingConstants;

/**
 * @author Arun Gupta
 */
public class ClientSOAPHandler implements SOAPHandler<SOAPMessageContext> {
    private static final String REFP_NAME = "//S12:Envelope/S12:Header/ck:CustomerKey";
    private static final String REPLYTO_REFP_VALUE = "Key#123456789";
    private static final String FAULTTO_REFP_VALUE = "Fault#123456789";
    private static final String IS_REFP = "//ck:CustomerKey/@wsa:IsReferenceParameter";

    static final XPath xpath;
    static final XPathExpression xpe;
    static {
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContextImpl());
        try {
            xpe = ClientSOAPHandler.xpath.compile(ClientSOAPHandler.REFP_NAME);
        } catch (XPathExpressionException e) {
            throw new WebServiceException();
        }
    }
    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext context) {
    }

    public boolean handleFault(SOAPMessageContext context) {
        try {
            if ((Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))
                return true;

            if (context.getMessage().getSOAPBody().getFault() == null) {
                return true;
            }
            String opName = getFaultDetail(context);
            if (opName == null)
                return true;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            context.getMessage().writeTo(baos);
            InputSource is = new InputSource(new ByteArrayInputStream(baos.toString().getBytes()));
            if (opName.equals("fault-test1233")) {
                assertRefp(is, FAULTTO_REFP_VALUE);
            } else if (opName.equals("fault-test1234")) {
                assertRefp(is, REPLYTO_REFP_VALUE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void assertRefp(InputSource is, String expected) throws XPathExpressionException {
        Element e = (Element) xpe.evaluate(is, XPathConstants.NODE);
        if (e == null)
            throw new AssertionError("Reference Parameter not found in the fault response");

        Attr attr = e.getAttributeNodeNS(W3CAddressingConstants.isReferenceParameterTag.getNamespaceURI(),
                             W3CAddressingConstants.isReferenceParameterTag.getLocalPart());
        if (attr == null)
            throw new AssertionError("wsa:IsReferenceParameter attribute not found");
        String attrValue = attr.getNodeValue();
        if (!Boolean.parseBoolean(attrValue))
            throw new AssertionError("Incorrect value found for wsa:IsReferenceParameter");

        if (e.getChildNodes().getLength() > 1)
            throw new AssertionError("Redudant children of reference parameter");

        String refpVal = e.getFirstChild().getNodeValue();
        if (refpVal == null)
            throw new AssertionError("Null value for reference parameter");

        if (!refpVal.equals(expected))
            throw new AssertionError("Unexpected reference parameter value, " +
                    "got: \"" + refpVal + "\", expected: \"" + expected + "\"");
    }

    public boolean handleMessage(SOAPMessageContext context) {
        return true;
    }

    protected String getFaultDetail(SOAPMessageContext soapMessageContext) throws SOAPException {
        String detail = null;

        SOAPBody sb = soapMessageContext.getMessage().getSOAPBody();
        if (sb.getFault() == null)
            return detail;

        if (sb.getFault().getFaultString() == null)
            return detail;

        detail = sb.getFault().getFaultString();
        detail = detail.substring(1, detail.indexOf("\"", 1));

        return detail;
    }
}
