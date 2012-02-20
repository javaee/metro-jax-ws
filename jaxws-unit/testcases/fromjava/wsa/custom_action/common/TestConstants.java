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
