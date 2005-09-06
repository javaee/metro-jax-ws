/*
 * $Id: ServerConnectionImpl.java,v 1.9 2005-09-06 23:51:30 jitu Exp $
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
import com.sun.net.httpserver.HttpExchange;

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

    private HttpExchange httpTransaction;
    private int status;
    private Map<String,List<String>> requestHeaders;
    private Map<String,List<String>> responseHeaders;
    private NoCloseInputStream is;
    private NoCloseOutputStream out;

    public ServerConnectionImpl(HttpExchange httpTransaction) {
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
            is = new NoCloseInputStream(httpTransaction.getRequestBody());
        }
        return is;
    }
    
    public OutputStream getOutput() {
        if (out == null) {
            try {
                closeInput();
                
                if (responseHeaders != null) {
                    for(Map.Entry <String, List<String>> entry : responseHeaders.entrySet()) {
                        String name = entry.getKey();
                        List<String> values = entry.getValue();
                        for(String value : values) {
                            httpTransaction.getResponseHeaders().add(name, value);
                        }
                    }
                }

                // write HTTP status code, and headers
                httpTransaction.sendResponseHeaders(getStatus(), 0);
                out = new NoCloseOutputStream(httpTransaction.getResponseBody());
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return out;
    }
    
    public void closeOutput() {
        if (out != null) {
            try {                 
                out.getOutputStream().close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        out = null;
    }
    
    public void closeInput() {
        if (is != null) {
            try {
                // Read everything from request and close it
                byte[] buf = new byte[1024];
                while (is.read(buf) != -1) {
                }             
                is.getInputStream().close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        is = null;
    }
    
    private static class NoCloseInputStream extends InputStream {
        private InputStream is;
        
        public NoCloseInputStream(InputStream is) {
            this.is = is;
        }
        
        @Override
        public int read() throws IOException {
            return is.read();
        }

        @Override
        public void close() throws IOException {           
            // Intentionally left empty. use closeInput() to close
        }
        
        public InputStream getInputStream() {
            return is;
        }
    }
    
    private static class NoCloseOutputStream extends OutputStream {
        private OutputStream out;
        
        public NoCloseOutputStream(OutputStream out) {
            this.out = out;
        }
        
        @Override
        public void write(int ch) throws IOException {
            out.write(ch);
        }

        @Override
        public void close() throws IOException {         
            // Intentionally left empty. use closeOutput() to close
        }
        
        public OutputStream getOutputStream() {
            return out;
        }
    }

}
