/**
 * $Id: WSDLOutputResolver.java,v 1.2 2005-07-20 20:58:53 kwalsh Exp $
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
public interface WSDLOutputResolver {
    public Result getWSDLOutput(String suggestedFilename);

    public Result getSchemaOutput(String namespace, String suggestedFilename);
}
