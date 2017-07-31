/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
