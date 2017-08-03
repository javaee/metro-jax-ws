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

package handler.handler_processing.common;

import java.io.*;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.Node;

public class HandlerUtil implements TestConstants {
    
    private String handlerName; // used to figure out client or server
    
    public HandlerUtil(String name) {
        handlerName = name;
    }
    
    // used to return false only in outbound case
    boolean returnFalseOutbound(MessageContext context, String name) {
        if (ignoreReportMessage(context)) {
            return true;
        }
        registerHandlerCalled(context, name);
        Boolean outbound =
            (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        return !outbound.booleanValue();
    }

    // used to return false only in inbound case
    boolean returnFalseInbound(MessageContext context, String name) {
        if (ignoreReportMessage(context)) {
            return true;
        }
        registerHandlerCalled(context, name);
        Boolean outbound =
            (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        return outbound.booleanValue();
    }

    boolean checkMessageContextProps(MessageContext context) {
        String [] propsToCheck = new String [] {
            MessageContext.MESSAGE_OUTBOUND_PROPERTY,
            //MessageContext.someProp,
            //MessageContext.anotherProp
        };
        for (String propName : propsToCheck) {
            if (context.get(propName) == null) {
                throw new RuntimeException(propName +
                    " property is null");
            }
        }
        return true;
    }

    boolean checkLogicalMessageContext(LogicalMessageContext context) {
        return true;
    }

    /*
     * Not sure just now -- should the "ultimate receiver" role be
     * in the soap message context? todo
     */
    boolean checkSOAPMessageContext(SOAPMessageContext smContext, String name) {

        // make sure roles are not null
        Set<String> roles = smContext.getRoles();
        if (roles == null) {
            throw new RuntimeException("roles are null in SOAPMessageContext");
        } else if (roles.size() != 3) { // two test roles + "next"
            throw new RuntimeException("roles.length should be 3, is " +
                roles.size());
        }

        boolean foundRole1 = false;
        boolean foundRole2 = false;
        boolean foundNext = false;
        String roleType = null;
        if (name.startsWith(SERVER_PREFIX)) {
            roleType = "server";
        } else if (name.startsWith(CLIENT_PREFIX)) {
            roleType = "client";
        }
        if (roleType != null) {
            for (String role: roles) {
                if (HandlerTracker.VERBOSE_HANDLERS) {
                    System.out.println("ROLE: \"" + role + "\"");
                }
                if (role.equals("http://sun.com/" +
                    roleType + "/role1")) {
                    foundRole1 = true;
                } else if (role.equals("http://sun.com/" +
                    roleType + "/role2")) {
                    foundRole2 = true;
                } else if (role.equals(
                    "http://schemas.xmlsoap.org/soap/actor/next")) {
                    foundNext = true;
                }
            }
            if (!foundRole1) {
                throw new RuntimeException(
                    "did not find http://sun.com/" + roleType + "/role1 in " +
                    roleType + " roles");
            }
            if (!foundRole2) {
                throw new RuntimeException(
                    "did not find http://sun.com/" + roleType + "/role2 in " +
                    roleType + " roles");
            }
            if (!foundNext) {
                throw new RuntimeException(
                    "did not find 'next' in " + roleType + " roles");
            }
        }

        // make sure no exception thrown (should be no headers for general case)
        JAXBContext jaxbContext = createJAXBContext();
        QName headerName = new QName("urn:foo", "MissingHeader");
        Object [] headers = smContext.getHeaders(headerName, jaxbContext, true);
        return true;
    }

    /*
     * If the "TestIntResponse" header is present, get the headers again
     * with allRoles=false. When the "role1" role is present in the
     * context, getHeaders() should return one header. When called
     * again with "role1" not in the context, getHeaders() should
     * return no headers.
     *
     * Also, "TestInt" header with no actor specified should be
     * in the message. Check for this with allRoles set to false.
     * Should get no headers in all cases. (I think. -Bobby)
     */
    boolean checkSOAPMessageContextAllRoles(
        SOAPMessageContext smContext, String name) {
        
        // make sure roles are not null
        Set<String> roles = smContext.getRoles();
        if (roles == null) {
            throw new RuntimeException("roles are null in SOAPMessageContext");
        }
        
        JAXBContext jaxbContext = createJAXBContext();
        
        // first TestIntResponse with actor 'client/role1'
        QName headerName = new QName("urn:test:types", "TestIntResponse");
        Object [] headers = smContext.getHeaders(headerName, jaxbContext, true);
        if (headers.length > 0) {
            headers = smContext.getHeaders(headerName, jaxbContext, false);
            if (roles.contains("http://sun.com/client/role1")) {
                if (headers.length != 1) {
                    throw new RuntimeException(
                        "should be 1 matching header, not " + headers.length);
                }
            } else {
                if (headers.length != 0) {
                    throw new RuntimeException("should be no matching headers");
                }
            }
        }
        
        // now check TestInt with no actor
        headerName = new QName("urn:test:types", "TestInt");
        headers = smContext.getHeaders(headerName, jaxbContext, true);
        if (headers.length > 0) {
            headers = smContext.getHeaders(headerName, jaxbContext, false);
            if (headers.length != 1) {
                throw new RuntimeException(
                    "should be 1 matching header, not " + headers.length);
            }
        }
        return true;
    }

    boolean addIntToSOAPMessage(SOAPMessageContext context, int diff) {
        try {
            SOAPMessage message = context.getMessage();
            SOAPBody body = message.getSOAPBody();

            SOAPElement bodyParam =
                (SOAPElement) body.getChildElements().next();
            
            if (!bodyParam.getLocalName().startsWith("TestInt")) {
                
                // just a convenience to ignore report service calls
                return true;
                //todo: use ignoreReportSOAPMessage for this
            }

            SOAPElement valueParam =
                (SOAPElement) bodyParam.getChildElements().next();
            int orig = Integer.parseInt(valueParam.getValue());
            valueParam.setValue(String.valueOf(orig + diff));
        } catch (SOAPException soapException) {
            throw new RuntimeException(soapException);
        }
        return true;
    }

    boolean addIntToLogicalMessage(LogicalMessageContext context, int diff) {
        try {
            LogicalMessage message = context.getMessage();
            Source source = message.getPayload();
            Transformer xFormer =
                TransformerFactory.newInstance().newTransformer();
            xFormer.setOutputProperty("omit-xml-declaration", "yes");
            DOMResult result = new DOMResult();
            xFormer.transform(source, result);
            
            Node documentNode = result.getNode();
            Node requestResponseNode = documentNode.getFirstChild();
            
            if (!requestResponseNode.getLocalName().startsWith("TestInt")) {
                
                // just a convenience to ignore report service calls
                return true;
                // todo: use ignoreReportLogicalMessage for this
            }
            
            Node textNode = requestResponseNode.getFirstChild().getFirstChild();
            int orig = Integer.parseInt(textNode.getNodeValue());
            if (HandlerTracker.VERBOSE_HANDLERS) {
                System.out.print("\torig value = " + orig);
            }
            textNode.setNodeValue(String.valueOf(orig + diff));
            if (HandlerTracker.VERBOSE_HANDLERS) {
                System.out.println("\tnew value = " + textNode.getNodeValue());
            }
            source = new DOMSource(documentNode);
            message.setPayload(source);
            
            /* code for getting a string below
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            xFormer.transform(source, result);
            String msgString = writer.toString();
            System.out.println("\n" + msgString + "\n");
             */
        } catch (TransformerException te) {
            throw new RuntimeException(te);
        }
        return true;
    }

    boolean registerHandlerCalled(MessageContext context, String name) {
        if (ignoreReportMessage(context)) {
            return true;
        }
        if (name == null || name.startsWith(CLIENT_PREFIX)) {
            HandlerTracker.getClientInstance().registerCalledHandler(name);
        } else if (name.startsWith(SERVER_PREFIX)) {
            HandlerTracker.getServerInstance().registerCalledHandler(name);
        }
        return true;
    }

    boolean registerHandleFaultCalled(MessageContext context, String name) {
        if (ignoreReportMessage(context)) {
            return true;
        }
        if (name == null || name.startsWith(CLIENT_PREFIX)) {
            HandlerTracker.getClientInstance().registerCalledHandler(
                name + "_FAULT");
        } else if (name.startsWith(SERVER_PREFIX)) {
            HandlerTracker.getServerInstance().registerCalledHandler(
                name + "_FAULT");
        }
        return true;
    }

    /*
     * Want to replace
     * <TestInt xmlns="urn:test:types"><intin>0</intin></TestInt> with
     * <TestIntResponse xmlns="urn:test:types"><intout>0</intout></TestIntResponse>
     *
     */
    void changeRequestToResponse(SOAPMessageContext context, String name) {
        registerHandlerCalled(context, name);
        if (ignoreReportMessage(context)) {
            return;
        }

        try {
            SOAPMessage message = context.getMessage();
            SOAPBody body = message.getSOAPBody();

            SOAPElement origBodyParam =
                (SOAPElement) body.getChildElements().next();
            origBodyParam.detachNode();
            QName origName = origBodyParam.getElementQName();
            
            body.addBodyElement(new QName(origName.getNamespaceURI(),
                "TestIntResponse", origName.getPrefix())).addChildElement(
                "intout").addTextNode("0");
            
            /*
            if (!bodyParam.getLocalName().startsWith("TestInt") &&
                !bodyParam.getLocalName().startsWith("Fault")) {
                return;
            }*/
        } catch (SOAPException se) {
            throw new RuntimeException(se);
        }        
    }
    
    /*
     * Want to replace
     * <TestInt xmlns="urn:test:types"><intin>0</intin></TestInt> with
     * <TestIntResponse xmlns="urn:test:types"><intout>0</intout></TestIntResponse>
     *
     */
    void changeRequestToResponse(LogicalMessageContext context, String name) {
        registerHandlerCalled(context, name);
        if (ignoreReportMessage(context)) {
            return;
        }

        String response = "<TestIntResponse xmlns=\"urn:test:types\">" +
            "<intout>0</intout></TestIntResponse>";
        context.getMessage().setPayload(new StreamSource(
            new StringReader(response)));
    }
    
    boolean throwRuntimeException(MessageContext context, String name,
            String direction) {
        registerHandlerCalled(context, name); // register called first
        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }

        throw new RuntimeException("handler " + name +
            " throwing runtime exception as instructed FOO");
    }

    boolean throwSimpleProtocolException(MessageContext context, String name,
            String direction) {
        registerHandlerCalled(context, name); // register called first
        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }

        throw new ProtocolException(name +
            " throwing protocol exception as instructed");
    }

