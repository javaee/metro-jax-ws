/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.client;

import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.handler.HandlerException;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;
import java.util.*;

/**
 * This class holds the handler information and roles on the Binding (mutable info in the binding).
 *
 * HandlerConfiguration is immutable, and a new object is created when the BindingImpl is created or User calls
 * Binding.setHandlerChain() or SOAPBinding.setRoles().
 *
 * During invocation in Stub.process(), snapshot of the handler configuration is set in Packet.handlerConfig. The
 * information in the HandlerConfiguration is used by MUPipe and HandlerTube implementations.
 * 
 * @author Rama Pulavarthi
 */
public class HandlerConfiguration {
    private final Set<String> roles;
    /**
     * This chain may contain both soap and logical handlers.
     */
    private final List<Handler> handlerChain;
    private final List<LogicalHandler> logicalHandlers;
    private final List<SOAPHandler> soapHandlers;
    private final List<MessageHandler> messageHandlers;
    private final Set<QName> handlerKnownHeaders;

    /**
     * @param roles               This contains the roles assumed by the Binding implementation.
     * @param handlerChain        This contains the handler chain set on the Binding
     */
    public HandlerConfiguration(Set<String> roles, List<Handler> handlerChain) {
        this.roles = roles;
        this.handlerChain = handlerChain;
        logicalHandlers = new ArrayList<LogicalHandler>();
        soapHandlers = new ArrayList<SOAPHandler>();
        messageHandlers = new ArrayList<MessageHandler>();
        Set<QName> modHandlerKnownHeaders = new HashSet<QName>();

        for (Handler handler : handlerChain) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler) handler);
            } else if (handler instanceof SOAPHandler) {
                soapHandlers.add((SOAPHandler) handler);
                Set<QName> headers = ((SOAPHandler<?>) handler).getHeaders();
                if (headers != null) {
                    modHandlerKnownHeaders.addAll(headers);
                }
            } else if (handler instanceof MessageHandler) {
                messageHandlers.add((MessageHandler) handler);
                Set<QName> headers = ((MessageHandler<?>) handler).getHeaders();
                if (headers != null) {
                    modHandlerKnownHeaders.addAll(headers);
                }
            }else {
                throw new HandlerException("handler.not.valid.type",
                    handler.getClass());
            }
        }
        
        handlerKnownHeaders = Collections.unmodifiableSet(modHandlerKnownHeaders);
    }

    /**
     * This is called when roles as reset on binding using SOAPBinding#setRoles(), to save reparsing the handlers again.
     * @param roles
     * @param oldConfig
     */
    public HandlerConfiguration(Set<String> roles, HandlerConfiguration oldConfig) {
        this.roles = roles;
        this.handlerChain = oldConfig.handlerChain;
        this.logicalHandlers = oldConfig.logicalHandlers;
        this.soapHandlers = oldConfig.soapHandlers;
        this.messageHandlers = oldConfig.messageHandlers;
        this.handlerKnownHeaders = oldConfig.handlerKnownHeaders;
    }

    public Set<String> getRoles() {
        return roles;
    }

    /**
     *
     * @return return a copy of handler chain
     */
    public List<Handler> getHandlerChain() {
        if(handlerChain == null)
            return Collections.emptyList();
        return new ArrayList<Handler>(handlerChain);

    }

    public List<LogicalHandler> getLogicalHandlers() {
        return logicalHandlers;
    }

    public List<SOAPHandler> getSoapHandlers() {
        return soapHandlers;
    }

    public List<MessageHandler> getMessageHandlers() {
        return messageHandlers;
    }

    public Set<QName> getHandlerKnownHeaders() {
        return handlerKnownHeaders;
    }

}
