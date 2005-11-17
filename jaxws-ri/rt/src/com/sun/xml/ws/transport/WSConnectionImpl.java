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

package com.sun.xml.ws.transport;

import com.sun.xml.ws.pept.ept.EPTFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.pept.transport.Connection;
import com.sun.xml.ws.spi.runtime.WSConnection;

/**
 * Abstract class for WSConnection. All client-side and server-side
 * transports should extend this class and override appropriate methods.
 *
 * @author WS Development Team
 */
public abstract class WSConnectionImpl implements WSConnection, Connection {
    Map<String, List<String>> headers = null;
    public OutputStream debugStream = null;
    public OutputStream outputStream = null;
    public InputStream inputStream = null;
    int statusCode;
    
    /** Creates a new instance of WSConnectionImpl */
    public WSConnectionImpl () {
    }

    public int getStatus () {
        return statusCode;
    }

    public void setStatus (int statusCode) {
        this.statusCode = statusCode;
    }

    public OutputStream getDebug () {
        return debugStream;
    }

    /**
     * @return outputStream
     * 
     * Returns the OutputStream on which the outbound message is written.
     * Any stream or connection initialization, pre-processing is done here.
     */
    public OutputStream getOutput() {
        return outputStream;
    }
    
    /**
     * @return inputStream
     *
     * Returns the InputStream on which the inbound message is received.
     * Any post-processing of message is done here.
     */
    public InputStream getInput() {
        return inputStream;
    }

    public Map<String, List<String>> getHeaders () {
        return headers;
    }
    
    public void setHeaders (Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public EPTFactory getEPTFactory () {
        throw new UnsupportedOperationException();
    }

    public ByteBuffer readUntilEnd () {
        throw new UnsupportedOperationException();
    }

    public void write (ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException();
    }

    public int read (ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Write connection headers in HTTP syntax using \r\n as a
     * separator.
     */
    public void writeHeaders(OutputStream os) {
        try {
            byte[] newLine = "\r\n".getBytes("us-ascii");

            // Write all headers ala HTTP (only first list entry serialized)
            Map<String, List<String>> headers = getHeaders();
            for (String header : headers.keySet()) {
                os.write((header + ":" + 
                    headers.get(header).get(0)).getBytes("us-ascii"));
                os.write(newLine);
            }

            // Write empty line as in HTTP
            os.write(newLine);
        }
        catch (Exception ex) {
            throw new ClientTransportException("local.client.failed",ex);
        }
    } 

    /**
     * Read and consume connection headers in HTTP syntax using 
     * \r\n as a separator.
     */
    public void readHeaders(InputStream is) {
        try {
            int c1, c2;
            StringBuffer line = new StringBuffer();
            
            if (headers == null) {
                headers = new HashMap<String, List<String>>();
            }
            else {
                headers.clear();            
            }
            
            // Read headers until finding a \r\n line
            while ((c1 = is.read()) != -1) {         
                if (c1 == '\r') {
                    c2 = is.read();
                    assert c2 != -1;

                    if (c2 == '\n') {
                        String s = line.toString();
                        if (s.length() == 0) {
                            break;  // found \r\n line
                        }
                        else {
                            int k  = s.indexOf(':');
                            assert k > 0;
                            ArrayList<String> value = new ArrayList<String>();
                            value.add(s.substring(k + 1));
                            headers.put(s.substring(0, k), value); 
                            line.setLength(0);      // clear line buffer
                        }
                    }
                    else {
                        line.append((char) c1).append((char) c2);   
                    }
                }
                else {
                    line.append((char) c1);
                }                
            }
        }
        catch (Exception ex) {
            throw new ClientTransportException("local.client.failed",ex);
        }            
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
    }
    
    public void close() {
        
    }
}
