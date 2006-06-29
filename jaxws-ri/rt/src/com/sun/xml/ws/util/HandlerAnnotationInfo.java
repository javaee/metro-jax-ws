/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.util;

import java.util.List;
import java.util.Set;

import javax.xml.ws.handler.Handler;

/**
 * Used to hold a list of handlers and a set of roles from an
 * annotated endpoint. At runtime, these are created by the
 * HandlerAnnotationProcessor at the request of client and
 * server code to create the handler chains.
 *
 * @see com.sun.xml.ws.util.HandlerAnnotationProcessor
 *
 * @author JAX-WS Development Team
 */
public class HandlerAnnotationInfo {
    
    private List<Handler> handlers;
    private Set<String> roles;
    
    /**
     * Return the handlers specified by the handler chain descriptor.
     *
     * @return A list of jax-ws handler objects.
     */
    public List<Handler> getHandlers() {
        return handlers;
    }
    
    /**
     * This method should only be called by HandlerAnnotationProcessor.
     *
     * @param handlers The handlers specified by the handler chain descriptor.
     */
    public void setHandlers(List<Handler> handlers) {
        this.handlers = handlers;
    }
    
    /**
     * Return the roles contained in the handler chain descriptor.
     *
     * @return A set of roles.
     */
    public Set<String> getRoles() {
        return roles;
    }
    
    /**
     * This method should only be called by HandlerAnnotationProcessor.
     *
     * @param roles The roles contained in the handler chain descriptor.
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    
}
