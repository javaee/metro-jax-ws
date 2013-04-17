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

package fromwsdl.handler.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.*;
import javax.xml.ws.handler.soap.*;
import javax.xml.ws.ProtocolException;
import javax.xml.soap.SOAPMessage;

public class BaseSOAPHandler implements SOAPHandler<SOAPMessageContext>,
    TestConstants, HasName {
    
    HandlerTracker tracker;
    String name;
    HandlerUtil util;
    boolean available;
    
    public BaseSOAPHandler() {
        available = false;
    }
    
    // used when adding a handler programatically
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean handleMessage(SOAPMessageContext messageContext) {
        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("handler " + name + " (action: " +
                tracker.getHandlerAction(name) + ")");
        }
        switch (tracker.getHandlerAction(name)) {
            case HA_RETURN_TRUE :
                return true;
            case HA_RETURN_FALSE :
                return false;
            case HA_RETURN_FALSE_OUTBOUND :
                return util.returnFalseOutbound(messageContext, name);
            case HA_RETURN_FALSE_CHANGE_MESSAGE :
                util.changeRequestToResponse(messageContext, name);
                return false;
            case HA_CHECK_MC_PROPS :
                return util.checkMessageContextProps(messageContext);
            case HA_CHECK_SMC :
                return util.checkSOAPMessageContext(messageContext, name);
            case HA_CHECK_SMC_ALL_ROLES :
                return util.checkSOAPMessageContextAllRoles(
                    messageContext, name);
            case HA_CHECK_LMC :
                throw new UnsupportedOperationException(name +
                    " can't check logical message context");
            case HA_ADD_ONE :
                return util.addIntToSOAPMessage(messageContext, 1);
            case HA_REGISTER_HANDLE_XYZ :
                return util.registerHandlerCalled(messageContext, name);
            case HA_THROW_RUNTIME_EXCEPTION_OUTBOUND:
                return util.throwRuntimeException(
                    messageContext, name, OUTBOUND);
            case HA_THROW_RUNTIME_EXCEPTION_INBOUND:
                return util.throwRuntimeException(
                    messageContext, name, INBOUND);
            case HA_ADD_HEADER_OUTBOUND :
                return util.addHeaderToMessage(messageContext, OUTBOUND, null);
            case HA_CHECK_FOR_ADDED_HEADER_OUTBOUND :
                return util.checkForAddedHeader(messageContext, OUTBOUND);
            case HA_CHECK_FOR_ADDED_HEADER_INBOUND :
                return util.checkForAddedHeader(messageContext, INBOUND);
            case HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND :
                return util.throwSimpleProtocolException(messageContext,
                    name, OUTBOUND);
            case HA_THROW_SOAP_FAULT_EXCEPTION_OUTBOUND :
                return util.throwSOAPFaultException(messageContext,
                    name, OUTBOUND);
            case HA_THROW_PROTOCOL_EXCEPTION_INBOUND :
                return util.throwSimpleProtocolException(messageContext,
                    name, INBOUND);
            case HA_THROW_SOAP_FAULT_EXCEPTION_INBOUND :
                return util.throwSOAPFaultException(messageContext,
                    name, INBOUND);
            case HA_CHECK_FOR_USER_PROPERTY_OUTBOUND :
                return util.checkUserProperty(messageContext, name, OUTBOUND);
            case HA_ADD_USER_PROPERTY_INBOUND:
                return util.addUserProperty(messageContext, name, INBOUND);
            case HA_ADD_GOOD_MU_HEADER_OUTBOUND :
                return util.addMustUndertandHeader(messageContext,
                    name, OUTBOUND, true, null);
            case HA_ADD_BAD_MU_HEADER_OUTBOUND :
                return util.addMustUndertandHeader(messageContext,
                    name, OUTBOUND, false, null);
            case HA_ADD_BAD_MU_HEADER_CLIENT2_OUTBOUND :
                return util.addMustUndertandHeader(messageContext,
                    name, OUTBOUND, false, "http://sun.com/client/role2");
            case HA_ADD_HEADER_OUTBOUND_CLIENT_ROLE1 :
                // add one header with actor and one without
                return ( util.addHeaderToMessage(messageContext,
                    OUTBOUND, "http://sun.com/client/role1") &&
                    util.addHeaderToMessage(messageContext,
                    OUTBOUND, null) );
            case HA_INSERT_FAULT_AND_THROW_PE_INBOUND :
                return util.insertFaultMessageAndThrowPE(
                    messageContext, name, INBOUND);
            case HA_INSERT_FAULT_AND_THROW_PE_OUTBOUND :
                return util.insertFaultMessageAndThrowPE(
                    messageContext, name, OUTBOUND);
            case HA_ADD_USER_PROPERTY_OUTBOUND :
                return util.addUserProperty(messageContext, name, OUTBOUND);
	    case HA_CHECK_MC_BAD_PROPS :
		return util.addBadPropertyTypes(messageContext);
        }
        throw new RuntimeException(name + " didn't understand action: " +
            tracker.getHandlerAction(name));
    }
    
    @PostConstruct
    public void initTheHandler() {
        if (name.startsWith(CLIENT_PREFIX)) {
            tracker = HandlerTracker.getClientInstance();
        } else if (name.startsWith(SERVER_PREFIX)) {
            tracker = HandlerTracker.getServerInstance();
        } else {
            throw new RuntimeException("unrecognized prefix in name: " + name);
        }
        tracker.registerHandler(name);
        util = new HandlerUtil(name);
        available = true;
    }
    
    public Set<QName> getHeaders() {
        Set<QName> headers = new HashSet<QName>();
        headers.add(new QName("http://example.com/someheader", "testheader1"));
        return headers;
    }
    
    public void close(MessageContext messageContext) {
        tracker.registerClose(name);
    }
    
    @PreDestroy
    public void destroyHandler() {
        if (tracker != null) {
            tracker.registerDestroy(name);
        }
        available = false;
    }
    
    // always register that handleFault was called and then check action
    public boolean handleFault(SOAPMessageContext messageContext) {
        tracker.registerCalledHandler(name + "_FAULT");
        switch (tracker.getHandleFaultAction(name)) {
            case HA_RETURN_TRUE :
                return true;
            case HA_RETURN_FALSE :
                return false;
            case HA_REGISTER_HANDLE_XYZ : // already registered
                return true;
            case HF_THROW_RUNTIME_EXCEPTION :
                throw new RuntimeException(name +
                    " throwing RuntimeException from handleFault");
            case HF_THROW_PROTOCOL_EXCEPTION :
                throw new ProtocolException(name +
                    " throwing ProtocolException from handleFault");
            case HF_RETURN_FALSE:
                return false;
            case HF_CHECK_FAULT_MESSAGE_STRING :
                return util.checkFaultMessageString(messageContext);
            case HF_GET_FAULT_IN_MESSAGE :
                return util.getFaultInMessage(messageContext);
            case HF_THROW_TEST_PROTOCOL_EXCEPTION :
                throw new TestProtocolException(name +
                    " throwing TestProtocolException from handleFault");
        }
        throw new RuntimeException(name + " didn't understand action: " +
            tracker.getHandleFaultAction(name));
    }
    
    public boolean isAvailable() {
        return available;
    }
    
}
