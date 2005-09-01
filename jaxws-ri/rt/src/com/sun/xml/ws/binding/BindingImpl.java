/*
 * $Id: BindingImpl.java,v 1.8 2005-09-01 05:35:51 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.binding;

import com.sun.xml.ws.binding.http.HTTPBindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Instances are created by the service, which then
 * sets the handler chain on the binding impl. The handler
 * caller class actually creates and manages the handlers.
 *
 * <p>Also used on the server side, where non-api calls such as
 * getHandlerChainCaller cannot be used. So the binding impl
 * now stores the handler list rather than deferring to the
 * handler chain caller.
 *
 * <p>This class is made abstract as we dont see a situation when a BindingImpl has much meaning without binding id.
 * IOw, for a specific binding there will be a class extending BindingImpl, for example SOAPBindingImpl.
 *
 * <p>The spi Binding interface extends Binding.
 *
 * @author WS Development Team
 */
public abstract class BindingImpl implements 
    com.sun.xml.ws.spi.runtime.Binding {

    // caller ignored on server side
    protected HandlerChainCaller chainCaller;

    private SystemHandlerDelegate systemHandlerDelegate;
    private List<Handler> handlers;
    private String bindingId;

    // called by DispatchImpl
    public BindingImpl(String bindingId) {
        this.bindingId = bindingId;
    }

    public BindingImpl(List<Handler> handlerChain, String bindingId) {
        handlers = handlerChain;
        this.bindingId = bindingId;
    }

    /**
     * Return a copy of the list. If there is a handler chain caller,
     * this is the proper list. Otherwise, return a copy of 'handlers'
     * or null if list is null.
     */
    public List<Handler> getHandlerChain() {
        if (chainCaller != null) {
            return new ArrayList(chainCaller.getHandlerChain());
        }
        if (handlers == null) {
            return null;
        }
        return new ArrayList(handlers);
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
    
    public String getActualBindingId() {
        return bindingId;
    }

    public SystemHandlerDelegate getSystemHandlerDelegate() {
        return systemHandlerDelegate;
    }

    public void setSystemHandlerDelegate(SystemHandlerDelegate delegate) {
        systemHandlerDelegate = delegate;
    }
    
    public static com.sun.xml.ws.spi.runtime.Binding getBinding(String bindingId,
        Class implementorClass, boolean tokensOK) {
        
        if (bindingId == null) {
            // Gets bindingId from @BindingType annotation
            bindingId = RuntimeModeler.getBindingId(implementorClass);
            if (bindingId == null) {            // Default one
                bindingId = SOAPBinding.SOAP11HTTP_BINDING;
            }
        }
        if (tokensOK) {
            if (bindingId.equals("##SOAP11_HTTP")) {
                bindingId = SOAPBinding.SOAP11HTTP_BINDING;
            } else if (bindingId.equals("##SOAP12_HTTP")) {
                bindingId = SOAPBinding.SOAP12HTTP_BINDING;
            } else if (bindingId.equals("##XML_HTTP")) {
                bindingId = HTTPBinding.HTTP_BINDING;
            }
        }
        if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)
            || bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)
            || bindingId.equals(SOAPBindingImpl.X_SOAP12HTTP_BINDING)) {
            return new SOAPBindingImpl(bindingId); 
        } else if (bindingId.equals(HTTPBinding.HTTP_BINDING)) {
            return new HTTPBindingImpl();
        } else {
            throw new IllegalArgumentException("Wrong bindingId "+bindingId);
        }
    }
    
    public static Binding getDefaultBinding() {
        return new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING);
    }

        
}
