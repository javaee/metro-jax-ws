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

package com.sun.tools.ws.processor.generator;

import com.sun.tools.ws.processor.modeler.ModelerConstants;

/**
 * @author WS Development Team
 */
public interface GeneratorConstants extends ModelerConstants {
    public static final char DOTC = '.';
    public static final String SIG_INNERCLASS = "$";
    public static final String JAVA_SRC_SUFFIX = ".java";
    public static final String QNAME_SUFFIX = "_QNAME";
    public static final String GET = "get";
    public static final String IS = "is";
    public static final String RESPONSE = "Response";
    public static final String FAULT_CLASS_MEMBER_NAME = "faultInfo";
}
