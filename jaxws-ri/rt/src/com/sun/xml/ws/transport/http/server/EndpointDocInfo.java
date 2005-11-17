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
package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.wsdl.parser.Service;
import com.sun.xml.ws.util.ByteArrayBuffer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

public class EndpointDocInfo implements DocInfo {
    private URL resourceUrl;
    private String queryString;
    private ByteArrayBuffer buf;
    private DOC_TYPE docType;
    private String tns;
    private Service service;
    private boolean portType;

    public EndpointDocInfo(URL resourceUrl, ByteArrayBuffer buf) {
        this.resourceUrl = resourceUrl;
        this.buf = buf;
    }
    
    public InputStream getDoc() {
        return buf.newInputStream();
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
