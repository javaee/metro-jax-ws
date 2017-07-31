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

package com.sun.xml.ws.client;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.handler.HandlerChainsModel;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;

import javax.jws.HandlerChain;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used by {@link WSServiceDelegate} to configure {@link BindingImpl}
 * with handlers. The two mechanisms encapsulated by this abstraction
 * is {@link HandlerChain} annotaion and {@link HandlerResolver}
 * interface.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class HandlerConfigurator {
    /**
     * Configures the given {@link BindingImpl} object by adding handlers to it.
     */
    abstract void configureHandlers(@NotNull WSPortInfo port, @NotNull BindingImpl binding);

    /**
     * Returns a {@link HandlerResolver}, if this object encapsulates any {@link HandlerResolver}.
     * Otherwise null.
     */
    abstract HandlerResolver getResolver();


    /**
     * Configures handlers by calling {@link HandlerResolver}.
     * <p>
     * When a null {@link HandlerResolver} is set by the user to
     * {@link Service#setHandlerResolver(HandlerResolver)}, we'll use this object
     * with null {@link #resolver}.
     */
    static final class HandlerResolverImpl extends HandlerConfigurator {
        private final @Nullable HandlerResolver resolver;

        public HandlerResolverImpl(HandlerResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        void configureHandlers(@NotNull WSPortInfo port, @NotNull BindingImpl binding) {
            if (resolver!=null) {
                binding.setHandlerChain(resolver.getHandlerChain(port));
            }
        }


        @Override
        HandlerResolver getResolver() {
            return resolver;
        }
    }

    /**
     * Configures handlers from {@link HandlerChain} annotation.
     *
     * <p>
     * This class is a simple
     * map of PortInfo objects to handler chains. It is used by a
     * {@link WSServiceDelegate} object, and can
     * be replaced by user code with a different class implementing
     * HandlerResolver. This class is only used on the client side, and
     * it includes a lot of logging to help when there are issues since
     * it deals with port names, service names, and bindings. All three
     * must match when getting a handler chain from the map.
     *
     * <p>It is created by the {@link WSServiceDelegate}
     * class , which uses {@link HandlerAnnotationProcessor} to create
     * a handler chain and then it sets the chains on this class and they
     * are put into the map. The ServiceContext uses the map to set handler
     * chains on bindings when they are created.
     */
    static final class AnnotationConfigurator extends HandlerConfigurator {
        private final HandlerChainsModel handlerModel;
        private final Map<WSPortInfo,HandlerAnnotationInfo> chainMap = new HashMap<WSPortInfo,HandlerAnnotationInfo>();
        private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".handler");

        AnnotationConfigurator(WSServiceDelegate delegate) {
            handlerModel = HandlerAnnotationProcessor.buildHandlerChainsModel(delegate.getServiceClass());
            assert handlerModel!=null; // this class is suppeod to be called only when there's @HandlerCHain
        }


        void configureHandlers(WSPortInfo port, BindingImpl binding) {
            //Check in cache first
            HandlerAnnotationInfo chain = chainMap.get(port);

            if(chain==null) {
                logGetChain(port);
                // Put it in cache
                chain = handlerModel.getHandlersForPortInfo(port);
                chainMap.put(port,chain);
            }

            if (binding instanceof SOAPBinding) {
                ((SOAPBinding) binding).setRoles(chain.getRoles());
            }

            logSetChain(port,chain);
            binding.setHandlerChain(chain.getHandlers());
        }

        HandlerResolver getResolver() {
            return new HandlerResolver() {
                public List<Handler> getHandlerChain(PortInfo portInfo) {
                    return new ArrayList<Handler>(
                        handlerModel.getHandlersForPortInfo(portInfo).getHandlers());
                }
            };
        }
        // logged at finer level
        private void logSetChain(WSPortInfo info, HandlerAnnotationInfo chain) {
            logger.finer("Setting chain of length " + chain.getHandlers().size() +
                " for port info");
            logPortInfo(info, Level.FINER);
        }

        // logged at fine level
        private void logGetChain(WSPortInfo info) {
            logger.fine("No handler chain found for port info:");
            logPortInfo(info, Level.FINE);
            logger.fine("Existing handler chains:");
            if (chainMap.isEmpty()) {
                logger.fine("none");
            } else {
                for (WSPortInfo key : chainMap.keySet()) {
                    logger.fine(chainMap.get(key).getHandlers().size() +
                        " handlers for port info ");
                    logPortInfo(key, Level.FINE);
                }
            }
        }

        private void logPortInfo(WSPortInfo info, Level level) {
            logger.log(level, "binding: " + info.getBindingID() +
                "\nservice: " + info.getServiceName() +
                "\nport: " + info.getPortName());
        }
    }
}
