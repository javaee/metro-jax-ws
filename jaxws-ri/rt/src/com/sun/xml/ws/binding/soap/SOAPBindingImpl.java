/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.binding.soap;

import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegateFactory;
import com.sun.xml.ws.util.SOAPUtil;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 * @author WS Development Team
 */
public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {


    public static final String X_SOAP12HTTP_BINDING =
        "http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/";

    protected static URI ROLE_NONE;

    protected Set<URI> requiredRoles;
    protected Set<URI> roles;
    protected boolean enableMtom = false;

    // called by DispatchImpl
    public SOAPBindingImpl(String bindingId) {
        super(bindingId);
        setup(getBindingId());
        //setupSystemHandlerDelegate();
    }

    public SOAPBindingImpl(List<Handler> handlerChain, String bindingId) {
        super(handlerChain, bindingId);
        setup(getBindingId());
        //setupSystemHandlerDelegate();
    }

    // if the binding id is unknown, no roles are added
    protected void setup(String bindingId) {
        requiredRoles = new HashSet<URI>();
        if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)) {
            requiredRoles.add(makeURI(
                SOAPNamespaceConstants.ACTOR_NEXT));
        } else if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            requiredRoles.add(makeURI(SOAP12NamespaceConstants.ROLE_NEXT));
            requiredRoles.add(makeURI(
                SOAP12NamespaceConstants.ROLE_ULTIMATE_RECEIVER));
        }
        ROLE_NONE = makeURI(SOAP12NamespaceConstants.ROLE_NONE);
        roles = new HashSet<URI>();
        addRequiredRoles();
        setRolesOnHandlerChain();
    }

    /*
    * For a non standard SOAP1.2 binding, return actual SOAP1.2 binding
    */
    @Override
    public String getBindingId() {
        String bindingId = super.getBindingId();
        if (bindingId.equals(SOAPBindingImpl.X_SOAP12HTTP_BINDING)) {
            return SOAP12HTTP_BINDING;
        }
        return bindingId;
    }

    /*
    * Use this to distinguish SOAP12HTTP_BINDING or X_SOAP12HTTP_BINDING
    */
    @Override
    public String getActualBindingId() {
        return super.getBindingId();
    }

    /*
     * When client sets a new handler chain, must also set roles on
     * the new handler chain caller that gets created.
     */
    public void setHandlerChain(List<Handler> chain) {
        super.setHandlerChain(chain);
        setRolesOnHandlerChain();
    }

    protected void addRequiredRoles() {
        roles.addAll(requiredRoles);
    }

    public java.util.Set<URI> getRoles() {
        return roles;
    }

    /*
     * Adds the next and other roles in case this has
     * been called by a user without them.
     */
    public void setRoles(Set<URI> roles) {
        if (roles == null) {
            roles = new HashSet<URI>();
        }
        if (roles.contains(ROLE_NONE)) {
            LocalizableMessageFactory messageFactory =
                new LocalizableMessageFactory("com.sun.xml.ws.resources.client");
            Localizer localizer = new Localizer();
            Localizable locMessage =
                messageFactory.getMessage("invalid.soap.role.none");
            throw new WebServiceException(localizer.localize(locMessage));
        }
        this.roles = roles;
        addRequiredRoles();
        setRolesOnHandlerChain();
    }


    /**
     * Used typically by the runtime to enable/disable Mtom optimization
     *
     * @return true or false
     */
    public boolean isMTOMEnabled() {
        return enableMtom;
    }

    /**
     * Client application can set if the Mtom optimization should be enabled
     *
     * @param b
     */
    public void setMTOMEnabled(boolean b) {
        this.enableMtom = b;
    }

    public SOAPFactory getSOAPFactory() {
        return SOAPUtil.getSOAPFactory(getBindingId());
    }


    public MessageFactory getMessageFactory() {
        return SOAPUtil.getMessageFactory(getBindingId());
    }

    public HandlerChainCaller getHandlerChainCaller() {
        HandlerChainCaller caller = super.getHandlerChainCaller();
        caller.setRoles(roles);
        return chainCaller;
    }
    
    protected void setRolesOnHandlerChain() {
        if (chainCaller != null) {
            chainCaller.setRoles(roles);
        }
    }

    // used to create uri's to have exception code in one place
    protected URI makeURI(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {

            // this should not happen with the strings in SOAPBindingImpl
            throw new RuntimeException(e);
        }
    }


    protected void setupSystemHandlerDelegate(QName serviceName) {

        SystemHandlerDelegateFactory shdFactory = SystemHandlerDelegateFactory.getFactory();
        if (shdFactory != null) {
            setSystemHandlerDelegate((SystemHandlerDelegate)
                shdFactory.getDelegate(serviceName));
        }
    }
}
