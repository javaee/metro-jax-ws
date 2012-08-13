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

package com.sun.xml.ws.dump;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.commons.xmlutil.Converter;
import com.sun.xml.ws.dump.MessageDumper.MessageType;
import com.sun.xml.ws.dump.MessageDumper.ProcessingState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class MessageDumpingTube extends AbstractFilterTubeImpl {
    static final String DEFAULT_MSGDUMP_LOGGING_ROOT = com.sun.xml.ws.util.Constants.LoggingDomain + ".messagedump";
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    //
    private final MessageDumper messageDumper;
    private final int tubeId;
    //
    private final MessageDumpingFeature messageDumpingFeature;
    /**
     * @param name
     *      Specify the name that identifies this {@link MessageDumpingTube}
     *      instance. This string will be printed when this pipe
     *      dumps messages, and allows people to distinguish which
     *      pipe instance is dumping a message when multiple
     *      {@link com.sun.xml.ws.util.pipe.DumpTube}s print messages out.
     * @param out
     *      The output to send dumps to.
     * @param next
     *      The next {@link com.sun.xml.ws.api.pipe.Tube} in the pipeline.
     */
    MessageDumpingTube(Tube next, MessageDumpingFeature feature) {
        super(next);

        this.messageDumpingFeature = feature;
        this.tubeId = ID_GENERATOR.incrementAndGet();
        this.messageDumper = new MessageDumper(
                "MesageDumpingTube",
                java.util.logging.Logger.getLogger(feature.getMessageLoggingRoot()),
                feature.getMessageLoggingLevel());
    }

    /**
     * Copy constructor.
     */
    MessageDumpingTube(MessageDumpingTube that, TubeCloner cloner) {
        super(that, cloner);


        this.messageDumpingFeature = that.messageDumpingFeature;
        this.tubeId = ID_GENERATOR.incrementAndGet();
        this.messageDumper = that.messageDumper;
    }

    public MessageDumpingTube copy(TubeCloner cloner) {
        return new MessageDumpingTube(this, cloner);
    }

    @Override
    public NextAction processRequest(Packet request) {
        dump(MessageType.Request, Converter.toString(request), Fiber.current().owner.id);
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        dump(MessageType.Response, Converter.toString(response), Fiber.current().owner.id);
        return super.processResponse(response);
    }

    @Override
    public NextAction processException(Throwable t) {
        dump(MessageType.Exception, Converter.toString(t), Fiber.current().owner.id);

        return super.processException(t);
    }

    protected final void dump(MessageType messageType, String message, String engineId) {
        String logMessage;
        if (messageDumpingFeature.getMessageLoggingStatus()) {
            messageDumper.setLoggingLevel(messageDumpingFeature.getMessageLoggingLevel());
            logMessage = messageDumper.dump(messageType, ProcessingState.Received, message, tubeId, engineId);
        } else {
            logMessage = messageDumper.createLogMessage(messageType, ProcessingState.Received, tubeId, engineId, message);
        }
        messageDumpingFeature.offerMessage(logMessage);
    }
}

