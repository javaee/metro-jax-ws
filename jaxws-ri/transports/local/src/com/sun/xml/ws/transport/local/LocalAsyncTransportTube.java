/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.transport.local;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.ContentNegotiation;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Transport {@link Tube} that routes a message to a service that runs within it.
 *
 * <p>
 * This is useful to test the whole client-server in a single VM.
 *
 * @author Jitendra Kotamraju
 */
final class LocalAsyncTransportTube extends AbstractTubeImpl {

    /**
     * Represents the service running inside the local transport.
     *
     * We use {@link HttpAdapter}, so that the local transport
     * excercise as much server code as possible. If this were
     * to be done "correctly" we should write our own {@link Adapter}
     * for the local transport.
     */
    private final HttpAdapter adapter;

    private final Codec codec;

    /**
     * The address of the endpoint deployed in this tube.
     */
    private final URI baseURI;

    // per-pipe reusable resources.
    // we don't really have to reuse anything since this isn't designed for performance,
    // but nevertheless we do it as an experiement.
    private final Map<String, List<String>> reqHeaders = new HashMap<String, List<String>>();

    public LocalAsyncTransportTube(URI baseURI, WSEndpoint endpoint, Codec codec) {
        this(baseURI,HttpAdapter.createAlone(endpoint),codec);
    }

    private LocalAsyncTransportTube(URI baseURI,HttpAdapter adapter, Codec codec) {
        this.adapter = adapter;
        this.codec = codec;
        this.baseURI = baseURI;
        assert codec !=null && adapter!=null;
    }

    /**
     * Copy constructor for {@link Tube#copy(TubeCloner)}.
     */
    private LocalAsyncTransportTube(LocalAsyncTransportTube that, TubeCloner cloner) {
        this(that.baseURI, that.adapter, that.codec.copy());
        cloner.add(that,this);
    }

    public @NotNull NextAction processException(@NotNull Throwable t) {
        return doThrow(t);
    }

    private class MyClosedCallback implements ClosedCallback {
        final Fiber fiber;
        LocalConnectionImpl con;
        String requestContentType;
        String requestAccept;
        final Packet request;

        MyClosedCallback(Packet request) {

            this.request = request;
            this.requestContentType = requestContentType;
            this.requestAccept = requestAccept;
            fiber = Fiber.current();
        }

        void setConnection(LocalConnectionImpl con) {
            this.con = con;
        }

        void setContentType(ContentType ct) {
            requestContentType = ct.getContentType();
            requestAccept = ct.getAcceptHeader();
        }

        public void onClosed() {
            String responseContentType = getResponseContentType(con);

            if (con.getStatus() == WSHTTPConnection.ONEWAY) {
                Packet reply = request.createClientResponse(null);    // one way. no response given.
                fiber.resume(reply);
                return;
            }

            // TODO: check if returned MIME type is the same as that which was sent
            // or is acceptable if an Accept header was used

            checkFIConnegIntegrity(request.contentNegotiation, requestContentType, requestAccept, responseContentType);

            Packet reply = request.createClientResponse(null);
            try {
                codec.decode(con.getInput(), responseContentType, reply);
            } catch(Exception e) {
                e.printStackTrace();
            }

            fiber.resume(reply);
        }
    }

    private void checkFIConnegIntegrity(ContentNegotiation conneg,
                                        String requestContentType, String requestAccept, String responseContentType) {
        requestAccept = (requestAccept == null) ? "" : requestAccept;
        if (requestContentType.contains("fastinfoset")) {
            if (!responseContentType.contains("fastinfoset")) {
                throw new RuntimeException(
                        "Request is encoded using Fast Infoset but response (" +
                        responseContentType +
                        ") is not");
            } else if (conneg == ContentNegotiation.none) {
                throw new RuntimeException(
                        "Request is encoded but Fast Infoset content negotiation is set to none");
            }
        } else if (requestAccept.contains("fastinfoset")) {
            if (!responseContentType.contains("fastinfoset")) {
                throw new RuntimeException(
                        "Fast Infoset is acceptable but response is not encoded in Fast Infoset");
            } else if (conneg == ContentNegotiation.none) {
                throw new RuntimeException(
                        "Fast Infoset is acceptable but Fast Infoset content negotiation is set to none");
            }
        } else if (conneg == ContentNegotiation.pessimistic) {
            throw new RuntimeException(
                    "Content negotitaion is set to pessimistic but Fast Infoset is not acceptable");
        } else if (conneg == ContentNegotiation.optimistic) {
            throw new RuntimeException(
                    "Content negotitaion is set to optimistic but the request (" +
                    requestContentType +
                    ") is not encoded using Fast Infoset");
        }
    }

