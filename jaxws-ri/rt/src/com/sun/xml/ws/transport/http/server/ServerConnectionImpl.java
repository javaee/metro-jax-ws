/*
 * $Id: ServerConnectionImpl.java,v 1.13 2005-10-06 02:25:03 jitu Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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

    private HttpExchange httpExchange;
    private int status;
    private Map<String,List<String>> requestHeaders;
    private Map<String,List<String>> responseHeaders;
    private NoCloseInputStream is;
    private NoCloseOutputStream out;
    private boolean closedInput;
    private boolean closedOutput;

    public ServerConnectionImpl(HttpExchange httpTransaction) {
        this.httpExchange = httpTransaction;
    }

    public Map<String,List<String>> getHeaders() {
        return httpExchange.getRequestHeaders();
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
            status = HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        return status;
    }
    
    public InputStream getInput() {
        if (is == null) {
            is = new NoCloseInputStream(httpExchange.getRequestBody());
        }
        return is;
    }
    
    public OutputStream getOutput() {
        if (out == null) {
            try {
                closeInput();
                int len = 0;
                if (responseHeaders != null) {
                    for(Map.Entry <String, List<String>> entry : responseHeaders.entrySet()) {
                        String name = entry.getKey();
                        List<String> values = entry.getValue();
                        if (name.equals("Content-Length")) {
                            // No need to add this header
                            len = Integer.valueOf(values.get(0));
                        } else {
                            for(String value : values) {
                                httpExchange.getResponseHeaders().add(name, value);
                            }
                        }
                    }
                }

                // write HTTP status code, and headers
                httpExchange.sendResponseHeaders(getStatus(), len);
                out = new NoCloseOutputStream(httpExchange.getResponseBody());
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
                closedOutput = true;
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
                closedInput = true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        is = null;
    }
    
    public void close() {
        try {
            if (!closedInput) {
                if (is == null) {
                    getInput();
                }
                closeInput();
            }
            if (!closedOutput) {
                if (out == null) {
                    getOutput();    
                }
                closeOutput();
            }
        } finally {
            try {
                httpExchange.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
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

        @Override
        public int read(byte b[]) throws IOException {
            return is.read(b);
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            return is.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return is.skip(n);
        }

        @Override
        public int available() throws IOException {
            return is.available();
        }

        @Override
        public void mark(int readlimit) {
            is.mark(readlimit);
        }


        @Override
        public void reset() throws IOException {
            is.reset();
        }

        @Override
        public boolean markSupported() {
            return is.markSupported();
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
        
        @Override
        public void write(byte b[]) throws IOException {
            out.write(b);
        }
        
        @Override
        public void write(byte b[], int off, int len) throws IOException {
            out.write(b, off, len);
        }
        
        @Override
        public void flush() throws IOException {
            out.flush();
        }
        
        public OutputStream getOutputStream() {
            return out;
        }
    }

}
