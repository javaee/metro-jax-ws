/**
 * $Id: WebServiceConstants.java,v 1.2 2005-05-25 21:20:45 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import com.sun.tools.ws.processor.modeler.rmi.RmiConstants;
import com.sun.tools.ws.processor.modeler.annotation.*;

/**
 *
 * @author  dkohlert
 */
public interface WebServiceConstants extends RmiConstants {

    public static final String RETURN                       = "return";
    public static final String RETURN_CAPPED                = "Return";
    public static final String RETURN_VALUE                 = "_return";
    public static final String SERVICE                      = "Service";
    public static final String RUNTIME_EXCEPTION_CLASSNAME  = "java.lang.RuntimeException";
    public static final String PD                           = ".";
    public static final String JAXWS                        = "jaxws";
    public static final String JAXWS_PACKAGE_PD             = JAXWS+PD;
    public static final String PD_JAXWS_PACKAGE_PD          = PD+JAXWS+PD;
    public static final String BEAN                         = "Bean";
    public static final String GET_PREFIX                   = "get";
    public static final String IS_PREFIX                    = "is";
    public static final String FAULT_INFO                   = "faultInfo";
    public static final String GET_FAULT_INFO               = "getFaultInfo";
    public static final String HTTP_PREFIX                  = "http://";
    public static final String JAVA_LANG_OBJECT             = "java.lang.Object";

    // 181 constants
    public static final String WEBSERVICE_NAMESPACE         = "http://www.bea.com/xml/ns/jws";
    public static final String HANDLER_CONFIG               = "handler-config";
    public static final String HANDLER_CHAIN                = "handler-chain";
    public static final String HANDLER_CHAIN_NAME           = "handler-chain-name";
    public static final String HANDLER                      = "handler";
    public static final String HANDLER_NAME                 = "handler-name";
    public static final String HANDLER_CLASS                = "handler-class";
    public static final String INIT_PARAM                   = "init-param";
    public static final String SOAP_ROLE                    = "soap-role";
    public static final String SOAP_HEADER                  = "soap-header";
    public static final String PARAM_NAME                   = "param-name";
    public static final String PARAM_VALUE                  = "param-value";
}
