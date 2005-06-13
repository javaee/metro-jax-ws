/*
 * $Id: ServletConnectionImpl.java,v 1.2 2005-06-13 20:21:25 jitu Exp $
 */

/*
* Copyright (c) 2005 Sun Microsystems, Inc.
* All rights reserved.
*/

package com.sun.xml.ws.transport.http.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.pept.ept.EPTFactory;
import com.sun.xml.ws.spi.runtime.JaxrpcConnection.STATUS;
import com.sun.xml.ws.spi.runtime.JaxrpcConnection;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author JAX-RPC RI Development Team
 */
public class ServletConnectionImpl implements JaxrpcConnection {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private STATUS status;
    private Map<String,List<String>> requestHeaders;
    private Map<String,List<String>> responseHeaders;

    public ServletConnectionImpl(HttpServletRequest request,
        HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    // TODO: correct the logic
    public int read(ByteBuffer byteBuffer) {
        return -1;
    }

    public void write(ByteBuffer byteBuffer) {

    }
    
    public EPTFactory getEPTFactory() {
        // TODO
        return null;
    }

    /*
     * @see com.sun.pept.transport.Connection#readUntilEnd()
     */
    public ByteBuffer readUntilEnd() {

        return null;
    }

    protected static byte[] readFully(InputStream istream) throws IOException {

        return null;
    }
    
    public Map<String,List<String>> getHeaders() {
        if (requestHeaders == null) {
            requestHeaders = new HashMap<String, List<String>>();
            Enumeration enums = request.getHeaderNames();
            while (enums.hasMoreElements()) {
                String headerName = (String) enums.nextElement();
                String headerValue = request.getHeader(headerName);
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
    
    /**
     * sets HTTP status code
     */
    private int getStatusCode() {
        switch(status) {
            case OK :
                return HttpURLConnection.HTTP_OK;
            case ONEWAY :
                return HttpURLConnection.HTTP_ACCEPTED;
            case UNSUPPORTED_MEDIA :
                return HttpURLConnection.HTTP_UNSUPPORTED_TYPE;
            case MALFORMED_XML :
                return HttpURLConnection.HTTP_BAD_REQUEST;
            case INTERNAL_ERR :
                return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        return HttpURLConnection.HTTP_OK;
    }
    
    public InputStream getInput() {
        try {
            return request.getInputStream();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
    
    public OutputStream getOutput() {
        // write HTTP status code, and headers
        response.setStatus(getStatusCode());
        if (responseHeaders != null) {
            for(Map.Entry <String, List<String>> entry : responseHeaders.entrySet()) {
                String name = entry.getKey();
                List<String> values = entry.getValue();
                for(String value : values) {
                    response.setHeader(name, value);
                }
            }
        }
        try {
            return response.getOutputStream();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

}
