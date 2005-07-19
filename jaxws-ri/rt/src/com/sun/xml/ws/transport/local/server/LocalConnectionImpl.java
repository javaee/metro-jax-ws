/*
 * $Id: LocalConnectionImpl.java,v 1.5 2005-07-19 18:10:07 arungupta Exp $
 */

/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.transport.local.server;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import com.sun.xml.ws.transport.WSConnectionImpl;
import com.sun.xml.ws.transport.local.LocalMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author WS Development Team
 *
 * Server-side Local transport implementation
 */
public class LocalConnectionImpl extends WSConnectionImpl {
    private int status;
    private LocalMessage lm;
    
    public LocalConnectionImpl (LocalMessage localMessage) {
        this.lm = localMessage;
    }
    
    public Map<String,List<String>> getHeaders () {
        return lm.getHeaders ();
    }
    
    /**
     * sets response headers.
     */
    public void setHeaders (Map<String,List<String>> headers) {
        lm.setHeaders (headers);
    }
    
    public void setStatus (int status) {
        this.status = status;
    }
    
    public InputStream getInput () {
        ByteArrayInputStream bis = new ByteArrayInputStream (lm.getOutput ().toByteArray ());
        
        return bis;
    }
    
    public OutputStream getOutput () {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        
        lm.setOutput (baos);
        
        return baos;
    }
}

