/*
 * $Id: SerializerConstants.java,v 1.2 2005-07-18 16:52:14 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package com.sun.xml.ws.encoding.soap;

/**
 *
 * @author WS Development Team
 */
public interface SerializerConstants {
    public static final boolean ENCODE_TYPE             = true;
    public static final boolean DONT_ENCODE_TYPE        = false;
    public static final boolean SERIALIZE_AS_REF        = true;
    public static final boolean DONT_SERIALIZE_AS_REF   = false;
    public static final boolean REFERENCEABLE           = true;
    public static final boolean NOT_REFERENCEABLE       = false;
    public static final boolean NULLABLE                = true;
    public static final boolean NOT_NULLABLE            = false;
    public static final boolean REFERENCED_INSTANCE     = true;
    public static final boolean UNREFERENCED_INSTANCE   = false;
}