    boolean throwSOAPFaultException(MessageContext context, String name,
            String direction) {
        registerHandlerCalled(context, name); // register called first
        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }

        // random info in soap fault
        try {
            QName faultCode = new QName("uri", "local", "prefix");
            String faultString = "fault";
            String faultActor = "faultActor";
            SOAPFault sf = SOAPFactory.newInstance().createFault(faultString, faultCode);
            sf.setFaultActor(faultActor);
            Detail detail = sf.addDetail();
            Name entryName = SOAPFactory.newInstance().createName("someFaultEntry");
            detail.addDetailEntry(entryName);
            throw new SOAPFaultException(sf);
        } catch (SOAPException e) {
            throw new RuntimeException("Couldn't create SOAPFaultException " + e);
        }
    }

    /*
     * This method should be used along with checkForAddedHeader,
     * and the test methods will instruct which handlers to add
     * and to check. The JAXBContext that is created by the util
     * method below only understands the existing generated classes,
     * so if a header is added other than these then a different
     * jaxb context should be created manually. These are the elements
     * that are generated in the soap body:
     *
     * <GetReport xmlns="urn:test:types"><reportType>abc</reportType></GetReport>
     * <GetReportResponse xmlns="urn:test:types"><name>abc</name>[unlimited number of <name> elements]</GetReportResponse>
     * <SetInstruction xmlns="urn:test:types"><name>abc</name><action>123</action></SetInstruction>
     * <TestInt xmlns="urn:test:types"><intin>123</intin></TestInt>
     * <TestIntResponse xmlns="urn:test:types"><intout>123</intout></TestIntResponse>
     * 
     * If the 'role' param is passed in, use a different header value 
     * for that element. See EndToEndTest.testAllRoles for the test case.
     *
     */
    boolean addHeaderToMessage(SOAPMessageContext context, String direction,
        String role) {
        
        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }
        
        try {
            SOAPEnvelope envelope =
                context.getMessage().getSOAPPart().getEnvelope();
            SOAPHeader header = envelope.getHeader();
            if (header == null) { // should be null originally
                header = envelope.addHeader();
            }
            QName headerName = new QName("urn:test:types", "TestInt");
            if (role != null) {
                headerName = new QName("urn:test:types", "TestIntResponse");
            }
            SOAPHeaderElement hElement =
                header.addHeaderElement(headerName);
            hElement.addChildElement("intin").setValue("123");
            if (role != null) {
                hElement.setActor(role);
            }
            int dummy = 0; // to stay in scope in debugger

        } catch (SOAPException soapException) {
            throw new RuntimeException(soapException);
        }
        return true;
    }

    /*
     * See addHeaderToMessage. To see the actual soap message in
     * local client transport, add System.out to the setTransport
     * call in TestCaseBase.getTestStub. (You could set it for
     * the report stub as well, but the test methods are instructed
     * to ignore those messages in general.)
     */
    boolean checkForAddedHeader(SOAPMessageContext context, String direction) {
        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }
        
        // check with saaj
        boolean foundWithSaaj = false;
        Name targetName = null;
        try {
            SOAPMessage message = context.getMessage();
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            SOAPHeader sh = message.getSOAPHeader();
            targetName = envelope.createName("TestInt",
                "", "urn:test:types");
            if (sh == null) {
                throw new RuntimeException("did not find header in message");
            }
            Iterator elementIter = sh.examineAllHeaderElements();
            while (elementIter.hasNext()) {
                SOAPHeaderElement element =
                    (SOAPHeaderElement) elementIter.next();
                if (element.getElementName().equals(targetName)) {
                    foundWithSaaj = true;
                    continue;
                }
            }
        } catch (SOAPException se) {
            throw new RuntimeException(se);
        }
        if (!foundWithSaaj) {
            throw new RuntimeException("did not find header matching " +
                targetName.getLocalName());
        }
        
        // check with jaxb
        JAXBContext jaxbContext = createJAXBContext();
        QName headerName = new QName("uri", "local"); // changeme
        // Object [] headers = context.getHeaders(headerName, jaxbContext, true);
        // test result here; throw exception if wrong
        return true;
    }

    boolean addMimeHeaderToMessage(SOAPMessageContext context, String direction) {

        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }

        SOAPMessage msg = context.getMessage();
        MimeHeaders mimeHdrs = msg.getMimeHeaders();
        mimeHdrs.setHeader("My-Mime-Hdr", "Highly Confidential");
        context.setMessage(msg);

        return true;
    }

    boolean checkMimeHeaderInMessage(SOAPMessageContext context, String direction) {

        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }

        SOAPMessage msg = context.getMessage();
        MimeHeaders mimeHdrs = msg.getMimeHeaders();
        String[] myHdrs = mimeHdrs.getHeader("My-Mime-Hdr");
        if(myHdrs == null || !myHdrs[0].equals("Highly Confidential"))
            throw new RuntimeException("Expected Mime Header Not present");
        else
            return true;        
    }

    /*
     * Add a mustUnderstand header to the message that should
     * be understood at the other end. If the direction does not
     * match, remove any headers in the message so the endpoint
     * or client does not get headers that shouldn't be there.
     *
     * The "actor" param is optional. Added for the client MU 2 test.
     */
    boolean addMustUndertandHeader(SOAPMessageContext context,
            String name, String direction, boolean validHeader, String actor) {
        registerHandlerCalled(context, name); // register called first
        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                    (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
                // not the direction we want
                SOAPEnvelope envelope =
                    context.getMessage().getSOAPPart().getEnvelope();
                SOAPHeader header = envelope.getHeader();
                if (header != null) {
                    header.detachNode();
                }
                return true;
            }
            if (ignoreReportMessage(context)) {
                return true;
            }
            SOAPEnvelope envelope =
                context.getMessage().getSOAPPart().getEnvelope();
            SOAPHeader header = envelope.getHeader();
            if (header == null) {
                header = envelope.addHeader();
            }
            QName elementName = null;
            if (validHeader) {
                elementName = new QName("http://example.com/someheader",
                    "testheader1", "ns3");
            } else {
                elementName = new QName("http://example.com/someheader",
                    "misunderstood", "foo");
            }
            SOAPHeaderElement muElement = header.addHeaderElement(elementName);
            muElement.setMustUnderstand(true);
            if (actor != null) {
                muElement.setActor(actor);
            } else {
                if (name.startsWith(CLIENT_PREFIX)) {
                    // target one of the server actors
                    muElement.setActor("http://sun.com/server/role1");
                } else {
                    // target the 'next' role
                    muElement.setActor(
                        "http://schemas.xmlsoap.org/soap/actor/next");
                }
            }
        } catch (SOAPException se) {
            throw new RuntimeException(se);
        }
        return true;
    }

    /*
     * Check to see if the property set by the user is included
     * in the MessageContext object.
     */
    boolean checkUserProperty(MessageContext context,
        String name, String direction) {

        registerHandlerCalled(context, name); // register called first
        return checkProperty(context, name, direction,
            USER_CLIENT_PROPERTY_NAME, USER_PROPERTY_CLIENT_SET);
    }

    boolean checkProperty(MessageContext context, String name,
        String direction, String expectedName, String expectedValue) {

        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }
        Object propValue = context.get(expectedName);
        if (propValue == null) {
            throw new RuntimeException(
                "User set property not in message context");
        }
        String propValueString = (String) propValue;
        if (!propValueString.equals(expectedValue)) {
            throw new RuntimeException("expecting: '" +
                USER_PROPERTY_CLIENT_SET +
                "', received: '" + propValueString + "'");
        }
        return true;
    }
    
    boolean addUserProperty(MessageContext context,
        String name, String direction) {

        registerHandlerCalled(context, name); // register called first
        return addProperty(context, name, direction,
            USER_HANDLER_PROPERTY_NAME, USER_PROPERTY_HANDLER_SET);
    }
    
    boolean addProperty(MessageContext context,
        String name, String direction,
        String propName, String propValue) {

        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }

        // add the property
        context.put(propName, propValue);
        context.setScope(propName, MessageContext.Scope.APPLICATION);
        return true;
    }

    boolean checkAndAddUserProps(MessageContext context,
        String name, String direction, String propName, String propValue) {

        registerHandlerCalled(context, name); // register called first
        return (checkProperty(context, name, direction,
                USER_HANDLER_PROPERTY_NAME, USER_PROPERTY_HANDLER_SET) &&

            /* present during response because context is same object used
               during request. */
            checkProperty(context, name, direction,
                USER_CLIENT_PROPERTY_NAME, USER_PROPERTY_CLIENT_SET) &&

            addProperty(context, name, direction,
                USER_HANDLER_PROPERTY_NAME + INBOUND,
                USER_PROPERTY_HANDLER_SET + INBOUND));
    }

    /*
     * Remove the contents of the body and insert a fault message.
     * Then throw a protocol exception with a different string than
     * in the fault.
     */
    boolean insertFaultMessageAndThrowPE(SOAPMessageContext context,
        String name, String direction) {
        
        registerHandlerCalled(context, name); // register called first
        Boolean outbound = (Boolean)
            context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if ( (outbound == Boolean.TRUE && direction.equals(INBOUND)) ||
                (outbound == Boolean.FALSE && direction.equals(OUTBOUND)) ) {
            // not the direction we want
            return true;
        }
        if (ignoreReportMessage(context)) {
            return true;
        }
        
        // now add the fault
        try {
            SOAPEnvelope envelope =
                context.getMessage().getSOAPPart().getEnvelope();
            SOAPBody body = envelope.getBody();
            body.removeContents();
            SOAPFault fault = body.addFault();
            fault.setFaultCode(envelope.createName("Server",
                        "env", "http://schemas.xmlsoap.org/soap/envelope/"));
            fault.setFaultString(MESSAGE_IN_FAULT);
        } catch (SOAPException se) {
            throw new RuntimeException("error creating fault in " +
                name + ": " + se);
        }

        // and throw the exception
        throw new ProtocolException(MESSAGE_IN_EXCEPTION);
    }

    boolean checkFaultMessageString(SOAPMessageContext context) {
        String message = null;
        try {
            SOAPBody body = context.getMessage().getSOAPBody();
            SOAPFault fault = body.getFault();
            message = fault.getFaultString();
        } catch (SOAPException se) {
            throw new RuntimeException(se);
        }
        if (!message.equals(MESSAGE_IN_FAULT)) {
            throw new RuntimeException("did not find proper message in fault");
        }
        return true;
    }

    /*
     * This tests a namespace prefix issue when
     * faults from the endpoint go through logical and soap handlers.
     */
    boolean getFaultInMessage(LogicalMessageContext context) {
        context.getMessage().getPayload();
        return true;
    }
    
    /*
     * This tests a namespace prefix issue when
     * faults from the endpoint go through logical and soap handlers.
     */
    boolean getFaultInMessage(SOAPMessageContext context) {
        try {
            context.getMessage().getSOAPBody();
        } catch (SOAPException se) {
            throw new RuntimeException(se);
        }
        return true;
    }
    
    // this requirement has been removed, so for now just return true
    boolean addBadPropertyTypes(MessageContext context) {
        System.out.println("(warning, HandlerUtil.addBadPropertyTypes " +
            "is no longer a valid test)");
	return true;
    }
    
    // internal methods below //

    /*
     * Used to check if a message is for the report service instead
     * of the test service. todo: break out the sections of getting soap
     * message or source into different methods to use above so we're not
     * duplicating work.
     */
    private boolean ignoreReportMessage(MessageContext context) {
        try {
            if (context instanceof LogicalMessageContext) {
                return ignoreReportLogicalMessage((LogicalMessageContext) context);
            } else if (context instanceof SOAPMessageContext) {
                return ignoreReportSOAPMessage((SOAPMessageContext) context);
            } else {
                throw new RuntimeException("context must be soap or logical, not " +
                        context.getClass().toString());
            }
        } catch (SOAPException se) {
            throw new RuntimeException(se);
        } catch (TransformerException te) {
            throw new RuntimeException(te);
        }
    }
    
    private boolean ignoreReportLogicalMessage(LogicalMessageContext context)
        throws TransformerException {
        
        LogicalMessage message = context.getMessage();
        Source source = message.getPayload();
        Transformer xFormer =
            TransformerFactory.newInstance().newTransformer();
        xFormer.setOutputProperty("omit-xml-declaration", "yes");
        DOMResult result = new DOMResult();
        xFormer.transform(source, result);

        Node documentNode = result.getNode();
        Node requestResponseNode = documentNode.getFirstChild();

        if (!requestResponseNode.getLocalName().startsWith("TestInt") &&
            !requestResponseNode.getLocalName().startsWith("Fault")) {
            return true;
        }
        return false;
    }
    
    private boolean ignoreReportSOAPMessage(SOAPMessageContext context)
        throws SOAPException {
        
        SOAPMessage message = context.getMessage();
        SOAPBody body = message.getSOAPBody();

        SOAPElement bodyParam =
            (SOAPElement) body.getChildElements().next();

        if (!bodyParam.getLocalName().startsWith("TestInt") &&
            !bodyParam.getLocalName().startsWith("Fault")) {
            return true;
        }
        return false;
    }

    /*
     * We don't know at compile time whether there will be server
     * or client side objects.
     */
    private JAXBContext createJAXBContext() {
        try {
            if (handlerName != null && handlerName.startsWith(SERVER_PREFIX)) {
                return JAXBContext.newInstance(Class.forName(
                    "handler.handler_processing.server.ObjectFactory"));
            } else {
                return JAXBContext.newInstance(Class.forName(
                    "handler.handler_processing.client.ObjectFactory"));
            }
        } catch (Exception e) { // ClassNotFoundException or JAXBException
            throw new RuntimeException(e);
        }
    }
}
