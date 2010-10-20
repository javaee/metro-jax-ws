/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.api.config.management;

import com.sun.xml.ws.api.server.Invoker;

import org.xml.sax.EntityResolver;

/**
 * Store the parameters that were passed into the original WSEndpoint instance
 * upon creation. This allows us to instantiate a new instance with the same
 * parameters.
 *
 * @author Fabian Ritzmann
 */
public class EndpointCreationAttributes {

    private final boolean processHandlerAnnotation;
    private final Invoker invoker;
    private final EntityResolver entityResolver;
    private final boolean isTransportSynchronous;

    /**
     * Instantiate this data access object.
     *
     * @param processHandlerAnnotation The original processHandlerAnnotation setting.
     * @param invoker The original Invoker instance.
     * @param resolver The original EntityResolver instance.
     * @param isTransportSynchronous The original isTransportSynchronous setting.
     */
    public EndpointCreationAttributes(final boolean processHandlerAnnotation,
            final Invoker invoker,
            final EntityResolver resolver,
            final boolean isTransportSynchronous) {
        this.processHandlerAnnotation = processHandlerAnnotation;
        this.invoker = invoker;
        this.entityResolver = resolver;
        this.isTransportSynchronous = isTransportSynchronous;
    }

    /**
     * Return the original processHandlerAnnotation setting.
     *
     * @return The original processHandlerAnnotation setting.
     */
    public boolean isProcessHandlerAnnotation() {
        return this.processHandlerAnnotation;
    }

    /**
     * Return the original Invoker instance.
     *
     * @return The original Invoker instance.
     */
    public Invoker getInvoker() {
        return this.invoker;
    }

    /**
     * Return the original EntityResolver instance.
     *
     * @return The original EntityResolver instance.
     */
    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }

    /**
     * Return the original isTransportSynchronous setting.
     *
     * @return The original isTransportSynchronous setting.
     */
    public boolean isTransportSynchronous() {
        return this.isTransportSynchronous;
    }
}
