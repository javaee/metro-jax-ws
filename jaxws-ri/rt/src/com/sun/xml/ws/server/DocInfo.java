/*
 * $Id: DocInfo.java,v 1.2 2005-06-03 20:48:35 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

import java.io.InputStream;

public interface DocInfo {
    
    /*
     * The implemenation needs to work for multiple invocations of this method
     */
    public InputStream getDoc();
    
    /*
     * @return wsdl=a, xsd=c etc
     */
    public String getQueryString();
    
    /*
     * @return /WEB-INF/wsdl/xxx.wsdl
     */
    public String getPath();

    /*
     * Used to resolve relative doc locations
     */
    public DocContext getDocContext();
    
}
