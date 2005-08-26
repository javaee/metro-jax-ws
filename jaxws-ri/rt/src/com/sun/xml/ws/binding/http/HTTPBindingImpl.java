/*
 * $Id: HTTPBindingImpl.java,v 1.5 2005-08-26 21:42:17 bbissett Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.binding.http;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.http.HTTPBinding;

import java.util.List;

import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

/**
 * @author WS Development Team
 */
public class HTTPBindingImpl extends BindingImpl implements HTTPBinding {

    public HTTPBindingImpl() {
        super(HTTPBinding.HTTP_BINDING);
    }

    public HTTPBindingImpl(List<Handler> handlerChain) {
        super(handlerChain, HTTPBinding.HTTP_BINDING);
    }

    /*
     * Sets the handler chain. Only logical handlers are
     * allowed with HTTPBinding.
     */
    @Override
    public void setHandlerChain(List<Handler> chain) {
        for (Handler handler : chain) {
            if (!(handler instanceof LogicalHandler)) {
                LocalizableMessageFactory messageFactory =
                    new LocalizableMessageFactory(
                    "com.sun.xml.ws.resources.client");
                Localizer localizer = new Localizer();
                Localizable locMessage =
                    messageFactory.getMessage("non.logical.handler.set",
                    handler.getClass().toString());
                throw new WebServiceException(localizer.localize(locMessage));
            }
        }
        super.setHandlerChain(chain);
    }
    
}
