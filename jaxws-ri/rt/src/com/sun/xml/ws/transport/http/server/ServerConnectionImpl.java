/*
 * $Id: ServerConnectionImpl.java,v 1.6 2005-09-04 02:18:41 jitu Exp $
 */

/*
* Copyright (c) 2005 Sun Microsystems, Inc.
* All rights reserved.
*/

package com.sun.xml.ws.transport.http.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.sun.pept.ept.EPTFactory;
import com.sun.xml.ws.transport.WSConnectionImpl;
import com.sun.net.httpserver.HttpInteraction;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <code>com.sun.xml.ws.spi.runtime.WSConnection</code> used with Java SE endpoints
 *
 * @author WS Development Team
 */
public class ServerConnectionImpl extends WSConnectionImpl {

    private HttpInteraction httpTransaction;
    private int status;
    private Map<String,List<String>> requestHeaders;
    private Map<String,List<String>> responseHeaders;
    private InputStream is;
    private OutputStream out;

    public ServerConnectionImpl(HttpInteraction httpTransaction) {
        this.httpTransaction = httpTransaction;
    }

    public Map<String,List<String>> getHeaders() {
        return httpTransaction.getRequestHeaders();
    }
    
    /**
     * sets response headers.
     */
    public void setHeaders(Map<String,List<String>> headers) {
        responseHeaders = headers;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    /**
     * sets HTTP status code
     */
    public int getStatus() {
        if (status == 0) {
            status = HttpURLConnection.HTTP_OK;
        }
        return status;
    }
    
    public InputStream getInput() {
        if (is == null) {
            is = httpTransaction.getRequestBody();
        }
        return is;
    }
    
    public OutputStream getOutput() {
        if (outputStream == null) {
            try {
                if (responseHeaders != null) {
                    for(Map.Entry <String, List<String>> entry : responseHeaders.entrySet()) {
                        String name = entry.getKey();
                        List<String> values = entry.getValue();
                        for(String value : values) {
                            httpTransaction.getResponseHeaders().addHeader(name, value);
                        }
                    }
                }

                // write HTTP status code, and headers
                httpTransaction.sendResponseHeaders(getStatus(), 0);
                outputStream = httpTransaction.getResponseBody();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return outputStream;
    }
    
    public void closeOutput() {
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void closeInput() {
        try {
            // Read everything from request and close it
            byte[] buf = new byte[1024];
            int num;
            while ((num = inputStream.read(buf)) != -1) {
            }
            is.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
