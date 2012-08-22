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

package com.sun.xml.ws.transport.async_client_transport;

import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.istack.NotNull;

import javax.xml.stream.XMLStreamWriter;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Rama.Pulavarthi@sun.com
 */
public class AsyncClientTransportTube extends AbstractFilterTubeImpl {
    WSBinding binding;
    NonAnonymousResponsesReceiver<Message> responseReceiver;
    RINonAnonymousResponseHandler nonAnonHandler;
    RequestSender requestSender;
    AddressingVersion addrVersion;
    WSEndpointReference nonAnonymousEPR;
    Header nonAnonymousHeader;


    protected AsyncClientTransportTube(ClientTubeAssemblerContext context) {
        super(TransportTubeFactory.create(Thread.currentThread().getContextClassLoader(), recreateClientContext(context)));
        this.binding = context.getBinding();
        AddressingVersion addrVersion = binding.getAddressingVersion();
        AsyncClientTransportFeature nonanonftr = binding.getFeature(AsyncClientTransportFeature.class);
        if (addrVersion != null && nonanonftr.isEnabled()) {
            if (nonanonftr.getReceiver() == null) {
                responseReceiver = new DefaultNonAnonymousResponseReceiver(nonanonftr.getNonanonAddress(), binding.getBindingID());

            } else {
                responseReceiver = nonanonftr.getReceiver();
            }
        }
        nonAnonHandler = new RINonAnonymousResponseHandler();
        responseReceiver.register(nonAnonHandler);
        requestSender = new RequestSender(toString(), next);
        nonAnonymousEPR = new WSEndpointReference(responseReceiver.getAddress(), binding.getAddressingVersion());
        nonAnonymousHeader = nonAnonymousEPR.createHeader(binding.getAddressingVersion().replyToTag);

    }

    protected AsyncClientTransportTube(AsyncClientTransportTube that, TubeCloner cloner) {
        super(that, cloner);
        this.binding = that.binding;
        this.nonAnonHandler = that.nonAnonHandler;
        this.responseReceiver = that.responseReceiver;
        this.requestSender = that.requestSender;
        this.nonAnonymousEPR = that.nonAnonymousEPR;
        this.nonAnonymousHeader = that.nonAnonymousHeader;
    }


    private static ClientTubeAssemblerContext recreateClientContext(ClientTubeAssemblerContext context) {
        return new ClientTubeAssemblerContext(
                context.getAddress(), context.getWsdlModel(), context.getBindingProvider(),
                recreateBinding(context.getBinding()),
                context.getContainer(), context.getCodec(), context.getSEIModel());
    }

    private static WSBinding recreateBinding(WSBinding binding) {
        //return new FeatureSupressingWSBinding(AsyncClientTransportFeature.class, binding);
        return BindingImpl.create(binding.getBindingId(), new FeatureSupressingWSBinding(AsyncClientTransportFeature.class,
                binding).getFeatures().toArray());
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new AsyncClientTransportTube(this, cloner);
    }

    public
    @NotNull
    NextAction processRequest(Packet request) {
        if (request.expectReply) {
            setNonAnnonymousReplyTo(request.getMessage(), binding.getAddressingVersion(), nonAnonymousHeader);
            String msgId = getMessageId(request.getMessage());
            nonAnonHandler.addNonAnonymousResponseHandler(msgId, new ClientResponseHandler(request));
            LOGGER.fine("Sending request with message id" + msgId);
            //requestSender.sendAsync(request, new SyncResponseHandler(msgId, nonAnonHandler));
            requestSender.send(request);
            return doSuspend();
        } else {
            //oneway, continue as usual
            return doInvoke(next, request);
        }

    }

    public
    @NotNull
    NextAction processResponse(Packet response) {
        return doReturnWith(response);
    }

    public
    @NotNull
    NextAction processException(Throwable t) {
        return doThrow(t);
    }

    public void preDestroy() {
        responseReceiver.unregister(nonAnonHandler);
        requestSender.close();
        nonAnonHandler.cleanUp();
        nonAnonHandler = null;
        responseReceiver = null;
        requestSender = null;

    }

    String getMessageId(Message m) {
        return m.getHeaders().getMessageID(binding.getAddressingVersion(), binding.getSOAPVersion());
    }

    String getRelatesTo(Message m) {
        return m.getHeaders().getRelatesTo(binding.getAddressingVersion(), binding.getSOAPVersion());
    }

