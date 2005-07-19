/*
 * $Id: WSConnectionImpl.java,v 1.1 2005-07-19 18:10:04 arungupta Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport;

import com.sun.pept.ept.EPTFactory;
import com.sun.xml.ws.spi.runtime.WSConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for WSConnection. All client-side and server-side
 * transports should extend this class and override appropriate methods.
 *
 * @author WS Development Team
 */
public abstract class WSConnectionImpl implements WSConnection {
    Map<String, List<String>> headers = null;
    public OutputStream debugStream = null;
    public OutputStream outputStream = null;
    public InputStream inputStream = null;
    int statusCode;
    STATUS status;
    
    /** Creates a new instance of WSConnectionImpl */
    public WSConnectionImpl () {
    }

    public int getStatus () {
        return statusCode;
    }

    public void setStatus (int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatus (STATUS status) {
        this.status = status;
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
}
