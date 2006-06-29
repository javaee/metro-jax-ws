/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.server;
import com.sun.xml.ws.wsdl.parser.Service;
import java.io.InputStream;
import java.net.URL;


public interface DocInfo {
    
    public enum DOC_TYPE { WSDL, SCHEMA, OTHER };
    
    /*
     * The implemenation needs to work for multiple invocations of this method
     */
    public InputStream getDoc();
    
    /*
     * @return wsdl=a, xsd=c etc
     */
    public String getQueryString();
    
    /*
     * set wsdl=a, xsd=c etc as queryString
     */
    public void setQueryString(String queryString);
    
    /*
     * Sets document type : WSDL, or Schema ?
     */
    public void setDocType(DOC_TYPE docType);
    
    /*
     * return document type : WSDL, or Schema ?
     */
    public DOC_TYPE getDocType();
    
    /*
     * Sets targetNamespace of WSDL, and schema
     */
    public void setTargetNamespace(String ns);
    
    /*
     * Sets targetNamespace of WSDL, and schema
     */
    public String getTargetNamespace();
    
    /*
     * Sets if the endpoint service is defined in this document
     */
    public void setService(Service service);
    
    /*
     * returns true if endpoint service is present in this document
     */
    public Service getService();
    
    /*
     * Sets if the endpoint Port Type is defined in this document
     */
    public void setHavingPortType(boolean portType);
    
    /*
     * returns true if endpoint PortType is present in this document
     */
    public boolean isHavingPortType();
    
    /*
     * @return /WEB-INF/wsdl/xxx.wsdl
     */
    public String getPath();
    
    /*
     * @return URL for /WEB-INF/wsdl/xxx.wsdl
     */
    public URL getUrl();
    
}
