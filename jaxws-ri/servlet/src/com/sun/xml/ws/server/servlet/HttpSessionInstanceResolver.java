package com.sun.xml.ws.server.servlet;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.developer.servlet.HttpSessionScope;
import com.sun.xml.ws.server.AbstractMultiInstanceResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

/**
 * Instance resolver that ties a service instance per {@link HttpSession}.
 *
 * TODO: how do we dispose instances?
 *
 * @author Kohsuke Kawaguchi
 */
public class HttpSessionInstanceResolver<T> extends AbstractMultiInstanceResolver<T> {
    public HttpSessionInstanceResolver(@NotNull Class<T> clazz) {
        super(clazz);
    }

    public @NotNull T resolve(Packet request) {
        HttpServletRequest sr = (HttpServletRequest) request.get(MessageContext.SERVLET_REQUEST);
        if(sr==null)
            throw new WebServiceException(
                clazz+" has @"+ HttpSessionScope.class.getSimpleName()+" but it's deployed on non-servlet endpoint");
        
        HttpSession session = sr.getSession();
        T o = clazz.cast(session.getAttribute(clazz.getName()));
        if(o==null) {
            o = create();
            session.setAttribute(clazz.getName(),o);
        }
        return o;
    }
}
