/*
 * $Id: ServletDocInfo.java,v 1.8 2005-10-26 02:20:35 jitu Exp $
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

import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.wsdl.parser.Service;
import java.io.InputStream;
import java.net.URL;
import javax.servlet.ServletContext;

public class ServletDocInfo implements DocInfo {
    private ServletContext context;
    private String resource;
    private String queryString;
    private DOC_TYPE docType;
    private Service service;
    private boolean hasPortType;
    private String tns;

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
    
    public void setDocType(DOC_TYPE docType) {
        this.docType = docType;
    }
    
    public DOC_TYPE getDocType() {
        return docType;
    }

    public void setTargetNamespace(String ns) {
        this.tns = ns;
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
