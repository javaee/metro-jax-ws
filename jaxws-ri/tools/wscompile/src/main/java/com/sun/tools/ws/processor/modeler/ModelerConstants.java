/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.processor.modeler;


/**
 *
 * @author WS Development Team
 */
public enum ModelerConstants {

    FALSE_STR("false"),
    ZERO_STR("0"),
    NULL_STR("null"),
    ARRAY_STR("Array"),

    /*
     * Java ClassNames
     */
    /*
      * Java ClassNames
      */
    IOEXCEPTION_CLASSNAME("java.io.IOException"),
    BOOLEAN_CLASSNAME("boolean"),
    BOXED_BOOLEAN_CLASSNAME("java.lang.Boolean"),
    BYTE_CLASSNAME("byte"),
    BYTE_ARRAY_CLASSNAME("byte[]"),
    BOXED_BYTE_CLASSNAME("java.lang.Byte"),
    BOXED_BYTE_ARRAY_CLASSNAME("java.lang.Byte[]"),
    CLASS_CLASSNAME("java.lang.Class"),
    CHAR_CLASSNAME("char"),
    BOXED_CHAR_CLASSNAME("java.lang.Character"),
    DOUBLE_CLASSNAME("double"),
    BOXED_DOUBLE_CLASSNAME("java.lang.Double"),
    FLOAT_CLASSNAME("float"),
    BOXED_FLOAT_CLASSNAME("java.lang.Float"),
    INT_CLASSNAME("int"),
    BOXED_INTEGER_CLASSNAME("java.lang.Integer"),
    LONG_CLASSNAME("long"),
    BOXED_LONG_CLASSNAME("java.lang.Long"),
    SHORT_CLASSNAME("short"),
    BOXED_SHORT_CLASSNAME("java.lang.Short"),
    BIGDECIMAL_CLASSNAME("java.math.BigDecimal"),
    BIGINTEGER_CLASSNAME("java.math.BigInteger"),
    CALENDAR_CLASSNAME("java.util.Calendar"),
    DATE_CLASSNAME("java.util.Date"),
    STRING_CLASSNAME("java.lang.String"),
    STRING_ARRAY_CLASSNAME("java.lang.String[]"),
    QNAME_CLASSNAME("javax.xml.namespace.QName"),
    VOID_CLASSNAME("void"),
    OBJECT_CLASSNAME("java.lang.Object"),
    SOAPELEMENT_CLASSNAME("javax.xml.soap.SOAPElement"),
    IMAGE_CLASSNAME("java.awt.Image"),
    MIME_MULTIPART_CLASSNAME("javax.mail.internet.MimeMultipart"),
    SOURCE_CLASSNAME("javax.xml.transform.Source"),
    DATA_HANDLER_CLASSNAME("javax.activation.DataHandler"),
    URI_CLASSNAME("java.net.URI"),
    //     URI_CLASSNAME                ("java.lang.String"),
    // Collections
    COLLECTION_CLASSNAME("java.util.Collection"),
    LIST_CLASSNAME("java.util.List"),
    SET_CLASSNAME("java.util.Set"),
    VECTOR_CLASSNAME("java.util.Vector"),
    STACK_CLASSNAME("java.util.Stack"),
    LINKED_LIST_CLASSNAME("java.util.LinkedList"),
    ARRAY_LIST_CLASSNAME("java.util.ArrayList"),
    HASH_SET_CLASSNAME("java.util.HashSet"),
    TREE_SET_CLASSNAME("java.util.TreeSet"),

    // Maps
    MAP_CLASSNAME("java.util.Map"),
    HASH_MAP_CLASSNAME("java.util.HashMap"),
    TREE_MAP_CLASSNAME("java.util.TreeMap"),
    HASHTABLE_CLASSNAME("java.util.Hashtable"),
    PROPERTIES_CLASSNAME("java.util.Properties"),
    //     WEAK_HASH_MAP_CLASSNAME     ("java.util.WeakHashMap"),
    JAX_WS_MAP_ENTRY_CLASSNAME("com.sun.xml.ws.encoding.soap.JAXWSMapEntry");

    private String value;

    private ModelerConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
