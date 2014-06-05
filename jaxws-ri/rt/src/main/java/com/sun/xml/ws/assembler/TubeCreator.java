/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.assembler;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.assembler.dev.ClientTubelineAssemblyContext;
import com.sun.xml.ws.assembler.dev.TubeFactory;
import com.sun.xml.ws.assembler.dev.TubelineAssemblyContextUpdater;
import com.sun.xml.ws.resources.TubelineassemblyMessages;
import com.sun.xml.ws.runtime.config.TubeFactoryConfig;

/**
 * Utility class that encapsulates logic of loading TubeFactory
 * instances and creating Tube instances.
 *
 * @author m_potociar
 */
final class TubeCreator {
    private static final Logger LOGGER = Logger.getLogger(TubeCreator.class);
    private final TubeFactory factory;
    private final String msgDumpPropertyBase;

    TubeCreator(TubeFactoryConfig config, ClassLoader tubeFactoryClassLoader) {
        String className = config.getClassName();
        try {
            Class<?> factoryClass;
            if (isJDKInternal(className)) {
                factoryClass = Class.forName(className, true, TubeCreator.class.getClassLoader());
            } else {
                factoryClass = Class.forName(className, true, tubeFactoryClassLoader);
            }
            if (TubeFactory.class.isAssignableFrom(factoryClass)) {
                // We can suppress "unchecked" warning here as we are checking for the correct type in the if statement above
                @SuppressWarnings("unchecked")
                Class<TubeFactory> typedClass = (Class<TubeFactory>) factoryClass;
                this.factory = typedClass.newInstance();
                this.msgDumpPropertyBase = this.factory.getClass().getName() + ".dump";
            } else {
                throw new RuntimeException(TubelineassemblyMessages.MASM_0015_CLASS_DOES_NOT_IMPLEMENT_INTERFACE(factoryClass.getName(), TubeFactory.class.getName()));
            }
        } catch (InstantiationException ex) {
            throw LOGGER.logSevereException(new RuntimeException(TubelineassemblyMessages.MASM_0016_UNABLE_TO_INSTANTIATE_TUBE_FACTORY(className), ex), true);
        } catch (IllegalAccessException ex) {
            throw LOGGER.logSevereException(new RuntimeException(TubelineassemblyMessages.MASM_0016_UNABLE_TO_INSTANTIATE_TUBE_FACTORY(className), ex), true);
        } catch (ClassNotFoundException ex) {
            throw LOGGER.logSevereException(new RuntimeException(TubelineassemblyMessages.MASM_0017_UNABLE_TO_LOAD_TUBE_FACTORY_CLASS(className), ex), true);
        }
    }

    Tube createTube(DefaultClientTubelineAssemblyContext context) {
        // TODO implement passing init parameters (if any) to the factory
        return factory.createTube(context);
    }

    Tube createTube(DefaultServerTubelineAssemblyContext context) {
        // TODO implement passing init parameters (if any) to the factory
        return factory.createTube(context);
    }

    void updateContext(ClientTubelineAssemblyContext context) {
        if (factory instanceof TubelineAssemblyContextUpdater) {
            ((TubelineAssemblyContextUpdater) factory).prepareContext(context);
        }
    }

    void updateContext(DefaultServerTubelineAssemblyContext context) {
        if (factory instanceof TubelineAssemblyContextUpdater) {
            ((TubelineAssemblyContextUpdater) factory).prepareContext(context);
        }
    }

    String getMessageDumpPropertyBase() {
        return msgDumpPropertyBase;
    }

    private boolean isJDKInternal(String className) {
        // avoid repackaging
        return className.startsWith("com." + "sun.xml.internal.ws");
    }

}
