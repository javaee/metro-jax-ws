/*
 * $Id: ServletDocContext.java,v 1.3 2005-09-10 19:48:09 kohsuke Exp $
 *
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
