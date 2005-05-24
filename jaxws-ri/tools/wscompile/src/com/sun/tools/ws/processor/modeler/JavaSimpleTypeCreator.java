/*
 * $Id: JavaSimpleTypeCreator.java,v 1.1 2005-05-24 13:42:06 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.modeler;

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.xml.ws.util.VersionUtil;

/**
 *
 * @author JAX-RPC Development Team
 */
public class JavaSimpleTypeCreator implements ModelerConstants {

    /*
     * Mapped JavaSimpleTypes
     */
    public JavaSimpleType BOOLEAN_JAVATYPE;
    public JavaSimpleType BOXED_BOOLEAN_JAVATYPE;
    public JavaSimpleType BYTE_JAVATYPE;
    public JavaSimpleType BYTE_ARRAY_JAVATYPE;
    public JavaSimpleType BOXED_BYTE_JAVATYPE;
    public JavaSimpleType BOXED_BYTE_ARRAY_JAVATYPE;
    public JavaSimpleType DOUBLE_JAVATYPE;
    public JavaSimpleType BOXED_DOUBLE_JAVATYPE;
    public JavaSimpleType FLOAT_JAVATYPE;
    public JavaSimpleType BOXED_FLOAT_JAVATYPE;
    public JavaSimpleType INT_JAVATYPE;
    public JavaSimpleType BOXED_INTEGER_JAVATYPE;
    public JavaSimpleType LONG_JAVATYPE;
    public JavaSimpleType BOXED_LONG_JAVATYPE;
    public JavaSimpleType SHORT_JAVATYPE;
    public JavaSimpleType BOXED_SHORT_JAVATYPE;
    public JavaSimpleType DECIMAL_JAVATYPE;
    public JavaSimpleType BIG_INTEGER_JAVATYPE;
    public JavaSimpleType CALENDAR_JAVATYPE;
    public JavaSimpleType DATE_JAVATYPE;
    public JavaSimpleType STRING_JAVATYPE;
    public JavaSimpleType STRING_ARRAY_JAVATYPE;
    public JavaSimpleType QNAME_JAVATYPE;
    public JavaSimpleType VOID_JAVATYPE;
    public JavaSimpleType OBJECT_JAVATYPE;
    public JavaSimpleType SOAPELEMENT_JAVATYPE;
    public JavaSimpleType URI_JAVATYPE;

    // Collections
    public JavaSimpleType COLLECTION_JAVATYPE;
    public JavaSimpleType LIST_JAVATYPE;
    public JavaSimpleType SET_JAVATYPE;
    public JavaSimpleType VECTOR_JAVATYPE;
    public JavaSimpleType STACK_JAVATYPE;
    public JavaSimpleType LINKED_LIST_JAVATYPE;
    public JavaSimpleType ARRAY_LIST_JAVATYPE;
    public JavaSimpleType HASH_SET_JAVATYPE;
    public JavaSimpleType TREE_SET_JAVATYPE;

    // Maps
    public JavaSimpleType MAP_JAVATYPE;
    public JavaSimpleType HASH_MAP_JAVATYPE;
    public JavaSimpleType TREE_MAP_JAVATYPE;
    public JavaSimpleType HASHTABLE_JAVATYPE;
    public JavaSimpleType PROPERTIES_JAVATYPE;
    public JavaSimpleType JAX_RPC_MAP_ENTRY_JAVATYPE;

    // Attachment types
    public JavaSimpleType IMAGE_JAVATYPE;
    public JavaSimpleType MIME_MULTIPART_JAVATYPE;
    public JavaSimpleType SOURCE_JAVATYPE;
    public JavaSimpleType DATA_HANDLER_JAVATYPE;

    // bug fix: 4923650
    private Map javaTypes = new HashMap();

