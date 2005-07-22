/*
 * $Id: LocalClientTransport.java,v 1.3 2005-07-22 23:04:29 arungupta Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.local.client;

import com.sun.xml.messaging.saaj.util.ByteInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MimeHeaders;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.binding.soap.BindingImpl;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.transport.WSConnectionImpl;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.localization.Localizable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.soap.MimeHeader;
import com.sun.xml.ws.transport.local.server.LocalConnectionImpl;
import com.sun.xml.ws.transport.local.LocalMessage;

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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        lm.setOutput(baos);

        return lm.getOutput();
    }
    
    @Override
    public void closeOutput() {
        super.closeOutput();
        WSConnection con = new LocalConnectionImpl(lm);
     
        try {
            tie.handle(con, endpointInfo);
        } catch (Exception ex) {
            if (ex instanceof Localizable) {
                throw new ClientTransportException("local.client.failed",
                        (Localizable) ex);
            } else {
                throw new ClientTransportException("local.client.failed",
                        new LocalizableExceptionAdapter(ex));
            }
        }
    }
    
    @Override
    public InputStream getInput() {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(lm.getOutput().toByteArray ());
            
            return bis;
        } catch (Exception ex) {
            if (ex instanceof Localizable) {
                throw new ClientTransportException("local.client.failed",
                        (Localizable) ex);
            } else {
                throw new ClientTransportException("local.client.failed",
                        new LocalizableExceptionAdapter(ex));
            }
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
