/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.config.management.policy;

import com.sun.xml.ws.api.config.management.policy.ManagementAssertion.Setting;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;

import java.util.HashMap;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagedClientAssertionTest extends TestCase {
    /**
     * The name of the id attribute of the ManagedClient policy assertion.
     */
    private static final QName ID_ATTRIBUTE_QNAME = new QName("", "id");
    /**
     * The name of the start attribute of the ManagedClient policy assertion.
     */
    private static final QName START_ATTRIBUTE_QNAME = new QName("start");
    private static final QName MANAGEMENT_ATTRIBUTE_QNAME = new QName("management");
    private static final QName MONITORING_ATTRIBUTE_QNAME = new QName("monitoring");

    public ManagedClientAssertionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getId method, of class ManagedClientAssertion.
     * @throws AssertionCreationException
     */
    public void testGetId() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        String expResult = "id1";
        String result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getId method, of class ManagedClientAssertion.
     * @throws AssertionCreationException 
     */
    public void testNoId() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final String result = instance.getId();
        assertNull(result);
    }

    /**
     * Test of getId method, of class ManagedClientAssertion.
     * @throws AssertionCreationException
     */
    public void testNoIdManagementDisabled() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "false");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        String result = instance.getId();
        assertNull(result);
    }

    /**
     * Test of getStart method, of class ManagedClientAssertion.
     * @throws AssertionCreationException
     */
    public void testGetStart() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(START_ATTRIBUTE_QNAME, "notify");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        String expResult = "notify";
        String result = instance.getStart();
        assertEquals(expResult, result);
    }

    public void testIsManagementEnabled() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertFalse(result);
    }

    public void testIsManagementEnabledTrue() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "true");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertFalse(result);
    }

    public void testIsManagementEnabledOn() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "on");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertFalse(result);
    }

    public void testIsManagementEnabledFalse() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "false");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertFalse(result);
    }

    public void testIsManagementEnabledOff() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "off");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertFalse(result);
    }

    public void testMonitoringAttribute() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(result, Setting.NOT_SET);
    }

    public void testMonitoringAttributeTrue() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MONITORING_ATTRIBUTE_QNAME, "true");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(result, Setting.ON);
    }

    public void testMonitoringAttributeOn() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MONITORING_ATTRIBUTE_QNAME, "on");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(result, Setting.ON);
    }

    public void testMonitoringAttributeFalse() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MONITORING_ATTRIBUTE_QNAME, "false");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(result, Setting.OFF);
    }

    public void testMonitoringAttributeOff() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MONITORING_ATTRIBUTE_QNAME, "off");
        final AssertionData data = AssertionData.createAssertionData(ManagedClientAssertion.MANAGED_CLIENT_QNAME,
                null, attributes, false, false);
        final ManagedClientAssertion instance = new ManagedClientAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(result, Setting.OFF);
    }

}
