/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.binding;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.client.HandlerConfiguration;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.resources.ClientMessages;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author WS Development Team
 */
public final class SOAPBindingImpl extends BindingImpl implements SOAPBinding {

    public static final String X_SOAP12HTTP_BINDING =
        "http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/";

    private static final String ROLE_NONE = SOAP12NamespaceConstants.ROLE_NONE;
    //protected boolean enableMtom;
    protected final SOAPVersion soapVersion;

    private Set<QName> portKnownHeaders = Collections.emptySet();
    private Set<QName> bindingUnderstoodHeaders = new HashSet<QName>();
    private final Lock lock = new ReentrantLock();

    /**
     * Use {@link BindingImpl#create(BindingID)} to create this.
     *
     * @param bindingId SOAP binding ID
     */
    SOAPBindingImpl(BindingID bindingId) {
        this(bindingId,EMPTY_FEATURES);
    }

    /**
     * Use {@link BindingImpl#create(BindingID)} to create this.
     *
     * @param bindingId binding id
     * @param features
     *      These features have a precedence over
     *      {@link BindingID#createBuiltinFeatureList() the implicit features}
     *      associated with the {@link BindingID}. 
     */
    SOAPBindingImpl(BindingID bindingId, WebServiceFeature... features) {
        super(bindingId, features);
        this.soapVersion = bindingId.getSOAPVersion();
        //populates with required roles and updates handlerConfig
        setRoles(new HashSet<String>());
        //Is this still required? comment out for now
        //setupSystemHandlerDelegate(serviceName);

        this.features.addAll(bindingId.createBuiltinFeatureList());
    }

    /**
     *  This method should be called if the binding has SOAPSEIModel
     *  The Headers understood by the Port are set, so that they can be used for MU
     *  processing.
     *
     * @param headers SOAP header names
     */
    public void setPortKnownHeaders(@NotNull Set<QName> headers) {
     
    	try{
    	  lock.lock();
          this.portKnownHeaders = headers;
		} finally {
    		lock.unlock();
    	}
    }

    /**
     * TODO A feature should be created to configure processing of MU headers. 
     * @param header
     * @return
     */
    public boolean understandsHeader(QName header) {
        return serviceMode == javax.xml.ws.Service.Mode.MESSAGE
                || portKnownHeaders.contains(header)
                || bindingUnderstoodHeaders.contains(header);

    }

    /**
     * Sets the handlers on the binding and then sorts the handlers in to logical and protocol handlers.
     * Creates a new HandlerConfiguration object and sets it on the BindingImpl. Also parses Headers understood by
     * Protocol Handlers and sets the HandlerConfiguration.
     */
    public void setHandlerChain(List<Handler> chain) {
        setHandlerConfig(new HandlerConfiguration(getHandlerConfig().getRoles(), chain));
    }

    protected void addRequiredRoles(Set<String> roles) {
        roles.addAll(soapVersion.requiredRoles);
    }

    public Set<String> getRoles() {
        return getHandlerConfig().getRoles();
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
        addRequiredRoles(roles);
        setHandlerConfig(new HandlerConfiguration(roles, getHandlerConfig()));
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
        features.setMTOMEnabled(b);
    }

    public SOAPFactory getSOAPFactory() {
        return soapVersion.getSOAPFactory();
    }

    public MessageFactory getMessageFactory() {
        return soapVersion.getMessageFactory();
    }

}
