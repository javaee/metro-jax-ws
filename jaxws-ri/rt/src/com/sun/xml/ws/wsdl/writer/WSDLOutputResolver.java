/**
 * $Id: WSDLOutputResolver.java,v 1.6 2005-09-01 21:51:00 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer;

import javax.xml.transform.Result;
import javax.xml.ws.Holder;


/**
 *
 * @author WS Development Team
 */
public interface WSDLOutputResolver {
    public Result getWSDLOutput(String suggestedFilename);

    /*
     * Updates filename if the suggested filename need to be changed in
     * wsdl:import
     *
     * return null if abstract WSDL need not be generated
     */
    public Result getAbstractWSDLOutput(Holder<String> filename);

    /*
     * Updates filename if the suggested filename need to be changed in
     * xsd:import
     *
     * return null if schema need not be generated
     */
    public Result getSchemaOutput(String namespace, Holder<String> filename);

}
