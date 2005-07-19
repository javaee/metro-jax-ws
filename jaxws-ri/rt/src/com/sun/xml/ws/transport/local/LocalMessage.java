/*
 * $Id: LocalMessage.java,v 1.1 2005-07-19 18:10:06 arungupta Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.local;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author WS Development Team
 */
public class LocalMessage {
    ByteArrayOutputStream baos;
    Map<String, List<String>> headers;
    
    public LocalMessage () {
    }
    
    public ByteArrayOutputStream getOutput() {
        return baos;
    }
    
    public void setOutput(ByteArrayOutputStream baos) {
        this.baos = baos;
    }
    
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
    
}
