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
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package handler.single_handlertube.common;

/**
 * @author Rama Pulavarthi
 */

public class TestConstants {
// used to tell the test service to throw an exception
    public static final int SERVER_THROW_RUNTIME_EXCEPTION = -100;
    public static final int SERVER_THROW_MYFAULT_EXCEPTION = -101;

    public static final String MESSAGE_IN_FAULT = "message in a fault";
    public static final String MESSAGE_IN_EXCEPTION = "message in an exception";

    public static final String USER_CLIENT_PROPERTY_NAME = "client_name_abc";
    public static final String USER_HANDLER_PROPERTY_NAME = "handler_name_123";
    public static final String USER_PROPERTY_CLIENT_SET = "foo_value1";
    public static final String USER_PROPERTY_HANDLER_SET = "foo_value2";

    public static final String HANDLER_NAME = "handlerName";
    public static final String CLIENT_PREFIX = "client";
    public static final String SERVER_PREFIX = "server";
    public static final String INBOUND = "inbound";
    public static final String OUTBOUND = "not inbound";

    // handler actions. not using enums because ints are easier to use in wsdl
    public static final int HA_RETURN_TRUE = 0; // the default
    public static final int HA_RETURN_FALSE = 1;
    public static final int HA_RETURN_FALSE_INBOUND = 2;
    public static final int HA_RETURN_FALSE_OUTBOUND = 3;
    public static final int HA_RETURN_FALSE_CHANGE_MESSAGE = 4;
    public static final int HA_ADD_AND_CHECK_PROPS_INBOUND = 5;
    public static final int HA_ADD_BAD_MU_HEADER_OUTBOUND = 6;
    public static final int HA_ADD_BAD_MU_HEADER_CLIENT2_OUTBOUND = 7;
    public static final int HA_ADD_GOOD_MU_HEADER_OUTBOUND = 8;
    public static final int HA_ADD_HEADER_OUTBOUND_CLIENT_ROLE1 = 9;
    public static final int HA_ADD_HEADER_OUTBOUND = 10;
    public static final int HA_ADD_ONE = 11;
    public static final int HA_ADD_USER_PROPERTY_INBOUND = 12;
    public static final int HA_ADD_USER_PROPERTY_OUTBOUND = 13;
    public static final int HA_CHECK_FOR_ADDED_HEADER_INBOUND = 14;
    public static final int HA_CHECK_FOR_ADDED_HEADER_OUTBOUND = 15;
    public static final int HA_CHECK_FOR_USER_PROPERTY_OUTBOUND = 16;
    public static final int HA_CHECK_MC_BAD_PROPS = 17;
    public static final int HA_CHECK_MC_PROPS = 18;
    public static final int HA_CHECK_LMC = 19;
    public static final int HA_CHECK_SMC = 20;
    public static final int HA_CHECK_SMC_ALL_ROLES = 21;
    public static final int HA_INSERT_FAULT_AND_THROW_PE_INBOUND = 22;
    public static final int HA_INSERT_FAULT_AND_THROW_PE_OUTBOUND = 23;
    public static final int HA_REGISTER_HANDLE_XYZ = 24;
    public static final int HA_THROW_PROTOCOL_EXCEPTION_INBOUND = 25;
    public static final int HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND = 26;
    public static final int HA_THROW_RUNTIME_EXCEPTION_INBOUND = 27;
    public static final int HA_THROW_RUNTIME_EXCEPTION_OUTBOUND = 28;
    public static final int HA_THROW_SOAP_FAULT_EXCEPTION_INBOUND = 29;
    public static final int HA_THROW_SOAP_FAULT_EXCEPTION_OUTBOUND = 30;

    // actions for a handler during a handleFault call
    // all need to be above 200 for server to check
    public static final int HF_CHECK_FAULT_MESSAGE_STRING = 200;
    public static final int HF_GET_FAULT_IN_MESSAGE = 201;
    public static final int HF_RETURN_FALSE = 202;
    public static final int HF_THROW_PROTOCOL_EXCEPTION = 203;
    public static final int HF_THROW_RUNTIME_EXCEPTION = 204;
    public static final int HF_THROW_TEST_PROTOCOL_EXCEPTION = 205;
    

    // report lists
    public static final String REPORT_CALLED_HANDLERS = "CalledHandlers";
    public static final String REPORT_CLOSED_HANDLERS = "ClosedHandlers";
    public static final String REPORT_DESTROYED_HANDLERS = "BitTheDust";

    //error messages
    public static final String ERR_CLIENT_CALLED_HANDLERS =
            "Client-side Called Handlers did not match";
    public static final String ERR_SERVER_CALLED_HANDLERS =
            "Server-side Called Handlers did not match";
    public static final String ERR_CLIENT_CLOSED_HANDLERS =
            "Client-side Closed Handlers did not match";
    public static final String ERR_SERVER_CLOSED_HANDLERS =
            "Server-side Closed Handlers did not match";
}
