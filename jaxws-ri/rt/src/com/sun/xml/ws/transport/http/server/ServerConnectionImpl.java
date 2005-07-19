/*
 * $Id: ServerConnectionImpl.java,v 1.5 2005-07-19 18:10:05 arungupta Exp $
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
import com.sun.xml.ws.spi.runtime.WSConnection.STATUS;
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
//    private OutputStream out;

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
    private int getStatusCode() {
//        switch(status) {
//            case STATUS.OK :
//                return HttpURLConnection.HTTP_OK;
//            case ONEWAY :
//                return HttpURLConnection.HTTP_ACCEPTED;
//            case UNSUPPORTED_MEDIA :
//                return HttpURLConnection.HTTP_UNSUPPORTED_TYPE;
//            case MALFORMED_XML :
//                return HttpURLConnection.HTTP_BAD_REQUEST;
//            case INTERNAL_ERR :
//                return HttpURLConnection.HTTP_INTERNAL_ERROR;
//        }
//        return HttpURLConnection.HTTP_OK;
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
                // Read everything from request and close it
                byte[] buf = new byte[1024];
                int num;
                while ((num = is.read(buf)) != -1) {
                }
                is.close();

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
                httpTransaction.sendResponseHeaders(getStatusCode(), 0);
                outputStream = httpTransaction.getResponseBody();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return outputStream;
    }

}
