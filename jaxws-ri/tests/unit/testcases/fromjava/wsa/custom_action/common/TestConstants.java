/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package fromjava.wsa.custom_action.common;

/**
 * @author Arun Gupta
 */
public class TestConstants {
    public static final String ADD_NUMBERS_IN_NOACTION = "http://foobar.org/AddNumbers/addNumbersNoActionRequest";
    public static final String ADD_NUMBERS_IN_EMPTYACTION = "http://foobar.org/AddNumbers/addNumbersEmptyActionRequest";
    public static final String ADD_NUMBERS_IN_ACTION = "http://example.com/input";
    public static final String ADD_NUMBERS2_IN_ACTION = "http://example.com/input2";
    public static final String ADD_NUMBERS3_IN_ACTION = "http://example.com/input3";
    public static final String ADD_NUMBERS_FAULT1_IN_ACTION = "finput1";
    public static final String ADD_NUMBERS_FAULT2_IN_ACTION = "finput2";
    public static final String ADD_NUMBERS_FAULT3_IN_ACTION = "finput3";
    public static final String ADD_NUMBERS_FAULT4_IN_ACTION = "http://foobar.org/AddNumbers/addNumbersFault4Request";
    public static final String ADD_NUMBERS_FAULT5_IN_ACTION = "http://foobar.org/AddNumbers/addNumbersFault5Request";
    public static final String ADD_NUMBERS_FAULT6_IN_ACTION = "http://foobar.org/AddNumbers/addNumbersFault6Request";
    public static final String ADD_NUMBERS_FAULT7_IN_ACTION = "http://foobar.org/AddNumbers/addNumbersFault7Request";

    public static final String ADD_NUMBERS_OUT_NOACTION = "http://foobar.org/AddNumbers/addNumbersNoActionResponse";
    public static final String ADD_NUMBERS_OUT_EMPTYACTION = "http://foobar.org/AddNumbers/addNumbersEmptyActionResponse";
    public static final String ADD_NUMBERS_OUT_ACTION = "http://example.com/output";
    public static final String ADD_NUMBERS2_OUT_ACTION = "http://example.com/output2";
    public static final String ADD_NUMBERS3_OUT_ACTION = "http://foobar.org/AddNumbers/addNumbers3Response";
    public static final String ADD_NUMBERS_FAULT1_ADDNUMBERS_ACTION = "http://fault1";
    public static final String ADD_NUMBERS_FAULT2_ADDNUMBERS_ACTION = "http://fault2/addnumbers";
    public static final String ADD_NUMBERS_FAULT2_TOOBIGNUMBERS_ACTION = "http://fault2/toobignumbers";
    public static final String ADD_NUMBERS_FAULT3_ADDNUMBERS_ACTION = "http://fault3/addnumbers";
    public static final String ADD_NUMBERS_FAULT3_TOOBIGNUMBERS_ACTION = "http://foobar.org/AddNumbers/addNumbersFault3/Fault/TooBigNumbersException";
    public static final String ADD_NUMBERS_FAULT4_ADDNUMBERS_ACTION = "http://fault4/addnumbers";
    public static final String ADD_NUMBERS_FAULT4_TOOBIGNUMBERS_ACTION = "http://foobar.org/AddNumbers/addNumbersFault4/Fault/TooBigNumbersException";
    public static final String ADD_NUMBERS_FAULT5_ADDNUMBERS_ACTION = "http://foobar.org/AddNumbers/addNumbersFault5/Fault/AddNumbersException";
    public static final String ADD_NUMBERS_FAULT5_TOOBIGNUMBERS_ACTION = "http://fault5/toobignumbers";
    public static final String ADD_NUMBERS_FAULT6_ADDNUMBERS_ACTION = "http://fault6/addnumbers";
    public static final String ADD_NUMBERS_FAULT6_TOOBIGNUMBERS_ACTION = "http://fault6/toobignumbers";
    public static final String ADD_NUMBERS_FAULT7_ADDNUMBERS_ACTION = "http://foobar.org/AddNumbers/addNumbersFault7/Fault/AddNumbersException";
    public static final String ADD_NUMBERS_FAULT7_TOOBIGNUMBERS_ACTION = "http://foobar.org/AddNumbers/addNumbersFault7/Fault/TooBigNumbersException";
}
