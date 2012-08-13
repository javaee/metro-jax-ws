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
package com.sun.xml.ws.assembler;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.assembler.dev.TubelineAssemblyDecorator;
import com.sun.xml.ws.dump.LoggingDumpTube;
import com.sun.xml.ws.resources.TubelineassemblyMessages;
import com.sun.xml.ws.util.ServiceFinder;

import java.util.Collection;
import java.util.logging.Level;

/**
* TODO: Write some description here ...
*
* @author Miroslav Kos (miroslav.kos at oracle.com)
*/
public class MetroTubelineAssembler implements TubelineAssembler {

    private static final String COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE = "com.sun.metro.soap.dump";
    public static final MetroConfigNameImpl JAXWS_TUBES_CONFIG_NAMES = new MetroConfigNameImpl("jaxws-tubes-default.xml", "jaxws-tubes.xml");

    private static enum Side {

        Client("client"),
        Endpoint("endpoint");
        private final String name;

        private Side(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class MessageDumpingInfo {

        final boolean dumpBefore;
        final boolean dumpAfter;
        final Level logLevel;

        MessageDumpingInfo(boolean dumpBefore, boolean dumpAfter, Level logLevel) {
            this.dumpBefore = dumpBefore;
            this.dumpAfter = dumpAfter;
            this.logLevel = logLevel;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(MetroTubelineAssembler.class);
    private final BindingID bindingId;
    private final TubelineAssemblyController tubelineAssemblyController;

    public MetroTubelineAssembler(final BindingID bindingId, MetroConfigName metroConfigName) {
        this.bindingId = bindingId;
        this.tubelineAssemblyController = new TubelineAssemblyController(metroConfigName);
    }

    TubelineAssemblyController getTubelineAssemblyController() {
        return tubelineAssemblyController;
    }

    @NotNull
    public Tube createClient(@NotNull ClientTubeAssemblerContext jaxwsContext) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Assembling client-side tubeline for WS endpoint: " + jaxwsContext.getAddress().getURI().toString());
        }

        DefaultClientTubelineAssemblyContext context = createClientContext(jaxwsContext);

        Collection<TubeCreator> tubeCreators = tubelineAssemblyController.getTubeCreators(context);

        for (TubeCreator tubeCreator : tubeCreators) {
            tubeCreator.updateContext(context);
        }

        TubelineAssemblyDecorator decorator = TubelineAssemblyDecorator.composite(
                ServiceFinder.find(TubelineAssemblyDecorator.class, context.getContainer()));
        
        boolean first = true;
        for (TubeCreator tubeCreator : tubeCreators) {
            final MessageDumpingInfo msgDumpInfo = setupMessageDumping(tubeCreator.getMessageDumpPropertyBase(), Side.Client);

            final Tube oldTubelineHead = context.getTubelineHead();
            LoggingDumpTube afterDumpTube = null;
            if (msgDumpInfo.dumpAfter) {
                afterDumpTube = new LoggingDumpTube(msgDumpInfo.logLevel, LoggingDumpTube.Position.After, context.getTubelineHead());
                context.setTubelineHead(afterDumpTube);
            }

            if (!context.setTubelineHead(decorator.decorateClient(tubeCreator.createTube(context), context))) { // no new tube has been created
                if (afterDumpTube != null) {
                    context.setTubelineHead(oldTubelineHead); // removing possible "after" message dumping tube
                }
            } else {
                final String loggedTubeName = context.getTubelineHead().getClass().getName();
                if (afterDumpTube != null) {
                    afterDumpTube.setLoggedTubeName(loggedTubeName);
                }

                if (msgDumpInfo.dumpBefore) {
                    final LoggingDumpTube beforeDumpTube = new LoggingDumpTube(msgDumpInfo.logLevel, LoggingDumpTube.Position.Before, context.getTubelineHead());
                    beforeDumpTube.setLoggedTubeName(loggedTubeName);
                    context.setTubelineHead(beforeDumpTube);
                }
            }
            
            if (first) {
                context.setTubelineHead(decorator.decorateClientTail(context.getTubelineHead(), context));
                first = false;
            }
        }

        return decorator.decorateClientHead(context.getTubelineHead(), context);
    }

    @NotNull
    public Tube createServer(@NotNull ServerTubeAssemblerContext jaxwsContext) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Assembling endpoint tubeline for WS endpoint: " + jaxwsContext.getEndpoint().getServiceName() + "::" + jaxwsContext.getEndpoint().getPortName());
        }

        DefaultServerTubelineAssemblyContext context = createServerContext(jaxwsContext);

        // FIXME endpoint URI for provider case
        Collection<TubeCreator> tubeCreators = tubelineAssemblyController.getTubeCreators(context);
        for (TubeCreator tubeCreator : tubeCreators) {
            tubeCreator.updateContext(context);
        }

        TubelineAssemblyDecorator decorator = TubelineAssemblyDecorator.composite(
                ServiceFinder.find(TubelineAssemblyDecorator.class, context.getEndpoint().getContainer()));
        
