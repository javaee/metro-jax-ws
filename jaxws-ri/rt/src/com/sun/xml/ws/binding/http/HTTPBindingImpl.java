/*
 * $Id: HTTPBindingImpl.java,v 1.3 2005-07-27 18:49:59 jitu Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.binding.http;

import com.sun.xml.ws.binding.BindingImpl;
import javax.xml.ws.handler.Handler;
import java.util.List;
import javax.xml.ws.http.HTTPBinding;



/**
 * @author WS Development Team
 */
public class HTTPBindingImpl extends BindingImpl implements HTTPBinding {

    public HTTPBindingImpl() {
        super(HTTPBinding.HTTP_BINDING);
    }

    // created by HandlerRegistryImpl
    public HTTPBindingImpl(List<Handler> handlerChain) {
        super(handlerChain, HTTPBinding.HTTP_BINDING);
    }

    /*
     * Sets the handler chain
     */
    @Override
    public void setHandlerChain(List<Handler> chain) {
        super.setHandlerChain(chain);
    }
    
}
