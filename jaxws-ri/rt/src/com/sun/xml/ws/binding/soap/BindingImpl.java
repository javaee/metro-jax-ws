/*
 * $Id: BindingImpl.java,v 1.1 2005-07-14 02:01:17 arungupta Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.binding.soap;

import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;

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
 *
 * Also used on the server side, where non-api calls such as
 * getHandlerChainCaller cannot be used. So the binding impl
 * now stores the handler list rather than deferring to the
 * handler chain caller.
 */

/**
 * This class is made abstract as we dont see a situation when a BindingImpl has much meaning without binding id.
 * IOw, for a specific binding there will be a class extending BindingImpl, for example SOAPBindingImpl.
 *
 * The spi Binding interface extends Binding.
 */
public abstract class BindingImpl implements 
    com.sun.xml.ws.spi.runtime.Binding {

    // caller ignored on server side
    HandlerChainCaller chainCaller;
    List<Handler> handlers;
    private String bindingId;
    private SystemHandlerDelegate systemHandlerDelegate;

    // called by DispatchImpl
    public BindingImpl(String bindingId) {
        this.bindingId = bindingId;
    }

    // created by HandlerRegistryImpl
    public BindingImpl(List<Handler> handlerChain, String bindingId) {
        handlers = handlerChain;
        this.bindingId = bindingId;
    }

    /*
     * Return a copy of the list.
     */
    public List<Handler> getHandlerChain() {
        if (chainCaller != null) {
            return new ArrayList(chainCaller.getHandlerChain());
        }
        return handlers;
    }

    public void setHandlerChain(List<Handler> chain) {
        if (chainCaller != null) {
            chainCaller.cleanup();
            chainCaller = new HandlerChainCaller(chain);
            handlers = chainCaller.getHandlerChain();
        } else {
            handlers = chain;
        }
    }

    public SecurityConfiguration getSecurityConfiguration() {
        throw new WebServiceException("Security is not implemented for JAXWS 2.0 Early Access.");
        //return null;
    }

    // used by client runtime before invoking handlers
    public HandlerChainCaller getHandlerChainCaller() {
        if (chainCaller == null) {
            chainCaller = new HandlerChainCaller(handlers);
        }
        return chainCaller;
    }

    public String getBindingId(){
        return bindingId;
    }

    public SystemHandlerDelegate getSystemHandlerDelegate() {
        return systemHandlerDelegate;
    }

    public void setSystemHandlerDelegate(SystemHandlerDelegate delegate) {
        systemHandlerDelegate = delegate;
    }

        
}
