/*
 * $Id: ProcessorOptions.java,v 1.5 2005-08-12 18:07:51 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor;

/**
 * Property names used by ProcessorActions
 *
 * @author WS Development Team
 */
public class ProcessorOptions {

    public final static String SOURCE_DIRECTORY_PROPERTY = "sourceDirectory";
    public final static String DESTINATION_DIRECTORY_PROPERTY =
        "destinationDirectory";
    public final static String NONCLASS_DESTINATION_DIRECTORY_PROPERTY =
        "nonclassDestinationDirectory";
    public final static String VALIDATE_WSDL_PROPERTY = "validationWSDL";
    public final static String EXPLICIT_SERVICE_CONTEXT_PROPERTY =
        "explicitServiceContext";
    public final static String PRINT_STACK_TRACE_PROPERTY = "printStackTrace";
    public final static String DONOT_OVERRIDE_CLASSES = "donotOverride";
    public final static String NO_DATA_BINDING_PROPERTY = "noDataBinding";
    public final static String USE_WSI_BASIC_PROFILE = "useWSIBasicProfile";
    public final static String STRICT_COMPLIANCE = "strictCompliance";
    public final static String JAXWS_SOURCE_VERSION = "sourceVersion";
    public final static String UNWRAP_DOC_LITERAL_WRAPPERS =
        "unwrapDocLitWrappers";
    public final static String BINDING_FILES = "bindingFiles";
    public final static String EXTENSIONS = "extensions";
    public final static String PROTOCOL = "protocol";
    public final static String TRANSPORT = "transport";
}
