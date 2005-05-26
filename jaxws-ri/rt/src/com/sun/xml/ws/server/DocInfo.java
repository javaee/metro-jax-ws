/*
 * $Id: DocInfo.java,v 1.1 2005-05-26 18:21:16 jitu Exp $
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
    
}
