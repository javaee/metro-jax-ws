/*
 * $Id: RmiConstants.java,v 1.1 2005-05-24 13:31:27 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.modeler.rmi;

import com.sun.tools.ws.processor.modeler.ModelerConstants;

/**
 *
 * @author JAX-RPC Development Team
 */
public interface RmiConstants extends ModelerConstants {
    /*
     * Type codes
     */
    static final int TC_BOOLEAN = 0;
    static final int TC_BYTE    = 1;
    static final int TC_CHAR    = 2;
    static final int TC_SHORT   = 3;
    static final int TC_INT     = 4;
    static final int TC_LONG    = 5;
    static final int TC_FLOAT   = 6;
    static final int TC_DOUBLE  = 7;
    static final int TC_NULL    = 8;
    static final int TC_ARRAY   = 9;
    static final int TC_CLASS   = 10;
    static final int TC_VOID    = 11;
    static final int TC_METHOD  = 12;
    static final int TC_ERROR   = 13;

    /* Signature Characters */
    static final char SIGC_VOID        = 'V';
    static final String SIG_VOID       = "V";
    static final char SIGC_BOOLEAN     = 'Z';
    static final String SIG_BOOLEAN    = "Z";
    static final char SIGC_BYTE        = 'B';
    static final String SIG_BYTE       = "B";
    static final char SIGC_CHAR        = 'C';
    static final String SIG_CHAR       = "C";
    static final char SIGC_SHORT       = 'S';
    static final String SIG_SHORT      = "S";
    static final char SIGC_INT         = 'I';
    static final String SIG_INT        = "I";
    static final char SIGC_LONG        = 'J';
    static final String SIG_LONG       = "J";
    static final char SIGC_FLOAT       = 'F';
    static final String SIG_FLOAT      = "F";
    static final char SIGC_DOUBLE      = 'D';
    static final String SIG_DOUBLE     = "D";
    static final char SIGC_ARRAY       = '[';
    static final String SIG_ARRAY      = "[";
    static final char SIGC_CLASS       = 'L';
    static final String SIG_CLASS      = "L";
    static final char SIGC_METHOD      = '(';
    static final String SIG_METHOD     = "(";
    static final char SIGC_ENDCLASS    = ';';
    static final String SIG_ENDCLASS   = ";";
    static final char SIGC_ENDMETHOD   = ')';
    static final String SIG_ENDMETHOD  = ")";
    static final char SIGC_PACKAGE     = '/';
    static final String SIG_PACKAGE    = "/";
    static final char SIGC_INNERCLASS  = '$';
    static final String SIG_INNERCLASS = "$";
    static final char SIGC_UNDERSCORE  = '_';

    // String constants
    public static final String RMI_MODELER_NESTED_RMI_MODELER_ERROR =
        "rmimodeler.nestedRmiModelerError";
    public static final String RMI_MODELER_CLASS_NOT_FOUND =
        "rmimodeler.class.not.found";
    public static final String RMI_MODELER_NESTED_INNER_CLASSES_NOT_SUPPORTED =
        "rmimodeler.nested.inner.classes.not.supported";
    public static final String RMI_MODELER_INVALID_REMOTE_INTERFACE =
        "rmimodeler.invalid.remote.interface";
    public static final String EMPTY_STRING = "";
    public static final String DOT = ".";
    public static final char DOTC = '.';
    public static final String UNDERSCORE = "_";
    public static final String IMPL = "Impl";
    public static final String RESPONSE_STRUCT = "_ResponseStruct";
    public static final String REQUEST_STRUCT = "_RequestStruct";
    public static final String RESULT = "result";
    public static final String ARRAY_OF = "arrayOf";
    public static final String PORT = "Port";
    public static final String BINDING = "Binding";
    public static final String RESPONSE = "Response";
    public static final String ELEMENT = "Element";
    public static final String RES_STRUCT = "_Res";
    public static final String REQ_STRUCT = "_Req";

    /*
     * Identifiers potentially useful for all Generators
     */
    public static final String EXCEPTION_CLASSNAME =
        java.lang.Exception.class.getName();
    public static final String REMOTE_CLASSNAME =
        java.rmi.Remote.class.getName();
    public static final String REMOTE_EXCEPTION_CLASSNAME =
        java.rmi.RemoteException.class.getName();
    public static final String RUNTIME_EXCEPTION_CLASSNAME =
        java.lang.RuntimeException.class.getName();
    public static final String SERIALIZABLE_CLASSNAME =
        java.io.Serializable.class.getName();
    public static final String HOLDER_CLASSNAME =
        javax.xml.ws.Holder.class.getName();

    // Exception messages
    public static final String GET_MESSAGE = "getMessage";
    public static final String GET_LOCALIZED_MESSAGE = "getLocalizedMessage";
}
