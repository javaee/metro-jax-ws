/**
 * $Id: WSConnection.java,v 1.7 2005-09-07 02:49:10 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import com.sun.pept.transport.Connection;
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

public interface WSConnection extends Connection {
    
    public static final int OK=200;
    public static final int ONEWAY=202;
    public static final int UNSUPPORTED_MEDIA=415;
    public static final int MALFORMED_XML=400;
    public static final int INTERNAL_ERR=500;
    
    /**
     * returns request headers
     */
    public Map<String,List<String>> getHeaders();
    
    /**
     * sets response headers
     */
    public void setHeaders(Map<String,List<String>> headers);
    
    public void setStatus(int status);
    
    public int getStatus();
    
    public InputStream getInput();
    
    public void closeInput();
    
    public OutputStream getOutput();
    
    public void closeOutput();
    
    public OutputStream getDebug();
    
    public void close();
    
}
