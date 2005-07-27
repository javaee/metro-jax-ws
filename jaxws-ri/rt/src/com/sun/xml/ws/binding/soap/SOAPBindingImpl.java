/*
 * $Id: SOAPBindingImpl.java,v 1.4 2005-07-27 18:50:00 jitu Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.binding.soap;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import com.sun.xml.ws.util.SOAPUtil;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.MessageFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import com.sun.xml.ws.binding.BindingImpl;



/**
 * @author WS Development Team
 */
public class SOAPBindingImpl extends BindingImpl implements SOAPBinding,
    com.sun.xml.ws.spi.runtime.SOAPBinding {

    private static URI ROLE_NONE;

    private Set<URI> requiredRoles;
    private Set<URI> roles;
    private boolean enableMtom = false;

    // called by DispatchImpl
    public SOAPBindingImpl(String bindingId) {
        super(bindingId);
        setup(bindingId);
    }

    // created by HandlerRegistryImpl
    public SOAPBindingImpl(List<Handler> handlerChain, String bindingId) {
        super(handlerChain, bindingId);
        setup(bindingId);
    }

    // if the binding id is unknown, no roles are added
    private void setup(String bindingId) {
        requiredRoles = new HashSet<URI>();
        if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)) {
            requiredRoles.add(makeURI(
                "http://schemas.xmlsoap.org/soap/actor/next"));
        } else if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            requiredRoles.add(makeURI(
                "http://www.w3.org/2003/05/soap-envelope/role/next"));
            requiredRoles.add(makeURI(
                "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"));
        }
        ROLE_NONE = makeURI("http://www.w3.org/2003/05/soap-envelope/role/none");
        roles = new HashSet<URI>();
        addRequiredRoles();
        setRolesOnHandlerChain();
    }

    /*
     * When client sets a new handler chain, must also set roles on
     * the new handler chain caller that gets created.
     */
    public void setHandlerChain(List<Handler> chain) {
        super.setHandlerChain(chain);
        setRolesOnHandlerChain();
    }

    private void addRequiredRoles() {
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
     *  Used typically by the runtime to enable/disable Mtom optimization
     * @return  true or false
     */
    public boolean isMTOMEnabled() {
        return enableMtom;
    }

    /**
     * Client application can set if the Mtom optimization should be enabled
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

    // handler chain caller only used on client side
    private void setRolesOnHandlerChain() {
        if (chainCaller != null) {
            chainCaller.setRoles(roles);
        }
    }

    // used to create uri's to have exception code in one place
    private URI makeURI(String str) {
        try {
            return new URI(str);
        } catch (URISyntaxException e) {

            // this should not happen with the strings in SOAPBindingImpl
            throw new RuntimeException(e);
        }
    }
}