    public JavaSimpleTypeCreator() {
        BOOLEAN_JAVATYPE = new JavaSimpleType(BOOLEAN_CLASSNAME, FALSE_STR);
        javaTypes.put(BOOLEAN_CLASSNAME, BOOLEAN_JAVATYPE);
        BOXED_BOOLEAN_JAVATYPE =
            new JavaSimpleType(BOXED_BOOLEAN_CLASSNAME, NULL_STR);
        javaTypes.put(BOXED_BOOLEAN_CLASSNAME, BOXED_BOOLEAN_JAVATYPE);
        BYTE_JAVATYPE = new JavaSimpleType(BYTE_CLASSNAME, "(byte)"+ZERO_STR);
        javaTypes.put(BYTE_CLASSNAME, BYTE_JAVATYPE);
        BYTE_ARRAY_JAVATYPE =
            new JavaSimpleType(BYTE_ARRAY_CLASSNAME, NULL_STR);
        javaTypes.put(BYTE_ARRAY_CLASSNAME, BYTE_ARRAY_JAVATYPE);
        BOXED_BYTE_JAVATYPE =
            new JavaSimpleType(BOXED_BYTE_CLASSNAME, NULL_STR);
        javaTypes.put(BOXED_BYTE_CLASSNAME, BOXED_BYTE_JAVATYPE);
        BOXED_BYTE_ARRAY_JAVATYPE =
            new JavaSimpleType(BOXED_BYTE_ARRAY_CLASSNAME, NULL_STR);
        javaTypes.put(BOXED_BYTE_ARRAY_CLASSNAME, BOXED_BYTE_ARRAY_JAVATYPE);
        DOUBLE_JAVATYPE = new JavaSimpleType(DOUBLE_CLASSNAME, ZERO_STR);
        javaTypes.put(DOUBLE_CLASSNAME, DOUBLE_JAVATYPE);
        BOXED_DOUBLE_JAVATYPE =
            new JavaSimpleType(BOXED_DOUBLE_CLASSNAME, NULL_STR);
        javaTypes.put(BOXED_DOUBLE_CLASSNAME, BOXED_DOUBLE_JAVATYPE);
        FLOAT_JAVATYPE = new JavaSimpleType(FLOAT_CLASSNAME, ZERO_STR);
        javaTypes.put(FLOAT_CLASSNAME, FLOAT_JAVATYPE);
        BOXED_FLOAT_JAVATYPE =
            new JavaSimpleType(BOXED_FLOAT_CLASSNAME, NULL_STR);
        javaTypes.put(BOXED_FLOAT_CLASSNAME, BOXED_FLOAT_JAVATYPE);
        INT_JAVATYPE = new JavaSimpleType(INT_CLASSNAME, ZERO_STR);
        javaTypes.put(INT_CLASSNAME, INT_JAVATYPE);
        BOXED_INTEGER_JAVATYPE =
            new JavaSimpleType(BOXED_INTEGER_CLASSNAME, NULL_STR);
        javaTypes.put(BOXED_INTEGER_CLASSNAME, BOXED_INTEGER_JAVATYPE);
        LONG_JAVATYPE = new JavaSimpleType(LONG_CLASSNAME, ZERO_STR);
        javaTypes.put(LONG_CLASSNAME, LONG_JAVATYPE);
        BOXED_LONG_JAVATYPE =
            new JavaSimpleType(BOXED_LONG_CLASSNAME, NULL_STR);
        javaTypes.put(BOXED_LONG_CLASSNAME, BOXED_LONG_JAVATYPE);
        SHORT_JAVATYPE =
            new JavaSimpleType(SHORT_CLASSNAME, "(short)"+ZERO_STR);
        javaTypes.put(SHORT_CLASSNAME, SHORT_JAVATYPE);
        BOXED_SHORT_JAVATYPE =
            new JavaSimpleType(BOXED_SHORT_CLASSNAME, NULL_STR);
        javaTypes.put(BOXED_SHORT_CLASSNAME, BOXED_SHORT_JAVATYPE);
        DECIMAL_JAVATYPE = new JavaSimpleType(BIGDECIMAL_CLASSNAME, NULL_STR);
        javaTypes.put(BIGDECIMAL_CLASSNAME, DECIMAL_JAVATYPE);
        BIG_INTEGER_JAVATYPE =
            new JavaSimpleType(BIGINTEGER_CLASSNAME, NULL_STR);
        javaTypes.put(BIGINTEGER_CLASSNAME, BIG_INTEGER_JAVATYPE);
        CALENDAR_JAVATYPE = new JavaSimpleType(CALENDAR_CLASSNAME, NULL_STR);
        javaTypes.put(CALENDAR_CLASSNAME, CALENDAR_JAVATYPE);
        DATE_JAVATYPE = new JavaSimpleType(DATE_CLASSNAME, NULL_STR);
        javaTypes.put(DATE_CLASSNAME, DATE_JAVATYPE);
        STRING_JAVATYPE = new JavaSimpleType(STRING_CLASSNAME, NULL_STR);
        javaTypes.put(STRING_CLASSNAME, STRING_JAVATYPE);
        STRING_ARRAY_JAVATYPE =
            new JavaSimpleType(STRING_ARRAY_CLASSNAME, NULL_STR);
        javaTypes.put(STRING_ARRAY_CLASSNAME, STRING_ARRAY_JAVATYPE);
        QNAME_JAVATYPE = new JavaSimpleType(QNAME_CLASSNAME, NULL_STR);
        javaTypes.put(QNAME_CLASSNAME, QNAME_JAVATYPE);


        VOID_JAVATYPE = new JavaSimpleType(VOID_CLASSNAME, null);
        javaTypes.put(VOID_CLASSNAME, VOID_JAVATYPE);
        OBJECT_JAVATYPE = new JavaSimpleType(OBJECT_CLASSNAME, null);
        javaTypes.put(OBJECT_CLASSNAME, OBJECT_JAVATYPE);
        SOAPELEMENT_JAVATYPE = new JavaSimpleType(SOAPELEMENT_CLASSNAME, null);
        javaTypes.put(SOAPELEMENT_CLASSNAME, SOAPELEMENT_JAVATYPE);
        URI_JAVATYPE = new JavaSimpleType(URI_CLASSNAME, null);
        javaTypes.put(URI_CLASSNAME, URI_JAVATYPE);


        // Collections
        COLLECTION_JAVATYPE = new JavaSimpleType(COLLECTION_CLASSNAME, null);
        javaTypes.put(COLLECTION_CLASSNAME, COLLECTION_JAVATYPE);
        LIST_JAVATYPE = new JavaSimpleType(LIST_CLASSNAME, null);
        javaTypes.put(LIST_CLASSNAME, LIST_JAVATYPE);
        SET_JAVATYPE = new JavaSimpleType(SET_CLASSNAME, null);
        javaTypes.put(SET_CLASSNAME, SET_JAVATYPE);
        VECTOR_JAVATYPE = new JavaSimpleType(VECTOR_CLASSNAME, null);
        javaTypes.put(VECTOR_CLASSNAME, VECTOR_JAVATYPE);
        STACK_JAVATYPE = new JavaSimpleType(STACK_CLASSNAME, null);
        javaTypes.put(STACK_CLASSNAME, STACK_JAVATYPE);
        LINKED_LIST_JAVATYPE = new JavaSimpleType(LINKED_LIST_CLASSNAME, null);
        javaTypes.put(LINKED_LIST_CLASSNAME, LINKED_LIST_JAVATYPE);
        ARRAY_LIST_JAVATYPE = new JavaSimpleType(ARRAY_LIST_CLASSNAME, null);
        javaTypes.put(ARRAY_LIST_CLASSNAME, ARRAY_LIST_JAVATYPE);
        HASH_SET_JAVATYPE = new JavaSimpleType(HASH_SET_CLASSNAME, null);
        javaTypes.put(HASH_SET_CLASSNAME, HASH_SET_JAVATYPE);
        TREE_SET_JAVATYPE = new JavaSimpleType(TREE_SET_CLASSNAME, null);
        javaTypes.put(TREE_SET_CLASSNAME, TREE_SET_JAVATYPE);

        // Maps
        MAP_JAVATYPE = new JavaSimpleType(MAP_CLASSNAME, null);
        javaTypes.put(MAP_CLASSNAME, MAP_JAVATYPE);
        HASH_MAP_JAVATYPE = new JavaSimpleType(HASH_MAP_CLASSNAME, null);
        javaTypes.put(HASH_MAP_CLASSNAME, HASH_MAP_JAVATYPE);
        TREE_MAP_JAVATYPE = new JavaSimpleType(TREE_MAP_CLASSNAME, null);
        javaTypes.put(TREE_MAP_CLASSNAME, TREE_MAP_JAVATYPE);
        HASHTABLE_JAVATYPE = new JavaSimpleType(HASHTABLE_CLASSNAME, null);
        javaTypes.put(HASHTABLE_CLASSNAME, HASHTABLE_JAVATYPE);
        PROPERTIES_JAVATYPE = new JavaSimpleType(PROPERTIES_CLASSNAME, null);
        javaTypes.put(PROPERTIES_CLASSNAME, PROPERTIES_JAVATYPE);
        JAX_RPC_MAP_ENTRY_JAVATYPE =
            new JavaSimpleType(JAX_RPC_MAP_ENTRY_CLASSNAME, null);
        javaTypes.put(JAX_RPC_MAP_ENTRY_CLASSNAME, JAX_RPC_MAP_ENTRY_JAVATYPE);

        // Attachment types
        IMAGE_JAVATYPE = new JavaSimpleType(IMAGE_CLASSNAME, null);
        javaTypes.put(IMAGE_CLASSNAME, IMAGE_JAVATYPE);
        MIME_MULTIPART_JAVATYPE = new JavaSimpleType(MIME_MULTIPART_CLASSNAME, null);
        javaTypes.put(MIME_MULTIPART_CLASSNAME, MIME_MULTIPART_JAVATYPE);
        SOURCE_JAVATYPE = new JavaSimpleType(SOURCE_CLASSNAME, null);
        javaTypes.put(SOURCE_CLASSNAME, SOURCE_JAVATYPE);
        DATA_HANDLER_JAVATYPE = new JavaSimpleType(DATA_HANDLER_CLASSNAME, null);
        javaTypes.put(DATA_HANDLER_CLASSNAME, DATA_HANDLER_JAVATYPE);
    }

    //  bug fix: 4923650
    public JavaSimpleType getJavaSimpleType(String classname) {
        return (JavaSimpleType) javaTypes.get(classname);
    }
}
