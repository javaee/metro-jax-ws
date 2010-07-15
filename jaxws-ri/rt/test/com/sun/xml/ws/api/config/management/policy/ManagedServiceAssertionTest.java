/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion.ImplementationRecord;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion.NestedParameters;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.SimpleAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagedServiceAssertionTest extends TestCase {
    /**
     * The name of the id attribute of the ManagedService policy assertion.
     */
    private static final QName ID_ATTRIBUTE_QNAME = new QName("", "id");
    /**
     * The name of the start attribute of the ManagedService policy assertion.
     */
    private static final QName START_ATTRIBUTE_QNAME = new QName("start");
    private static final QName MANAGEMENT_ATTRIBUTE_QNAME = new QName("management");
    private static final QName MONITORING_ATTRIBUTE_QNAME = new QName("monitoring");
    private static final QName ENDPOINT_DISPOSE_DELAY_ATTRIBUTE_QNAME = new QName("endpointDisposeDelay");

    private static final QName COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "CommunicationServerImplementations");
    private static final QName COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "CommunicationServerImplementation");
    private static final QName CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfiguratorImplementation");
    private static final QName CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfigSaverImplementation");
    private static final QName CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfigReaderImplementation");
    private static final QName CLASS_NAME_ATTRIBUTE_QNAME = new QName("className");

    public ManagedServiceAssertionTest(String testName) {
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
     * Test of getId method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetId() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        String expResult = "id1";
        String result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getId method, of class ManagedServiceAssertion.
     */
    public void testNoId() {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        try {
            ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
            fail("Expected AssertionCreationException because the ServerManagedServiceAssertion requires an id attribute.");
        } catch (AssertionCreationException e) {
            // expected
        }
    }

    /**
     * Test of getId method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testNoIdManagementDisabled() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "false");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        String result = instance.getId();
        assertNull(result);
    }

    /**
     * Test of getStart method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetStart() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(START_ATTRIBUTE_QNAME, "notify");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        String expResult = "notify";
        String result = instance.getStart();
        assertEquals(expResult, result);
    }

    public void testIsManagementEnabled() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertTrue(result);
    }

    public void testIsManagementEnabledTrue() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "true");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertTrue(result);
    }

    public void testIsManagementEnabledOn() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "on");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertTrue(result);
    }

    public void testIsManagementEnabledFalse() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "false");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertFalse(result);
    }

    public void testIsManagementEnabledOff() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MANAGEMENT_ATTRIBUTE_QNAME, "off");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final boolean result = instance.isManagementEnabled();
        assertFalse(result);
    }

    public void testMonitoringAttribute() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(Setting.NOT_SET, result);
    }

    public void testMonitoringAttributeTrue() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MONITORING_ATTRIBUTE_QNAME, "true");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(Setting.ON, result);
    }

    public void testMonitoringAttributeOn() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MONITORING_ATTRIBUTE_QNAME, "on");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(Setting.ON, result);
    }

    public void testMonitoringAttributeFalse() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MONITORING_ATTRIBUTE_QNAME, "false");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame(Setting.OFF, result);
    }

    public void testMonitoringAttributeOff() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(MONITORING_ATTRIBUTE_QNAME, "off");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final Setting result = instance.monitoringAttribute();
        assertSame( Setting.OFF, result);
    }

    public void testEndpointDisposeDelayDefault() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final long result = instance.getEndpointDisposeDelay(2000l);
        assertEquals(2000l, result);
    }

    public void testEndpointDisposeDelayValue() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(ENDPOINT_DISPOSE_DELAY_ATTRIBUTE_QNAME, "4500");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        final long result = instance.getEndpointDisposeDelay(2000l);
        assertEquals(4500l, result);
    }

    public void testEndpointDisposeDelayInvalid() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(ENDPOINT_DISPOSE_DELAY_ATTRIBUTE_QNAME, "not an integer");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        try {
            final long result = instance.getEndpointDisposeDelay(2000l);
            fail("Expected a WebserviceException, instead got this value: \"" + result + '"');
        } catch (WebServiceException e) {
            // Expected this exception
        }
    }

    /**
     * Test of getCommunicationServerImplementations method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetCommunicationServerImplementations() throws AssertionCreationException {
        final HashMap<QName, String> implementationAttributes = new HashMap<QName, String>();
        implementationAttributes.put(CLASS_NAME_ATTRIBUTE_QNAME, "CommunicationServerTestClass");
        final AssertionData implementationData = AssertionData.createAssertionData(
                COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME, null, implementationAttributes, false, false);
        final PolicyAssertion implementationParameter = new SimpleAssertion(implementationData, null) { };

        final LinkedList<PolicyAssertion> implementationsParameters = new LinkedList<PolicyAssertion>();
        implementationsParameters.add(implementationParameter);
        final AssertionData implementationsData = AssertionData.createAssertionData(
                COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME, null, null, false, false);
        final PolicyAssertion implementationsParameter = new SimpleAssertion(implementationsData, implementationsParameters) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(implementationsParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<QName, String> expMap = new HashMap<QName, String>();
        final ImplementationRecord expResult = new ImplementationRecord(
                "CommunicationServerTestClass", expMap, new LinkedList<NestedParameters>());
        final Collection<ImplementationRecord> records = instance.getCommunicationServerImplementations();
        assertEquals(1, records.size());
        final ImplementationRecord record = records.iterator().next();
        assertEquals(expResult, record);
    }

    /**
     * Test of getConfiguratorImplementation method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetConfiguratorImplementation() throws AssertionCreationException {
        final HashMap<QName, String> configuratorAttributes = new HashMap<QName, String>();
        configuratorAttributes.put(CLASS_NAME_ATTRIBUTE_QNAME, "ConfiguratorTestClass");
        final AssertionData configuratorData = AssertionData.createAssertionData(
                CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME, null, configuratorAttributes, false, false);
        final PolicyAssertion configuratorParameter = new SimpleAssertion(configuratorData, null) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(configuratorParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<QName, String> expMap = new HashMap<QName, String>();
        ImplementationRecord expResult = new ImplementationRecord(
                "ConfiguratorTestClass", expMap, new LinkedList<NestedParameters>());
        ImplementationRecord result = instance.getConfiguratorImplementation();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConfigSaverImplementation method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetConfigSaverImplementation() throws AssertionCreationException {
        final HashMap<QName, String> configSaverAttributes = new HashMap<QName, String>();
        configSaverAttributes.put(CLASS_NAME_ATTRIBUTE_QNAME, "ConfigSaverTestClass");
        final AssertionData configSaverData = AssertionData.createAssertionData(
                CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME, null, configSaverAttributes, false, false);
        final PolicyAssertion configSaverParameter = new SimpleAssertion(configSaverData, null) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(configSaverParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<QName, String> expMap = new HashMap<QName, String>();
        ImplementationRecord expResult = new ImplementationRecord(
                "ConfigSaverTestClass", expMap, new LinkedList<NestedParameters>());
        ImplementationRecord result = instance.getConfigSaverImplementation();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConfigReaderImplementation method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetConfigReaderImplementation() throws AssertionCreationException {
        final HashMap<QName, String> configReaderAttributes = new HashMap<QName, String>();
        configReaderAttributes.put(CLASS_NAME_ATTRIBUTE_QNAME, "ConfigReaderTestClass");
        final AssertionData configReaderData = AssertionData.createAssertionData(
                CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME, null, configReaderAttributes, false, false);
        final PolicyAssertion configReaderParameter = new SimpleAssertion(configReaderData, null) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(configReaderParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<QName, String> expMap = new HashMap<QName, String>();
        ImplementationRecord expResult = new ImplementationRecord(
                "ConfigReaderTestClass", expMap, new LinkedList<NestedParameters>());
        ImplementationRecord result = instance.getConfigReaderImplementation();
        assertEquals(expResult, result);
    }

    public void testGetConfigReaderImplementationJdbcDataSourceName() throws AssertionCreationException {
        final AssertionData parameterData = AssertionData.createAssertionData(
                new QName("parameter"), "source1", null, false, false);
        final LinkedList<PolicyAssertion> configReaderParameters = new LinkedList<PolicyAssertion>();
        configReaderParameters.add(new SimpleAssertion(parameterData, null) { });

        final AssertionData configReaderData = AssertionData.createAssertionData(
                CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME, null, null, false, false);
        final PolicyAssertion configReaderParameter = new SimpleAssertion(configReaderData, configReaderParameters) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(configReaderParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<String, String> expMap = new HashMap<String, String>();
        ImplementationRecord implementation = instance.getConfigReaderImplementation();
        Map<QName, String> parameters = implementation.getParameters();
        String expResult = "source1";
        String result = parameters.get(new QName("parameter"));
        assertEquals(expResult, result);
    }

    public void testGetCommunicationServerJmxServerUrl() throws AssertionCreationException {
        final QName jmxServerUrlName = new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JmxServerUrl");
        final AssertionData environmentData = AssertionData.createAssertionData(
                jmxServerUrlName, "http://localhost/", null, false, false);
        final LinkedList<PolicyAssertion> environmentParameters = new LinkedList<PolicyAssertion>();
        environmentParameters.add(new SimpleAssertion(environmentData, null) { });

        final AssertionData commServerData = AssertionData.createAssertionData(
                COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME, null, null, false, false);
        final LinkedList<PolicyAssertion> commServerParameters = new LinkedList<PolicyAssertion>();
        commServerParameters.add(new SimpleAssertion(commServerData, environmentParameters) { });

        final AssertionData commServersData = AssertionData.createAssertionData(
                COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME, null, null, false, false);
        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(new SimpleAssertion(commServersData, commServerParameters) { });

        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final Collection<ImplementationRecord> implementations = instance.getCommunicationServerImplementations();
        final ImplementationRecord implementation = implementations.iterator().next();
        final Map<QName, String> result = implementation.getParameters();
        final Map<QName, String> expResult = new HashMap<QName, String>();
        expResult.put(jmxServerUrlName, "http://localhost/");
        assertEquals(expResult, result);
    }

    public void testGetCommunicationServerConnectorServerEnvironment() throws AssertionCreationException {
        final QName parameterName = new QName("ParameterName");
        final AssertionData nestedData = AssertionData.createAssertionData(
                parameterName, "parameterValue", null, false, false);
        final LinkedList<PolicyAssertion> nestedAssertions = new LinkedList<PolicyAssertion>();
        nestedAssertions.add(new SimpleAssertion(nestedData, null) { });

        final AssertionData environmentData = AssertionData.createAssertionData(
                new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JmxConnectorServerEnviroment"), null, null, false, false);
        final LinkedList<PolicyAssertion> environmentParameters = new LinkedList<PolicyAssertion>();
        environmentParameters.add(new SimpleAssertion(environmentData, nestedAssertions) { });

        final AssertionData commServerData = AssertionData.createAssertionData(
                COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME, null, null, false, false);
        final LinkedList<PolicyAssertion> commServerParameters = new LinkedList<PolicyAssertion>();
        commServerParameters.add(new SimpleAssertion(commServerData, environmentParameters) { });

        final AssertionData commServersData = AssertionData.createAssertionData(
                COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME, null, null, false, false);
        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(new SimpleAssertion(commServersData, commServerParameters) { });

        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final Collection<ImplementationRecord> implementations = instance.getCommunicationServerImplementations();
        final ImplementationRecord implementation = implementations.iterator().next();
        final Collection<NestedParameters> nestedParameters = implementation.getNestedParameters();
        final NestedParameters nestedParameter = nestedParameters.iterator().next();
        final QName nestedName = nestedParameter.getName();
        assertEquals("JmxConnectorServerEnviroment", nestedName.getLocalPart());
        final Map<QName, String> result = nestedParameter.getParameters();
        final Map<QName, String> expResult = new HashMap<QName, String>();
        expResult.put(parameterName, "parameterValue");
        assertEquals(expResult, result);
    }

}