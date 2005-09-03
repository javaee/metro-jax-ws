/*
 * $Id: ServletDocInfo.java,v 1.5 2005-09-03 02:10:34 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.server.DocContext;
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.wsdl.parser.Service;
import java.io.InputStream;
import java.net.URL;
import javax.servlet.ServletContext;

public class ServletDocInfo implements DocInfo {
    private ServletContext context;
    private String resource;
    private String queryString;
    private DocContext docContext;
    private DOC_TYPE docType;
    private Service service;
    private boolean hasPortType;
    private String tns;

    public ServletDocInfo(ServletContext context, String resource) {
        this.context = context;
        this.resource = resource;
        this.docContext = new ServletDocContext(context);
    }
    
    public InputStream getDoc() {
        return context.getResourceAsStream(resource);
    }
    
    public String getPath() {
        return resource;
    }
    
    public URL getUrl() {
        try {
            return context.getResource(resource);
        } catch(Exception e) {
            return null;
        }
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
        this.docType = docType;
    }
    
    public DOC_TYPE getDocType() {
        return docType;
    }

    public void setTargetNamespace(String ns) {
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
        this.hasPortType = portType;
    }
    
    public boolean hasPortType() {
        return hasPortType;
    }
    
}
