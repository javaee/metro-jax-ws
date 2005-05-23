/*
 * $Id: LocalConnectionImpl.java,v 1.1 2005-05-23 23:02:26 bbissett Exp $
 */

/*
* Copyright (c) 2005 Sun Microsystems, Inc.
* All rights reserved.
*/

package com.sun.xml.ws.transport.local.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.server.SOAPConnection;
import com.sun.xml.ws.spi.runtime.MessageContext;
import com.sun.xml.ws.spi.runtime.JaxrpcConnection;
import com.sun.xml.ws.transport.http.server.MessageContextProperties;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.xml.transform.stream.StreamSource;

/**
 * @author JAX-RPC RI Development Team
 */
public class LocalConnectionImpl implements JaxrpcConnection, SOAPConnection
{
    private SOAPMessageContext context;
    private EPTFactory eptFactory;
    private Map<String,List<String>> requestHeaders;
    private Map<String,List<String>> responseHeaders;
    private STATUS status;

    public LocalConnectionImpl(MessageContext context,
        EPTFactory eptFactory) {
        this.context = (SOAPMessageContext)context;
        this.eptFactory = eptFactory;
    }

    public EPTFactory getEPTFactory() {
        return eptFactory;
    }

    public int read(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException();
    }

    public void write(ByteBuffer byteBuffer) {
        try {
            MimeHeaders mhs = new MimeHeaders();
            if (responseHeaders != null) {
                for(Map.Entry <String, List<String>> entry : responseHeaders.entrySet()) {
                    String name = entry.getKey();
                    List<String> values = entry.getValue();
                    for(String value : values) {
                        mhs.addHeader(name, value);
                    }
                }
            }
            byte[] data = byteBuffer.array();
            int offset = byteBuffer.arrayOffset();
            int length = byteBuffer.limit()-byteBuffer.position();
            ByteInputStream bis = new ByteInputStream(data, offset, length);
            SOAPMessage msg = context.createMessage(mhs, bis);
            context.setMessage(msg);
        } catch(IOException ie) {
            ie.printStackTrace();
        }
    }

    /*
     * @see com.sun.pept.transport.Connection#readUntilEnd()
     */
    public ByteBuffer readUntilEnd() {
        try {
            SOAPMessage msg = context.getMessage();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            msg.writeTo(bos);
            byte[] data = bos.toByteArray();
            return ByteBuffer.wrap(data, 0, data.length);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }
    
    public Map<String,List<String>> getHeaders() {
        if (requestHeaders == null) {
            requestHeaders = new HashMap<String, List<String>>();
            MimeHeaders mhs = context.getMessage().getMimeHeaders();
            Iterator i = mhs.getAllHeaders();
            while (i.hasNext()) {
                MimeHeader mh = (MimeHeader) i.next();
                String headerName = mh.getName();
                String headerValue = mh.getValue();
                List<String> values = requestHeaders.get(headerName);
                if (values == null) {
                    values = new ArrayList<String>();
                    requestHeaders.put(headerName, values);
                }
                values.add(headerValue);
            }
        }
        return requestHeaders;
    }
    
    /**
     * sets response headers.
     */
    public void setHeaders(Map<String,List<String>> headers) {
        responseHeaders = headers;
    }
    
    public void setStatus(STATUS status) {
        this.status = status;
    }
    
    
    
    public SOAPMessage getSOAPMessage() {
        return context.getMessage();
    }
        
    public void sendResponse(SOAPMessage soapMessage) {
        context.setMessage(soapMessage);
    }
            
    public void sendResponseOneway() {
        context.put(MessageContextProperties.ONE_WAY_OPERATION, "true");
    }
                
    public void sendResponseError() {
        throw new UnsupportedOperationException();
    }
                    
    public SOAPMessage getSOAPMessage(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

}
