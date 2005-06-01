/**
 * $Id: _WsdlOutputResolver.java,v 1.1 2005-06-01 22:13:52 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.wsdl.writer;

import javax.xml.transform.Result;

/**
 *
 * @author dkohlert
 */
public interface WsdlOutputResolver {
    public Result getWSDLOutput(String suggestedFilename);
    public Result getSchemaOutput(String namespace, String suggestedFilename);
}
