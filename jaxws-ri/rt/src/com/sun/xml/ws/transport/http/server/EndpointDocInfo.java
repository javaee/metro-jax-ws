/*
 * $Id: EndpointDocInfo.java,v 1.3 2005-08-31 15:51:15 jitu Exp $
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
import java.net.URL;

public class EndpointDocInfo implements DocInfo {
    private URL resourceUrl;
    private String queryString;
    private byte[] buf;
    private DOC_TYPE docType;
    private String tns;
    private Service service;
    private boolean portType;

    public EndpointDocInfo(URL resourceUrl, byte[] buf) {
        this.resourceUrl = resourceUrl;
        this.buf = buf;
    }
    
    public InputStream getDoc() {
        return new ByteArrayInputStream(buf);
    }
    
    public String getPath() {
        return null;
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    
    public DocContext getDocContext() {
        return null;
    }
    
    public void setDocType(DOC_TYPE docType) {
        this.docType = docType;
    }
    
    public DOC_TYPE getDocType() {
        return docType;
    }

    public void setTargetNamespace(String tns) {
        this.tns = tns;
    }
    
    public String getTargetNamespace() {
        return tns;
    }
    
    public void setService(Service service) {
        this.service = service;
    }
    
    public Service getService() {
        return service;
    }
    
    public void setPortType(boolean portType) {
        this.portType = portType;
    }
    
    public boolean hasPortType() {
        return portType;
    }
    
    public URL getUrl() {
        return resourceUrl;
    }
    
}
