/*
 * $Id: EndpointDocInfo.java,v 1.1 2005-08-27 02:54:28 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.server.DocContext;
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.wsdl.parser.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class EndpointDocInfo implements DocInfo {
    private String resource;
    private String queryString;
    private DocContext docContext;
    private byte[] buf;

    public EndpointDocInfo(String resource, byte[] buf) {
        this.resource = resource;
        this.buf = buf;
        this.docContext = null;
    }
    
    public InputStream getDoc() {
        return new ByteArrayInputStream(buf);
    }
    
    public String getPath() {
        return resource;
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    
    public DocContext getDocContext() {
        return docContext;
    }
    
    public void setDocType(DOC_TYPE docType) {
        
    }
    
    public DOC_TYPE getDocType() {
        return null;
    }

    public void setTargetNamespace(String ns) {
        
    }
    
    public String getTargetNamespace() {
        return null;
    }
    
    public void setService(Service service) {
        
    }
    
    public Service getService() {
        return null;
    }
    
    public void setPortType(boolean portType) {
        
    }
    
    public boolean hasPortType() {
        return false;
    }
    
}
