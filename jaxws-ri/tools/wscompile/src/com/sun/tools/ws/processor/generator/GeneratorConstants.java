/*
 * $Id: GeneratorConstants.java,v 1.1 2005-05-23 23:14:48 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import com.sun.tools.ws.processor.modeler.ModelerConstants;

/**
 * @author JAX-RPC Development Team
 */
public interface GeneratorConstants extends ModelerConstants {

    /*
     * Constants used in the generators
     */
    public static final String FILE_TYPE_WSDL = "Wsdl";
    public static final String FILE_TYPE_REMOTE_INTERFACE = "RemoteInterface";
    public static final String FILE_TYPE_SERVICE = "Service";
    public static final String FILE_TYPE_HANDLER_CONFIG = "HandlerConfig";
    public static final String FILE_TYPE_SERVICE_IMPL = "ServiceImpl";
    public static final String FILE_TYPE_EXCEPTION = "Exception";
    public static final String FILE_TYPE_WRAPPER_BEAN = "WrapperBean";
    public static final String FILE_TYPE_EXCEPTION_BEAN = "ExceptionBean";
    /*
     * Identifiers potentially useful for all Generators
     */

    public static final String ID_DELEGATE_BASE = "com.sun.xml.rpc.soap.internal.DelegateBase";


    public static final String UNDERSCORE = "_";
    public static final String STUB_SUFFIX = "_Stub";

    public static final String CLIENT_DELEGATE_SUFFIX = "_Delegate";
    public static final String CLIENT_CONTACTINFOLIST_SUFFIX = "_ContactInfoList";

    public static final String CLIENT_ENCODER_DECODER_SUFFIX = "_CED";
    public static final String SERVER_ENCODER_DECODER_SUFFIX = "_SED";
    public static final String TIE_SUFFIX = "_Tie";

    public static final String PEPT_TIE_SUFFIX = "_PTie";
    public static final String EPTFF_SUFFIX = "_EPTFF";
    public static final String JAVA_SRC_SUFFIX = ".java";
    public static final String IMPL_SUFFIX = "_Impl";
    public static final String ARRAY = "Array";
    public static final String MEMBER_PREFIX = "my";

    public static final String OPCODE_SUFFIX = "_OPCODE";
    public static final String QNAME_SUFFIX = "_QNAME";
    public static final String TYPE_QNAME_SUFFIX = "_TYPE" + QNAME_SUFFIX;

    public static final String GET = "get";
    public static final String IS = "is";
    public static final String SET = "set";
    public static final String RESPONSE = "Response";
    public static final String NS_PREFIX = "ns";
    public static final String SERVICE_SUFFIX = "_Service";
    public static final String JAVA_PACKAGE_PREFIX = "java.";
    public static final String JAVAX_PACKAGE_PREFIX = "javax.";
    public static final String FAULT_CLASS_MEMBER_NAME = "faultInfo";
}
