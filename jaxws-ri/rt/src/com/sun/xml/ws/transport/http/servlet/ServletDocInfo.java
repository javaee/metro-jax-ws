/*
 * $Id: ServletDocInfo.java,v 1.1 2005-05-26 18:21:17 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.server.DocInfo;
import java.io.InputStream;
import javax.servlet.ServletContext;

public class ServletDocInfo implements DocInfo {
    private ServletContext context;
    private String resource;
    private String queryString;

    public ServletDocInfo(ServletContext context, String resource) {
        this.context = context;
        this.resource = resource;
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
    
}
