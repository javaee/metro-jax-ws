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

package com.sun.xml.ws.assembler;

import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;
import junit.framework.TestCase;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class MetroConfigLoaderTest extends TestCase {

    private static final class TestDataInfo {

        final String appConfigFileName;
        final int expectedTubes;

        public TestDataInfo(String appConfigFileName, int expectedTubes) {
            this.appConfigFileName = appConfigFileName;
            this.expectedTubes = expectedTubes;
        }
    }
    private static final TestDataInfo[] APP_METRO_CONFIGS = new TestDataInfo[]{
        new TestDataInfo("jaxws-tubes.xml", 12),
        new TestDataInfo("jaxws-tubes-no-default.xml", 13),
        new TestDataInfo("jaxws-tubes-no-default-single-tube.xml", 1)
    };

    public MetroConfigLoaderTest(String testName) {
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
     * Test of getTubelineForEndpoint method, of class MetroConfigLoader.
     */
    public void testGetEndpointSideTubeFactoriesTest() throws URISyntaxException {
        TestDataInfo tdi = APP_METRO_CONFIGS[0];
        MetroConfigLoader configLoader = new MetroConfigLoader(MockupMetroConfigLoader.createMockupContainer(tdi.appConfigFileName),
                MetroTubelineAssembler.JAXWS_TUBES_CONFIG_NAMES);

        TubeFactoryList result;
        result = configLoader.getEndpointSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/HttpPingPort)"));
        assertTrue(containsTubeFactoryConfig(result, "server"));
        assertEquals(tdi.expectedTubes, result.getTubeFactoryConfigs().size());

        result = configLoader.getEndpointSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/JmsPingPort)"));
        assertTrue(containsTubeFactoryConfig(result, "default-server"));
        assertEquals(tdi.expectedTubes, result.getTubeFactoryConfigs().size());

        result = configLoader.getEndpointSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/OtherPingPort)"));
        assertTrue(containsTubeFactoryConfig(result, "default-server"));
        assertEquals(tdi.expectedTubes, result.getTubeFactoryConfigs().size());
    }

    /**
     * Test of getTubelineForEndpoint method, of class MetroConfigLoader.
     */
    public void testGetClientSideTubeFactoriesTest() throws URISyntaxException {
        TestDataInfo tdi = APP_METRO_CONFIGS[0];
        MetroConfigLoader configLoader = new MetroConfigLoader(MockupMetroConfigLoader.createMockupContainer(tdi.appConfigFileName),
                        MetroTubelineAssembler.JAXWS_TUBES_CONFIG_NAMES);

        TubeFactoryList result;
        result = configLoader.getClientSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/HttpPingPort)"));
        assertTrue(containsTubeFactoryConfig(result, "default-client"));
        assertEquals(tdi.expectedTubes, result.getTubeFactoryConfigs().size());

        result = configLoader.getClientSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/JmsPingPort)"));
        assertTrue(containsTubeFactoryConfig(result, "client"));
        assertEquals(tdi.expectedTubes, result.getTubeFactoryConfigs().size());

        result = configLoader.getClientSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/OtherPingPort)"));
        assertTrue(containsTubeFactoryConfig(result, "default-client"));
        assertEquals(tdi.expectedTubes, result.getTubeFactoryConfigs().size());
    }

    /**
     * Test of getTubelineForEndpoint method, of class MetroConfigLoader - loading from default Metro config
     */
    public void testGetEndpointSideTubeFactoriesLoadFromDefaultConfig() throws URISyntaxException {
        for (int i = 1; i < APP_METRO_CONFIGS.length; i++) {
            TestDataInfo tdi = APP_METRO_CONFIGS[i];

            MetroConfigLoader configLoader = new MetroConfigLoader(MockupMetroConfigLoader.createMockupContainer(tdi.appConfigFileName),
                            MetroTubelineAssembler.JAXWS_TUBES_CONFIG_NAMES);

            TubeFactoryList result;
            result = configLoader.getEndpointSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/HttpPingPort)"));
            assertTrue(containsTubeFactoryConfig(result, "server"));
            assertEquals(tdi.expectedTubes, result.getTubeFactoryConfigs().size());

            result = configLoader.getEndpointSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/JmsPingPort)"));
            assertFalse(result.getTubeFactoryConfigs().isEmpty());
            assertFalse(containsTubeFactoryConfig(result, "server"));

            result = configLoader.getEndpointSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/OtherPingPort)"));
            assertFalse(result.getTubeFactoryConfigs().isEmpty());
            assertFalse(containsTubeFactoryConfig(result, "server"));
        }
    }

    /**
     * Test of getTubelineForEndpoint method, of class MetroConfigLoader - loading from default Metro config
     */
    public void testGetClientSideTubeFactoriesLoadFromDefaultConfig() throws URISyntaxException {
        for (int i = 1; i < APP_METRO_CONFIGS.length; i++) {
            TestDataInfo tdi = APP_METRO_CONFIGS[i];

            MetroConfigLoader configLoader = new MetroConfigLoader(MockupMetroConfigLoader.createMockupContainer(tdi.appConfigFileName),
                            MetroTubelineAssembler.JAXWS_TUBES_CONFIG_NAMES);

            TubeFactoryList result;
            result = configLoader.getClientSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/HttpPingPort)"));
            assertFalse(result.getTubeFactoryConfigs().isEmpty());
            assertFalse(containsTubeFactoryConfig(result, "client"));

            result = configLoader.getClientSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/JmsPingPort)"));
            assertTrue(containsTubeFactoryConfig(result, "client"));
            assertEquals(tdi.expectedTubes, result.getTubeFactoryConfigs().size());

            result = configLoader.getClientSideTubeFactories(new URI("http://org.sample#wsdl11.port(PingService/OtherPingPort)"));
            assertFalse(result.getTubeFactoryConfigs().isEmpty());
            assertFalse(containsTubeFactoryConfig(result, "client"));
        }
    }

    private boolean containsTubeFactoryConfig(TubeFactoryList tubeList, String tubeFactoryName) {
        for (TubeFactoryConfig config : tubeList.getTubeFactoryConfigs()) {
            if (config.getClassName().equals(tubeFactoryName)) {
                return true;
            }
        }

        return false;
    }
}
