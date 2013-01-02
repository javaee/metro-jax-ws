/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.wsa.custom_action.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;

import junit.framework.TestCase;

/**
 * @author Rama Pulavarthi
 */
public class AddNumbersTest extends TestCase {
    public AddNumbersTest(String name) {
        super(name);
    }

    private AddNumbers createStub() throws Exception {
        return new AddNumbersService().getAddNumbersPort(new AddressingFeature());
    }

    public void testDefaultActions() throws Exception {
        int result = createStub().addNumbersNoAction(10, 10);
        assertEquals(20, result);
    }

    public void testEmptyActions() throws Exception {
        int result = createStub().addNumbersEmptyAction(10, 10);
        assertEquals(20, result);
    }

    public void testExplicitActions() throws Exception {
        AddNumbers port = createStub();
        ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.com/input");
        int result = port.addNumbers(10, 10);
        assertEquals(20, result);
    }

    public void testExplicitActions2() throws Exception {
        AddNumbers port = createStub();
        ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.com/input2");
        int result = port.addNumbers2(10, 10);
        assertEquals(20, result);
    }

    public void testDefaultOutputActionWithInputSpecified() throws Exception {
        AddNumbers port = createStub();
        ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.com/input3");
        int result = port.addNumbers3(10, 10);
        assertEquals(20, result);
    }

    public void testOneFault() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput1");
            port.addNumbersFault1(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testTwoFaults_ExplicitAddNumbers() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput2");
            port.addNumbersFault2(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testTwoFaults_ExplicitTooBigNumbers() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput2");
            port.addNumbersFault2(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testTwoFaults_OnlyAddNumbersSpecified_AddNumbers() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput3");
            port.addNumbersFault3(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testTwoFaults_OnlyAddNumbersSpecified_TooBigNumbers() throws Exception {
        try {
            AddNumbers port = createStub();
            ((BindingProvider) port).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "finput3");
            port.addNumbersFault3(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_OnlyAddNumbersSpecified_AddNumbers() throws Exception {
        try {
            createStub().addNumbersFault4(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_OnlyAddNumbersSpecified_TooBigNumbers() throws Exception {
        try {
            createStub().addNumbersFault4(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_OnlyTooBigNumbersSpecified_AddNumbers() throws Exception {
        try {
            createStub().addNumbersFault5(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_OnlyTooBigNumbersSpecified_TooBigNumbers() throws Exception {
        try {
            createStub().addNumbersFault5(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_BothSpecified_AddNumbers() throws Exception {
        try {
            createStub().addNumbersFault6(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_BothSpecified_TooBigNumbers() throws Exception {
        try {
            createStub().addNumbersFault6(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_BothEmptyString_AddNumbers() throws Exception {
        try {
            createStub().addNumbersFault7(-10, 10);
            fail("AddNumbersException_Exception MUST be thrown");
        } catch (AddNumbersException_Exception ex) {
            assertTrue(true);
        }
    }

    public void testOnlyFaultActions_BothEmptyString_TooBigNumbers() throws Exception {
        try {
            createStub().addNumbersFault7(20, 10);
            fail("TooBigNumbersException_Exception MUST be thrown");
        } catch (TooBigNumbersException_Exception ex) {
            assertTrue(true);
        }
    }
}
