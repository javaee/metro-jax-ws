/*
 * $Id: ServletDocContext.java,v 1.2 2005-06-03 20:48:36 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.server.DocContext;
import java.net.URL;
import javax.servlet.ServletContext;

public class ServletDocContext implements DocContext {
    
    private ServletContext context;
    
    public ServletDocContext(ServletContext context) {
        this.context = context;
    }
    
    public String getAbsolutePath(String abs, String rel) {
        String path = null;
        try {
            URL absUrl = context.getResource(abs);
            URL url = new URL(absUrl, rel);
            String file = url.getPath();
            int index = file.lastIndexOf("/WEB-INF/wsdl");
            if (index != -1) {
                path = file.substring(index);
            } else {
                // Do we throw error ?
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "/WEB-INF/wsdl/"+rel;
    }
    
}