    public class ClientResponseHandler implements NonAnonymousResponseHandler<Message> {
        final Fiber fiber;
        final Packet request;

        public ClientResponseHandler(Packet request) {
            this.request = request;
            this.fiber = Fiber.current();

        }


        public void onReceive(@NotNull Message msg) {
            LOGGER.info("Client being resumed for processing message with id" + getRelatesTo(msg));
            try {
                if (dump) {
                    System.out.println("Received message: ");
                    ByteArrayBuffer baos = new ByteArrayBuffer();
                    XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
                    msg.copy().writeTo(writer);
                    writer.close();
                    baos.writeTo(System.out);
                    System.out.flush();
                }
            } catch (Exception e) {
                onError(e);
            }
            Packet reply = request.createClientResponse(msg);
            fiber.resume(reply);
        }

        public void onError(@NotNull Throwable t) {
            fiber.resume(t);
        }

    }

    class RINonAnonymousResponseHandler implements NonAnonymousResponseHandler<Message> {
        Map<String, NonAnonymousResponseHandler> waiting = Collections.synchronizedMap(
                new HashMap<String, NonAnonymousResponseHandler>());

        public void addNonAnonymousResponseHandler(String msgId, NonAnonymousResponseHandler handler) {
            waiting.put(msgId, handler);
        }

        public NonAnonymousResponseHandler remove(String msgId) {
            return waiting.remove(msgId);
        }

        public void cleanUp() {
            waiting.clear();
        }

        public void onReceive(final @NotNull Message response) {
            String msgId = getRelatesTo(response);
            LOGGER.fine("Received message with id" + msgId);
            if (msgId != null) {
                final NonAnonymousResponseHandler handler = waiting.remove(msgId);
                if (handler == null) {
                    LOGGER.warning("Received unexpected message with realtesTo id = " + msgId);
                } else {
                    handler.onReceive(response);
                    /*
                    Thread clientResponseHandlerThread = new Thread(new Runnable() {

                        public void run() {
                            handler.onReceive(response);
                        }
                    }, "Client ResponseHadler Thread for handling response for message id" + msgId);

                    clientResponseHandlerThread.start();
                    */
                }
            } else {
                LOGGER.warning("Received unexpected message - cannot find key");
            }
        }

        public void onError(@NotNull Throwable t) {
            // no op
        }

    }


    class SyncResponseHandler implements Fiber.CompletionCallback {
        private final String msgId;
        private RINonAnonymousResponseHandler nonAnonResponseTracker;

        SyncResponseHandler(String msgId, RINonAnonymousResponseHandler nonAnonResponseTracker) {
            this.msgId = msgId;
            this.nonAnonResponseTracker = nonAnonResponseTracker;
        }

        public void onCompletion(@NotNull Packet response) {
            Message responseMessage = response.getMessage();
            if (responseMessage != null) {
                if (responseMessage.hasPayload()) {
                    String relatesToId = getRelatesTo(responseMessage);
                    if (!msgId.equals(relatesToId)) {
                        LOGGER.warning("Received unexpected message for id = " + msgId + "with id = " + getMessageId(responseMessage));
                    }
                    NonAnonymousResponseHandler responseHandler = nonAnonResponseTracker.remove(msgId);
                    if (responseHandler != null) {
                        responseHandler.onReceive(responseMessage);
                    }

                }
            }
        }

        public void onCompletion(@NotNull Throwable error) {
            LOGGER.warning("Received unexpected error for request with id = " + msgId);

            NonAnonymousResponseHandler responseHandler = nonAnonResponseTracker.remove(msgId);
            if (responseHandler != null) {
                responseHandler.onError(error);
            }
        }
    }

    void setNonAnnonymousReplyTo(Message m, AddressingVersion av, Header nonAnonymousHeader) {
        HeaderList headers = m.getHeaders();
        headers.remove(av.replyToTag);
        headers.add(nonAnonymousHeader);

        if (headers.remove(av.faultToTag) != null) {
            headers.add(nonAnonymousHeader);
        }

    }

    /**
     * Dumps what goes across NonAnonymousResponseTube.
     */
    public static boolean dump;

    static {
        boolean b;
        try {
            b = Boolean.getBoolean(AsyncClientTransportTube.class.getName() + ".dump");
        } catch (Throwable t) {
            b = false;
        }
        dump = b;
    }

    private static final Logger LOGGER = Logger.getLogger(AsyncClientTransportTube.class.getName());
}
