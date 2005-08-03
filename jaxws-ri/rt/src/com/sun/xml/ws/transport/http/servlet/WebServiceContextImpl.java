/**
 * $Id: WebServiceContextImpl.java,v 1.1 2005-08-03 22:54:07 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.transport.http.servlet;
import java.security.Principal;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.spi.runtime.MessageContext;
import javax.servlet.http.HttpServletRequest;

public class WebServiceContextImpl implements WebServiceContext  {
    
    public static ThreadLocal msgContext = new ThreadLocal();
    
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
        MessageContext ctxt = (MessageContext)msgContext.get();
        if (ctxt != null) {
            HttpServletRequest req = (HttpServletRequest)ctxt.get(
                "javax.xml.ws.servlet.request");
            if (req != null) {
                return req.getUserPrincipal();
            }
        }
        throw new IllegalStateException();
    }


    public boolean isUserInRole(String role) {
        MessageContext ctxt = (MessageContext)msgContext.get();
        if (ctxt != null) {
            HttpServletRequest req = (HttpServletRequest)ctxt.get(
                "javax.xml.ws.servlet.request");
            if (req != null) {
                return req.isUserInRole(role);
            }
        }
        throw new IllegalStateException();
    }
    
}
