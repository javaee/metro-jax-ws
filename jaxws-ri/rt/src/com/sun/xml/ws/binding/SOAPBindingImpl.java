/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.binding;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.client.HandlerConfiguration;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.handler.HandlerException;
import com.sun.xml.ws.resources.ClientMessages;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.util.*;


/**
 * @author WS Development Team
 */
public final class SOAPBindingImpl extends BindingImpl implements SOAPBinding {

    public static final String X_SOAP12HTTP_BINDING =
        "http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/";

    private static final String ROLE_NONE = SOAP12NamespaceConstants.ROLE_NONE;
    private Set<String> roles;
    //protected boolean enableMtom;
    protected final SOAPVersion soapVersion;

    private Set<QName> portKnownHeaders = Collections.emptySet();

    /**
     * Use {@link BindingImpl#create(BindingID)} to create this.
     */
    SOAPBindingImpl(BindingID bindingId) {
        this(bindingId,EMPTY_FEATURES);
    }

    /**
     * Use {@link BindingImpl#create(BindingID)} to create this.
     *
     * @param features
     *      These features have a precedence over
     *      {@link BindingID#createBuiltinFeatureList() the implicit features}
     *      associated with the {@link BindingID}. 
     */
    SOAPBindingImpl(BindingID bindingId, WebServiceFeature... features) {
        super(bindingId);
        this.soapVersion = bindingId.getSOAPVersion();
        roles = new HashSet<String>();
        addRequiredRoles();
        //Is this still required? comment out for now
        //setupSystemHandlerDelegate(serviceName);

        setFeatures(features);
        this.features.addAll(bindingId.createBuiltinFeatureList());
    }

    /**
     *  This method should be called if the binding has SOAPSEIModel
     *  The Headers understood by the Port are set, so that they can be used for MU
     *  processing.
     *
     * @param headers
     */
    public void setPortKnownHeaders(@NotNull Set<QName> headers) {
        this.portKnownHeaders = headers;
        // apply this change to HandlerConfiguration
        setHandlerConfig(createHandlerConfig(getHandlerChain()));
    }

    /**
     * This method separates the logical and protocol handlers. 
     * Also parses Headers understood by SOAPHandlers and
     * sets the HandlerConfiguration.
     */
    protected HandlerConfiguration createHandlerConfig(List<Handler> handlerChain) {
        List<LogicalHandler> logicalHandlers = new ArrayList<LogicalHandler>();
        List<SOAPHandler> soapHandlers = new ArrayList<SOAPHandler>();
        List<MessageHandler> messageHandlers = new ArrayList<MessageHandler>();
        Set<QName> handlerKnownHeaders = new HashSet<QName>();

        for (Handler handler : handlerChain) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler) handler);
            } else if (handler instanceof SOAPHandler) {
                soapHandlers.add((SOAPHandler) handler);
                Set<QName> headers = ((SOAPHandler<?>) handler).getHeaders();
                if (headers != null) {
                    handlerKnownHeaders.addAll(headers);
                }
            } else if (handler instanceof MessageHandler) {
                messageHandlers.add((MessageHandler) handler);
                Set<QName> headers = ((MessageHandler<?>) handler).getHeaders();
                if (headers != null) {
                    handlerKnownHeaders.addAll(headers);
                }
            }else {
                throw new HandlerException("handler.not.valid.type",
                    handler.getClass());
            }
        }
        return new HandlerConfiguration(roles,portKnownHeaders,handlerChain,
                logicalHandlers,soapHandlers,messageHandlers,handlerKnownHeaders);
    }

    protected void addRequiredRoles() {
        roles.addAll(soapVersion.requiredRoles);
    }

    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Adds the next and other roles in case this has
     * been called by a user without them.
     * Creates a new HandlerConfiguration object and sets it on the BindingImpl.
     */
    public void setRoles(Set<String> roles) {
        if (roles == null) {
            roles = new HashSet<String>();
        }
        if (roles.contains(ROLE_NONE)) {
            throw new WebServiceException(ClientMessages.INVALID_SOAP_ROLE_NONE());
        }
        this.roles = roles;
        addRequiredRoles();
        HandlerConfiguration oldConfig = getHandlerConfig();
        setHandlerConfig(new HandlerConfiguration(this.roles, portKnownHeaders, oldConfig.getHandlerChain(),
                oldConfig.getLogicalHandlers(),oldConfig.getSoapHandlers(), oldConfig.getMessageHandlers(),
                oldConfig.getHandlerKnownHeaders()));
    }


    /**
     * Used typically by the runtime to enable/disable Mtom optimization
     */
    public boolean isMTOMEnabled() {
        return isFeatureEnabled(MTOMFeature.class);
    }

    /**
     * Client application can override if the MTOM optimization should be enabled
     */
    public void setMTOMEnabled(boolean b) {
        setFeatures(new MTOMFeature(b));
    }

    public SOAPFactory getSOAPFactory() {
        return soapVersion.saajSoapFactory;
    }

    public MessageFactory getMessageFactory() {
        return soapVersion.saajMessageFactory;
    }

    private static final WebServiceFeature[] EMPTY_FEATURES = new WebServiceFeature[0];
}
