/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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