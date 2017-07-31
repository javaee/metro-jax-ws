/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.ws.wsdl.parser;


/**
*
* @author zheng.jun.li@oracle.com
*/
public class WSImportSecTest extends WSImportSecTestBase{

    public void testXmlSecSysParam() throws Exception{
    	 /**
         * Scenario for reading local file contents
        */
    	runTests( new TestParameters(new String[] {"-Xdebug", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_1.wsdl"},
                            SECURE_PROCESSING_ON_MSG, FILE_NOT_FOUND_MSG, "XXE Scenario 1 WITH secure processing:"));
    	runTests( new TestParameters(new String[] {"-Xdebug", "-disableXmlSecurity", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_1.wsdl"},
                            FILE_NOT_FOUND_MSG, SECURE_PROCESSING_ON_MSG, "XXE Scenario 1 WITHOUT secure processing:"));
        /**
         * Scenario for URL connection
        */
    	runTests( new TestParameters(new String[] {"-Xdebug", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_2.wsdl"},
                            SECURE_PROCESSING_ON_MSG, "XXE Scenario 2 WITH secure processing:"));
    	runTests( new TestParameters(new String[] {"-Xdebug", "-disableXmlSecurity", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_2.wsdl"},
                            BASIC_WSIMPORT_TOOL_MSG, SECURE_PROCESSING_ON_MSG,
                            "XXE Scenario 2 WITHOUT secure processing:", TIMEOUT));

        /**
         * Scenario for FTP connection timeout
        */
    	runTests( new TestParameters(new String[]{"-Xdebug", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_3.wsdl"},
                            SECURE_PROCESSING_ON_MSG, "XXE Scenario 3 WITH secure processing:"));
    	runTests( new TestParameters(new String[]{"-Xdebug", "-disableXmlSecurity", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_3.wsdl"},
                            BASIC_WSIMPORT_TOOL_MSG, SECURE_PROCESSING_ON_MSG, "XXE Scenario 3 WITHOUT secure processing:", TIMEOUT));
        /**
         * Scenario for recursive DTD Entity expansion
        */
    	runTests( new TestParameters(new String[] {"-Xdebug", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_4.wsdl"},
                            SECURE_PROCESSING_ON_MSG, RECURSIVE_ENTITY_REFERENCE_MSG, "XEE Scenario 4 WITH secure processing:"));
    	runTests( new TestParameters(new String[] {"-Xdebug", "-disableXmlSecurity", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_4.wsdl"},
                            RECURSIVE_ENTITY_REFERENCE_MSG, SECURE_PROCESSING_ON_MSG, "XEE Scenario 4 WITHOUT secure processing:"));

        /**
         * Scenario for exceeding JDK limit of entity expansions.
        */
    	runTests( new TestParameters(new String[] {"-Xdebug", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_5.wsdl"},
                            SECURE_PROCESSING_ON_MSG, ENTITY_EXPANSION_LIMIT_MSG, "XEE Scenario 5 WITH secure processing:"));
    	runTests( new TestParameters(new String[] {"-Xdebug", "-disableXmlSecurity", "./src/test/resources/com/sun/tools/ws/wsdl/parser/wsimport_5.wsdl"},
                            ENTITY_EXPANSION_LIMIT_MSG, SECURE_PROCESSING_ON_MSG, "XEE Scenario 5 WITHOUT secure processing:"));
    }

}
