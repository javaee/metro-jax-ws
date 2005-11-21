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
package com.sun.xml.ws.spi.runtime;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


/**
 * Captures many transports that are used to talk with WS endpoints.
 * 
 * For endpoints deployed in light weight http server in J2SE, the implemenation
 * of this class uses HttpTransaction to read from or write to stream.
 *
 * For endpoints deployed in servlet container, the implementation of this
 * class uses HttpServletRequest to read a request, and uses HttpServletResponse
 * to write response.
 *
 * This also works for local transport, JMS transport.
 *
 * Runtime can access to the implementation of this interface using
 * messageInfo.getConnection()
 * 
 */

public interface WSConnection {
    
    public static final int OK=200;
    public static final int ONEWAY=202;
    public static final int UNSUPPORTED_MEDIA=415;
    public static final int MALFORMED_XML=400;
    public static final int INTERNAL_ERR=500;
    
    /**
     * returns transport headers
     * @return transport headers
     */
    public Map<String,List<String>> getHeaders();
    
    /**
     * sets transport headers
     */
    public void setHeaders(Map<String,List<String>> headers);
    
    /**
     * sets the transport status code like <code>OK</code>
     */
    public void setStatus(int status);
    
    /**
     * @return return the status code
     */
    public int getStatus();
    
    /**
     * Transport's underlying input stream
     * @return Transport's underlying input stream
     */
    public InputStream getInput();
    
    /**
     * Closes transport's input stream
     */
    public void closeInput();
    
    /**
     * Transport's underlying output stream
     * @return Transport's underlying output stream
     */
    public OutputStream getOutput();
    
    /**
     * Closes transport's output stream
     */
    public void closeOutput();
    
    public OutputStream getDebug();
    
    /**
     * Closes transport connection
     */
    public void close();
    
}
