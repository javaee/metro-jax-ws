/*
 * $Id: ServletDocInfo.java,v 1.2 2005-06-03 20:48:36 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.server.DocContext;
import com.sun.xml.ws.server.DocInfo;
import java.io.InputStream;
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
    
    public String getQueryString() {
        return queryString;
    }
    
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    
    public DocContext getDocContext() {
        return docContext;
    }
    
}
