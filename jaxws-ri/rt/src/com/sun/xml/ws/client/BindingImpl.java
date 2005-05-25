/*
 * $Id: BindingImpl.java,v 1.2 2005-05-25 20:16:24 kohlert Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client;

import com.sun.xml.ws.handler.HandlerChainCaller;

import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.security.SecurityConfiguration;
import java.util.ArrayList;
import java.util.List;

/*
 * Instances are created by HandlerRegistryImpl, which then
 * sets the handler chain on the binding impl. The handler
 * caller class actually creates and manages the handlers.
 */

public class BindingImpl implements Binding {

    HandlerChainCaller chainCaller;

    // called by DispatchImpl
    public BindingImpl() {
        chainCaller = new HandlerChainCaller(new ArrayList());
    }

    // created by HandlerRegistryImpl
    BindingImpl(List<Handler> handlerChain) {
        chainCaller = new HandlerChainCaller(handlerChain);
    }

    /*
     * Return a copy of the list.
     */
    public List<Handler> getHandlerChain() {
        return new ArrayList(chainCaller.getHandlerChain());
    }

    public void setHandlerChain(List<Handler> chain) {

        // have old chain call destroy on handlers first
        chainCaller.cleanup();

        // create new caller
        chainCaller = new HandlerChainCaller(chain);
    }

    public SecurityConfiguration getSecurityConfiguration() {
        throw new WebServiceException("Security is not implemented for JAXWS 2.0 Early Access.");
        //return null;
    }

    // used by runtime before invoking handlers
    public HandlerChainCaller getHandlerChainCaller() {
        return chainCaller;
    }

}
