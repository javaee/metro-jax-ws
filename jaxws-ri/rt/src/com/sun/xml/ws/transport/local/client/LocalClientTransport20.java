/*
 * $Id: LocalClientTransport20.java,v 1.1 2005-05-23 23:02:26 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.local.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.SOAPMessage;

import com.sun.xml.ws.client.ClientTransport;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;
import com.sun.xml.ws.spi.runtime.JaxrpcConnection;
import com.sun.xml.ws.transport.local.server.LocalConnectionImpl;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author JAX-RPC Development Team
 */
public class LocalClientTransport20 implements ClientTransport {

    private RuntimeEndpointInfo endpointInfo;
    private OutputStream logStream;
    private Tie tie = new Tie();
    
    
    //this class is used primarily for debugging purposes
    public LocalClientTransport20(RuntimeEndpointInfo endpointInfo) {
        this(endpointInfo, null);
    }

    public LocalClientTransport20(RuntimeEndpointInfo endpointInfo,
        OutputStream logStream) {
        this.endpointInfo = endpointInfo;
        this.logStream = logStream;
    }

    public void invoke(String endpoint, SOAPMessageContext context) {
        try {
            if (logStream != null) {
                String s = "\n******************\nRequest\n";
                logStream.write(s.getBytes());
                displayMimeHeaders(context);
                context.getMessage().writeTo(logStream);
                logStream.write("\n".getBytes());
            }

            setSOAPMessageFromSAAJ(context);
            
            JaxrpcConnection con = new LocalConnectionImpl(context, null);
            tie.handle(con, endpointInfo);

            setSOAPMessageFromSAAJ(context);

            // set this because a sender cannot rely on it being set
            // automatically
            context.setFailure(false);

            if (logStream != null) {
                String s = "\nResponse\n";
                logStream.write(s.getBytes());
                displayMimeHeaders(context);
                if (context.getMessage() != null) {
                    context.getMessage().writeTo(logStream);
                } else {
                    logStream.write("NULL".getBytes());
                }
                s = "\n******************\n\n";
                logStream.write(s.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof Localizable) {
                throw new ClientTransportException("local.client.failed",
                        (Localizable) e);
            } else {
                throw new ClientTransportException("local.client.failed",
                        new LocalizableExceptionAdapter(e));
            }
        }
    }

    /**
     * Set the SOAPMessage in the context using stream thru SAAJ, this is
     * to closely match http behaviour
     *
     * @param context
     */
    private void setSOAPMessageFromSAAJ(SOAPMessageContext context) throws Exception {
        if (context.getMessage() != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            context.getMessage().writeTo(os);

            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

            SOAPMessage message =
                    context.createMessage(context.getMessage().getMimeHeaders(),
                            is);
            context.setMessage(message);
        }
    }

    private void displayMimeHeaders(SOAPMessageContext context) throws Exception {
        for (Iterator iter = context.getMessage().getMimeHeaders()
                .getAllHeaders(); iter.hasNext();) {
            MimeHeader header = (MimeHeader) iter.next();
            String s = header.getName() + ": " + header.getValue() + "\n";
            logStream.write(s.getBytes());
        }
    }

    public void invokeOneWay(String endpoint, SOAPMessageContext context) {
        try {
            if (logStream != null) {
                String s = "\n******************\nOneway Request\n";
                logStream.write(s.getBytes());
                context.getMessage().writeTo(logStream);
                logStream.write("\n".getBytes());
            }

            JaxrpcConnection con = new LocalConnectionImpl(context, null);
            tie.handle(con, endpointInfo);

            // set this because a sender cannot rely on it being set
            // automatically
            context.setFailure(false);

        } catch (Exception e) {
            if (e instanceof Localizable) {
                throw new ClientTransportException("local.client.failed",
                        (Localizable) e);
            } else {
                throw new ClientTransportException("local.client.failed",
                        new LocalizableExceptionAdapter(e));
            }
        }
    }


}
