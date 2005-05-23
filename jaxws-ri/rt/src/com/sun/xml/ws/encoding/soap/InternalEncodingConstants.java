/*
 * $Id: InternalEncodingConstants.java,v 1.1 2005-05-23 22:30:14 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap;

import javax.xml.namespace.QName;

/**
 *
 * @author JAX-RPC Development Team
 */
public interface InternalEncodingConstants {
    // internal URI
    public static final String JAXRPC_URI               = "http://java.sun.com/jax-rpc-ri/internal";

    // Attachment Types
    public static final QName QNAME_TYPE_IMAGE          = new QName(JAXRPC_URI, "image");
    public static final QName QNAME_TYPE_MIME_MULTIPART = new QName(JAXRPC_URI, "multipart");
    public static final QName QNAME_TYPE_SOURCE         = new QName(JAXRPC_URI, "text_xml");
    public static final QName QNAME_TYPE_DATA_HANDLER   = new QName(JAXRPC_URI, "datahandler");

    // Arrays
    public static final QName ARRAY_ELEMENT_NAME        = new QName("item");

    // Collections
    public static final QName COLLECTION_ELEMENT_NAME   = new QName("item");
    public static final QName QNAME_TYPE_COLLECTION     = new QName(JAXRPC_URI, "collection");
    public static final QName QNAME_TYPE_LIST           = new QName(JAXRPC_URI, "list");
    public static final QName QNAME_TYPE_SET            = new QName(JAXRPC_URI, "set");
    public static final QName QNAME_TYPE_ARRAY_LIST     = new QName(JAXRPC_URI, "arrayList");
    public static final QName QNAME_TYPE_VECTOR         = new QName(JAXRPC_URI, "vector");
    public static final QName QNAME_TYPE_STACK          = new QName(JAXRPC_URI, "stack");
    public static final QName QNAME_TYPE_LINKED_LIST    = new QName(JAXRPC_URI, "linkedList");
    public static final QName QNAME_TYPE_HASH_SET       = new QName(JAXRPC_URI, "hashSet");
    public static final QName QNAME_TYPE_TREE_SET       = new QName(JAXRPC_URI, "treeSet");

    // Maps
    public static final QName JAX_RPC_MAP_ENTRY_KEY_NAME    = new QName("key");
    public static final QName JAX_RPC_MAP_ENTRY_VALUE_NAME  = new QName("value");
    public static final QName QNAME_TYPE_MAP                = new QName(JAXRPC_URI, "map");
    public static final QName QNAME_TYPE_JAX_RPC_MAP_ENTRY  = new QName(JAXRPC_URI, "mapEntry");
    public static final QName QNAME_TYPE_HASH_MAP           = new QName(JAXRPC_URI, "hashMap");
    public static final QName QNAME_TYPE_TREE_MAP           = new QName(JAXRPC_URI, "treeMap");
    public static final QName QNAME_TYPE_HASHTABLE          = new QName(JAXRPC_URI, "hashtable");
    public static final QName QNAME_TYPE_PROPERTIES         = new QName(JAXRPC_URI, "properties");
//    public static final QName QNAME_TYPE_WEAK_HASH_MAP      = new QName(JAXRPC_URI, "weakhashmap");

}