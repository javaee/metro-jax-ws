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

package com.sun.xml.ws.transport.local.client;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.handler.MessageContextImpl;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.transport.WSConnectionImpl;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.ByteArrayBuffer;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import com.sun.xml.ws.transport.local.server.LocalConnectionImpl;
import com.sun.xml.ws.transport.local.LocalMessage;

import static com.sun.xml.ws.developer.JAXWSProperties.CONTENT_NEGOTIATION_PROPERTY;

import javax.xml.ws.http.HTTPException;

/**
 * @author WS Development Team
 */
public class LocalClientTransport extends WSConnectionImpl {

    private RuntimeEndpointInfo endpointInfo;
    private Tie tie = new Tie();
    LocalMessage lm = new LocalMessage();

    //this class is used primarily for debugging purposes
    public LocalClientTransport(RuntimeEndpointInfo endpointInfo) {
        this(endpointInfo, null);
    }

    public LocalClientTransport(RuntimeEndpointInfo endpointInfo,
                                OutputStream logStream) {
        this.endpointInfo = endpointInfo;
        debugStream = logStream;
    }


    @Override
    public OutputStream getOutput() {
        try {
            lm.setOutput(new ByteArrayBuffer());
            return lm.getOutput();
        }
        catch (Exception ex) {
            throw new ClientTransportException("local.client.failed",ex);
        }
    }

    private static void checkMessageContentType(WSConnection con, boolean response) {
        String negotiation = System.getProperty(CONTENT_NEGOTIATION_PROPERTY, "none").intern();
        String contentType = con.getHeaders().get("Content-Type").get(0);

        // Use indexOf() to handle Multipart/related types
        if (negotiation == "none") {
            // OK only if XML
            if (contentType.indexOf("text/xml") < 0 &&
                   contentType.indexOf("application/soap+xml") < 0 &&
                   contentType.indexOf("application/xop+xml") < 0)
            {
                throw new RuntimeException("Invalid content type '" + contentType
                    + "' with content negotiation set to '" + negotiation + "'.");
            }
        }
        else if (negotiation == "optimistic") {
            // OK only if FI
            if (contentType.indexOf("application/fastinfoset") < 0 &&
                   contentType.indexOf("application/soap+fastinfoset") < 0)
            {
                throw new RuntimeException("Invalid content type '" + contentType
                    + "' with content negotiation set to '" + negotiation + "'.");
            }
        }
        else if (negotiation == "pessimistic") {
            // OK if FI request is anything and response is FI
            if (response &&
                    contentType.indexOf("application/fastinfoset") < 0 &&
                    contentType.indexOf("application/soap+fastinfoset") < 0)
            {
                throw new RuntimeException("Invalid content type '" + contentType
                    + "' with content negotiation set to '" + negotiation + "'.");
            }
        }
    }

    @Override
    public void closeOutput() {
        super.closeOutput();
        WSConnection con = new LocalConnectionImpl(lm);

        // Copy headers for content negotiation
        con.setHeaders(getHeaders());

        // Check request content type based on negotiation property
        checkMessageContentType(this, false);

        try {
            // Set a MessageContext per invocation
            WebServiceContext wsContext = endpointInfo.getWebServiceContext();
            wsContext.setMessageContext(new MessageContextImpl());
            tie.handle(con, endpointInfo);

            checkMessageContentType(con, true);
        }
        catch (Exception ex) {
            new ProtocolException("Server side Exception:" + ex);
        }
    }

    @Override
    public InputStream getInput() {
        try {
            return lm.getOutput().newInputStream();
        }
        catch (Exception ex) {
            throw new ClientTransportException("local.client.failed",ex);
        }
    }

    @Override
    public void setHeaders(Map<String, List<String>> headers) {
        lm.setHeaders(headers);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return lm.getHeaders();
    }

}
