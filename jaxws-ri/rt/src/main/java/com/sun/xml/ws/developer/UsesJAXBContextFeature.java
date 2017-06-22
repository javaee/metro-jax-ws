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

package com.sun.xml.ws.developer;

import com.sun.xml.ws.api.FeatureConstructor;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.WebServiceFeature;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * A {@link WebServiceFeature} that instructs the JAX-WS runtime to use a specific {@link JAXBContextFactory}
 * instance of creating {@link JAXBContext}.
 * 
 * @see UsesJAXBContext
 * @since 2.1.5
 * @author Kohsuke Kawaguchi
 */
@ManagedData
public class UsesJAXBContextFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link UsesJAXBContext} feature.
     */
    public static final String ID = "http://jax-ws.dev.java.net/features/uses-jaxb-context";

    private final JAXBContextFactory factory;

    /**
     * Creates {@link UsesJAXBContextFeature}.
     *
     * @param factoryClass
     *      This class has to have a public no-arg constructor, which will be invoked to create
     *      a new instance. {@link JAXBContextFactory#createJAXBContext(SEIModel, List, List)} will
     *      be then called to create {@link JAXBContext}.
     */
    @FeatureConstructor("value")
    public UsesJAXBContextFeature(@NotNull Class<? extends JAXBContextFactory> factoryClass) {
        try {
            factory = factoryClass.getConstructor().newInstance();
        } catch (InstantiationException e) {
            Error x = new InstantiationError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (IllegalAccessException e) {
            Error x = new IllegalAccessError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (InvocationTargetException e) {
            Error x = new InstantiationError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (NoSuchMethodException e) {
            Error x = new NoSuchMethodError(e.getMessage());
            x.initCause(e);
            throw x;
        }
    }

    /**
     * Creates {@link UsesJAXBContextFeature}.
     * This version allows {@link JAXBContextFactory} to carry application specific state.
     *
     * @param factory
     *      Uses a specific instance of {@link JAXBContextFactory} to create {@link JAXBContext}.
     */
    public UsesJAXBContextFeature(@Nullable JAXBContextFactory factory) {
        this.factory = factory;
    }

    /**
     * Creates {@link UsesJAXBContextFeature}.
     * This version allows you to create {@link JAXBRIContext} upfront and uses it.
     */
    public UsesJAXBContextFeature(@Nullable final JAXBRIContext context) {
        this.factory = new JAXBContextFactory() {
            @NotNull
            public JAXBRIContext createJAXBContext(@NotNull SEIModel sei, @NotNull List<Class> classesToBind, @NotNull List<TypeReference> typeReferences) throws JAXBException {
                return context;
            }
        };
    }

    /**
     * Gets the {@link JAXBContextFactory} instance to be used for creating {@link JAXBContext} for SEI.
     *
     * @return
     *      null if the default {@link JAXBContext} shall be used.
     */
    @ManagedAttribute
    public @Nullable JAXBContextFactory getFactory() {
        return factory;
    }

    @ManagedAttribute
    public String getID() {
        return ID;
    }
}
