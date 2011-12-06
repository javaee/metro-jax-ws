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

package com.sun.xml.ws.developer;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.SEIModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.List;

/**
 * Factory to create {@link JAXBContext}.
 *
 * <p>
 * JAX-WS uses JAXB to perform databinding when you use the service endpoint interface, and normally
 * the JAX-WS RI drives JAXB and creates a necessary {@link JAXBContext} automatically.
 *
 * <p>
 * This annotation is a JAX-WS RI vendor-specific feature, which lets applications create {@link JAXBRIContext}
 * (which is the JAXB RI's {@link JAXBContext} implementation.)
 * Combined with the JAXB RI vendor extensions defined in {@link JAXBRIContext}, appliation can use this to
 * fine-tune how the databinding happens, such as by adding more classes to the binding context,
 * by controlling the namespace mappings, and so on.
 *
 * <p>
 * Applications should either use {@link UsesJAXBContextFeature} or {@link UsesJAXBContext} to instruct
 * the JAX-WS runtime to use a custom factory.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1.5
 */
public interface JAXBContextFactory {
    /**
     * Called by the JAX-WS runtime to create a {@link JAXBRIContext} for the given SEI.
     *
     * @param sei
     *      The {@link SEIModel} object being constructed. This object provides you access to
     *      what SEI is being processed, and therefore useful if you are writing a generic
     *      {@link JAXBContextFactory} that can work with arbitrary SEI classes.
     *
     * @param classesToBind
     *      List of classes that needs to be bound by JAXB. This value is computed according to
     *      the JAX-WS spec and given to you.
     *
     *      The calling JAX-WS runtime expects the returned {@link JAXBRIContext} to be capable of
     *      handling all these classes, but you can add more (which is more common), or remove some
     *      (if you know what you are doing.)
     *
     *      The callee is free to mutate this list.
     *
     * @param typeReferences
     *      List of {@link TypeReference}s, which is also a part of the input to the JAXB RI to control
     *      how the databinding happens. Most likely this will be just a pass-through to the
     *      {@link JAXBRIContext#newInstance} method.
     *
     * @return
     *      A non-null valid {@link JAXBRIContext} object.
     *
     * @throws JAXBException
     *      If the callee encounters a fatal problem and wants to abort the JAX-WS runtime processing
     *      of the given SEI, throw a {@link JAXBException}. This will cause the port instantiation
     *      to fail (if on client), or the application deployment to fail (if on server.)
     */
    @NotNull JAXBRIContext createJAXBContext(@NotNull SEIModel sei, @NotNull List<Class> classesToBind, @NotNull List<TypeReference> typeReferences) throws JAXBException;

    /**
     * The default implementation that creates {@link JAXBRIContext} according to the standard behavior.
     */
    public static final JAXBContextFactory DEFAULT = new JAXBContextFactory() {
        @NotNull
        public JAXBRIContext createJAXBContext(@NotNull SEIModel sei, @NotNull List<Class> classesToBind, @NotNull List<TypeReference> typeReferences) throws JAXBException {
            return JAXBRIContext.newInstance(classesToBind.toArray(new Class[classesToBind.size()]),
                    typeReferences, null, sei.getTargetNamespace(), false, null);
        }
    };
}
