/**
 * $Id: WebServiceContextImpl.java,v 1.2 2005-09-06 23:54:30 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.transport.http.server;
import java.security.Principal;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.spi.runtime.MessageContext;


public class WebServiceContextImpl implements WebServiceContext  {
    
    public ThreadLocal msgContext = new ThreadLocal();
    
    public MessageContext getMessageContext() {
        MessageContext ctxt = (MessageContext)msgContext.get();
        if (ctxt == null) {
            throw new IllegalStateException();
        }
        return ctxt;
    }
    
    public void setMessageContext(MessageContext ctxt) {
        msgContext.set(ctxt);
    }
    
    public Principal getUserPrincipal() {
        return null;
    }


    public boolean isUserInRole(String role) {
        return false;
    }
    
}
