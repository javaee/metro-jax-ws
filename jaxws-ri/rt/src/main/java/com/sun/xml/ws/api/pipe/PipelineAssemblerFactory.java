/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.api.pipe;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.util.ServiceFinder;

import javax.xml.ws.soap.SOAPBinding;
import java.util.logging.Logger;

/**
 * Creates {@link PipelineAssembler}.
 *
 * <p>
 * To create a pipeline,
 * the JAX-WS runtime locates {@link PipelineAssemblerFactory}s through
 * the <tt>META-INF/services/com.sun.xml.ws.api.pipe.PipelineAssemblerFactory</tt> files.
 * Factories found are checked to see if it supports the given binding ID one by one,
 * and the first valid {@link PipelineAssembler} returned will be used to create
 * a pipeline.
 *
 * <p>
 * TODO: is bindingId really extensible? for this to be extensible,
 * someone seems to need to hook into WSDL parsing.
 *
 * <p>
 * TODO: JAX-WSA might not define its own binding ID -- it may just go to an extension element
 * of WSDL. So this abstraction might need to be worked on.
 *
 * @author Kohsuke Kawaguchi
 * @deprecated
 *      Use {@link TubelineAssemblerFactory} instead.
 */
public abstract class PipelineAssemblerFactory {
    /**
     * Creates a {@link PipelineAssembler} applicable for the given binding ID.
     *
     * @param bindingId
     *      The binding ID for which a pipeline will be created,
     *      such as {@link SOAPBinding#SOAP11HTTP_BINDING}.
     *      Must not be null.
     *
     * @return
     *      null if this factory doesn't recognize the given binding ID.
     */
    public abstract PipelineAssembler doCreate(BindingID bindingId);

    /**
     * Locates {@link PipelineAssemblerFactory}s and create
     * a suitable {@link PipelineAssembler}.
     *
     * @param bindingId
     *      The binding ID string for which the new {@link PipelineAssembler}
     *      is created. Must not be null.
     * @return
     *      Always non-null, since we fall back to our default {@link PipelineAssembler}.
     */
    public static PipelineAssembler create(ClassLoader classLoader, BindingID bindingId) {
        for (PipelineAssemblerFactory factory : ServiceFinder.find(PipelineAssemblerFactory.class,classLoader)) {
            PipelineAssembler assembler = factory.doCreate(bindingId);
            if(assembler!=null) {
                logger.fine(factory.getClass()+" successfully created "+assembler);
                return assembler;
            }
        }

        // default binding IDs that are known
        // TODO: replace this with proper ones
        return new com.sun.xml.ws.util.pipe.StandalonePipeAssembler();
    }

    private static final Logger logger = Logger.getLogger(PipelineAssemblerFactory.class.getName());
}
