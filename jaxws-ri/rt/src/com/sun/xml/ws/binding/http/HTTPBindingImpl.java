/*
 * $Id: HTTPBindingImpl.java,v 1.2 2005-07-27 00:27:32 vivekp Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.binding.http;

import com.sun.xml.ws.binding.soap.BindingImpl;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import javax.xml.ws.http.HTTPBinding;



/**
 * @author WS Development Team
 */
public class HTTPBindingImpl extends BindingImpl implements HTTPBinding {


    // called by DispatchImpl
    public HTTPBindingImpl() {
        super(HTTPBinding.HTTP_BINDING);
    }

    // created by HandlerRegistryImpl
    public HTTPBindingImpl(List<Handler> handlerChain) {
        super(handlerChain, HTTPBinding.HTTP_BINDING);
    }


    /*
     * When client sets a new handler chain, must also set roles on
     * the new handler chain caller that gets created.
     */
    @Override
    public void setHandlerChain(List<Handler> chain) {
        super.setHandlerChain(chain);
    }
    
//    public void setHttpMethod(javax.xml.ws.http.HTTPBinding.HTTP_METHOD method) {
//
//    }
//
//    public javax.xml.ws.http.HTTPBinding.HTTP_METHOD getHttpMethod() {
//        return null;
//    }
}
