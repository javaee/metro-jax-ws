/*
 * $Id: ServletDocInfo.java,v 1.4 2005-08-30 02:13:31 jitu Exp $
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