        boolean first = true;
        for (TubeCreator tubeCreator : tubeCreators) {
            final MessageDumpingInfo msgDumpInfo = setupMessageDumping(tubeCreator.getMessageDumpPropertyBase(), Side.Endpoint);

            final Tube oldTubelineHead = context.getTubelineHead();
            LoggingDumpTube afterDumpTube = null;
            if (msgDumpInfo.dumpAfter) {
                afterDumpTube = new LoggingDumpTube(msgDumpInfo.logLevel, LoggingDumpTube.Position.After, context.getTubelineHead());
                context.setTubelineHead(afterDumpTube);
            }

            if (!context.setTubelineHead(decorator.decorateServer(tubeCreator.createTube(context), context))) { // no new tube has been created
                if (afterDumpTube != null) {
                    context.setTubelineHead(oldTubelineHead); // removing possible "after" message dumping tube
                }
            } else {
                final String loggedTubeName = context.getTubelineHead().getClass().getName();
                if (afterDumpTube != null) {
                    afterDumpTube.setLoggedTubeName(loggedTubeName);
                }

                if (msgDumpInfo.dumpBefore) {
                    final LoggingDumpTube beforeDumpTube = new LoggingDumpTube(msgDumpInfo.logLevel, LoggingDumpTube.Position.Before, context.getTubelineHead());
                    beforeDumpTube.setLoggedTubeName(loggedTubeName);
                    context.setTubelineHead(beforeDumpTube);
                }
            }
            
            if (first) {
                context.setTubelineHead(decorator.decorateServerTail(context.getTubelineHead(), context));
                first = false;
            }
        }

        return decorator.decorateServerHead(context.getTubelineHead(), context);
    }

    private MessageDumpingInfo setupMessageDumping(String msgDumpSystemPropertyBase, Side side) {
        boolean dumpBefore = false;
        boolean dumpAfter = false;
        Level logLevel = Level.INFO;

        // checking common properties
        Boolean value = getBooleanValue(COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE);
        if (value != null) {
            dumpBefore = value.booleanValue();
            dumpAfter = value.booleanValue();
        }

        value = getBooleanValue(COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE + ".before");
        dumpBefore = (value != null) ? value.booleanValue() : dumpBefore;

        value = getBooleanValue(COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE + ".after");
        dumpAfter = (value != null) ? value.booleanValue() : dumpAfter;

        Level levelValue = getLevelValue(COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE + ".level");
        if (levelValue != null) {
            logLevel = levelValue;
        }

        // narrowing to proper communication side on common properties
        value = getBooleanValue(COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE + "." + side.toString());
        if (value != null) {
            dumpBefore = value.booleanValue();
            dumpAfter = value.booleanValue();
        }

        value = getBooleanValue(COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE + "." + side.toString() + ".before");
        dumpBefore = (value != null) ? value.booleanValue() : dumpBefore;

        value = getBooleanValue(COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE + "." + side.toString() + ".after");
        dumpAfter = (value != null) ? value.booleanValue() : dumpAfter;

        levelValue = getLevelValue(COMMON_MESSAGE_DUMP_SYSTEM_PROPERTY_BASE + "." + side.toString() + ".level");
        if (levelValue != null) {
            logLevel = levelValue;
        }


        // checking general tube-specific properties
        value = getBooleanValue(msgDumpSystemPropertyBase);
        if (value != null) {
            dumpBefore = value.booleanValue();
            dumpAfter = value.booleanValue();
        }

        value = getBooleanValue(msgDumpSystemPropertyBase + ".before");
        dumpBefore = (value != null) ? value.booleanValue() : dumpBefore;

        value = getBooleanValue(msgDumpSystemPropertyBase + ".after");
        dumpAfter = (value != null) ? value.booleanValue() : dumpAfter;

        levelValue = getLevelValue(msgDumpSystemPropertyBase + ".level");
        if (levelValue != null) {
            logLevel = levelValue;
        }

        // narrowing to proper communication side on tube-specific properties
        msgDumpSystemPropertyBase += "." + side.toString();

        value = getBooleanValue(msgDumpSystemPropertyBase);
        if (value != null) {
            dumpBefore = value.booleanValue();
            dumpAfter = value.booleanValue();
        }

        value = getBooleanValue(msgDumpSystemPropertyBase + ".before");
        dumpBefore = (value != null) ? value.booleanValue() : dumpBefore;

        value = getBooleanValue(msgDumpSystemPropertyBase + ".after");
        dumpAfter = (value != null) ? value.booleanValue() : dumpAfter;

        levelValue = getLevelValue(msgDumpSystemPropertyBase + ".level");
        if (levelValue != null) {
            logLevel = levelValue;
        }

        return new MessageDumpingInfo(dumpBefore, dumpAfter, logLevel);
    }

    private Boolean getBooleanValue(String propertyName) {
        Boolean retVal = null;

        String stringValue = System.getProperty(propertyName);
        if (stringValue != null) {
            retVal = Boolean.valueOf(stringValue);
            LOGGER.fine(TubelineassemblyMessages.MASM_0018_MSG_LOGGING_SYSTEM_PROPERTY_SET_TO_VALUE(propertyName, retVal));
        }

        return retVal;
    }

    private Level getLevelValue(String propertyName) {
        Level retVal = null;

        String stringValue = System.getProperty(propertyName);
        if (stringValue != null) {
            // if value is not null => property is set, we will try to override the default logging level
            LOGGER.fine(TubelineassemblyMessages.MASM_0018_MSG_LOGGING_SYSTEM_PROPERTY_SET_TO_VALUE(propertyName, stringValue));
            try {
                retVal = Level.parse(stringValue);
            } catch (IllegalArgumentException ex) {
                LOGGER.warning(TubelineassemblyMessages.MASM_0019_MSG_LOGGING_SYSTEM_PROPERTY_ILLEGAL_VALUE(propertyName, stringValue), ex);
            }
        }

        return retVal;
    }

    // Extension point to change Tubeline Assembly behaviour: override if necessary ...
    protected DefaultServerTubelineAssemblyContext createServerContext(ServerTubeAssemblerContext jaxwsContext) {
        return new DefaultServerTubelineAssemblyContext(jaxwsContext);
    }

    // Extension point to change Tubeline Assembly behaviour: override if necessary ...
    protected DefaultClientTubelineAssemblyContext createClientContext(ClientTubeAssemblerContext jaxwsContext) {
        return new DefaultClientTubelineAssemblyContext(jaxwsContext);
    }

}
