/**
 * $Id: WSDLOutputResolver.java,v 1.3 2005-08-19 21:06:38 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer;

import javax.xml.transform.Result;


/**
 *
 * @author WS Development Team
 */
public interface WSDLOutputResolver {
    public Result getWSDLOutput(String suggestedFilename);

    public Result getSchemaOutput(String namespace, String suggestedFilename);
}