    private String getResponseContentType(LocalConnectionImpl con) {
        Map<String, List<String>> rsph = con.getResponseHeaders();
        if(rsph!=null) {
            List<String> c = rsph.get("Content-Type");
            if(c!=null && !c.isEmpty())
                return c.get(0);
        }
        return null;
    }

    @NotNull
    public NextAction processRequest(@NotNull Packet request) {
        try {
        // Set up WSConnection with tranport headers, request content

        // get transport headers from message
        reqHeaders.clear();
        Map<String, List<String>> rh = (Map<String, List<String>>) request.invocationProperties.get(MessageContext.HTTP_REQUEST_HEADERS);
        //assign empty map if its null
        if(rh != null){
            reqHeaders.putAll(rh);
        }

        MyClosedCallback callback = new MyClosedCallback(request);
        LocalConnectionImpl con = new LocalConnectionImpl(baseURI,reqHeaders,callback);
        callback.setConnection(con);
        // Calling getStaticContentType sets some internal state in the codec
        // TODO : need to fix this properly in Codec
        ContentType contentType = codec.getStaticContentType(request);
        String requestContentType;
        if (contentType != null) {
            requestContentType = contentType.getContentType();
            codec.encode(request, con.getOutput());
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            contentType = codec.encode(request, baos);
            requestContentType = contentType.getContentType();
            baos.writeTo(con.getOutput());
        }
        callback.setContentType(contentType);
        reqHeaders.put("Content-Type", Collections.singletonList(requestContentType));

        String requestAccept = contentType.getAcceptHeader();
        if (contentType.getAcceptHeader() != null) {
            reqHeaders.put("Accept", Collections.singletonList(requestAccept));
        }

        if(dump)
            dump(con,"request",reqHeaders);

        //Packet serverReq = adapter.decodePacket(con, codec);
        adapter.invokeAsync(con);
        return doSuspend();
        } catch(IOException ioe) {
            throw new WebServiceException(ioe);
        }
    }

    @NotNull
    public NextAction processResponse(@NotNull Packet response) {
        return doReturnWith(response);
    }

    public void preDestroy() {
        // Nothing to do here. Intenionally left empty
    }

    public LocalAsyncTransportTube copy(TubeCloner cloner) {
        return new LocalAsyncTransportTube(this, cloner);
    }

    private void dump(LocalConnectionImpl con, String caption, Map<String,List<String>> headers) {
        System.out.println("---["+caption +"]---");
        if(headers!=null) {
            for (Entry<String,List<String>> header : headers.entrySet()) {
                if(header.getValue().isEmpty()) {
                    // I don't think this is legal, but let's just dump it,
                    // as the point of the dump is to uncover problems.
                    System.out.println(header.getValue());
                } else {
                    for (String value : header.getValue()) {
                        System.out.println(header.getKey()+": "+value);
                    }
                }
            }
        }
        System.out.println(con.toString());
        System.out.println("--------------------");
    }

    /**
     * Dumps what goes across HTTP transport.
     */
    private static final boolean dump;

    static {
        boolean b;
        try {
            b = Boolean.getBoolean(LocalTransportTube.class.getName()+".dump");
        } catch( Throwable t ) {
            b = false;
        }
        dump = b;
    }

    private static final boolean async;
    static {
        boolean b;
        try {
            b = Boolean.getBoolean(LocalTransportTube.class.getName()+".async");
        } catch( Throwable t ) {
            b = false;
        }
        async = b;
    }
}
