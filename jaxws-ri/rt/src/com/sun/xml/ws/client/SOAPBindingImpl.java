/*
 * $Id: SOAPBindingImpl.java,v 1.2 2005-05-27 17:50:56 vivekp Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author JAX-RPC RI Development Team
 */
public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {

    private static URI ROLE_NEXT_1_1;
    private static URI ROLE_NEXT_1_2;
    private static URI ROLE_NONE;
    private static URI ROLE_ULTIMATE_RECEIVER;

    private Set<URI> roles;
    private boolean enableMtom = false;

    // called by DispatchImpl
    SOAPBindingImpl() {
        super();
        setup();
    }

    // created by HandlerRegistryImpl
    SOAPBindingImpl(List<Handler> handlerChain) {
        super(handlerChain);
        setup();
    }

    private void setup() {
        ROLE_NEXT_1_1 = makeURI("http://schemas.xmlsoap.org/soap/actor/next");
        ROLE_NEXT_1_2 = makeURI("http://www.w3.org/2003/05/soap-envelope/role/next");
        ROLE_NONE = makeURI("http://www.w3.org/2003/05/soap-envelope/role/none");
        ROLE_ULTIMATE_RECEIVER = makeURI("http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");
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
        roles.add(ROLE_NEXT_1_1);
        roles.add(ROLE_NEXT_1_2);
        roles.add(ROLE_ULTIMATE_RECEIVER);
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
     * Client application can set if the Mtom optimization should be enabled
     * @param enable
     */
    public void setMtomEnabled(boolean enable){
        enableMtom = enable;
    }

    /**
     *  Used typically by the runtime to enable/disable Mtom optimization
     * @return  true or false
     */
    public boolean isMtomEnabled(){
        return enableMtom;
    }

    private void setRolesOnHandlerChain() {
        Set<String> roleStrings = new HashSet<String>(roles.size());
        for (URI uri : roles) {
            roleStrings.add(uri.toString());
        }
        chainCaller.setRoles(roleStrings);
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
