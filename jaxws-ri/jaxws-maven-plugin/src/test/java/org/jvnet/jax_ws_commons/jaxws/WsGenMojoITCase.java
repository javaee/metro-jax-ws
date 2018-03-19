/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jvnet.jax_ws_commons.jaxws;

import java.io.File;
import java.io.IOException;
import static org.jvnet.jax_ws_commons.jaxws.Assertions.assertFileContains;
import static org.jvnet.jax_ws_commons.jaxws.Assertions.assertFileNotPresent;
import static org.jvnet.jax_ws_commons.jaxws.Assertions.assertFilePresent;
import static org.jvnet.jax_ws_commons.jaxws.Assertions.assertJarContains;
import static org.jvnet.jax_ws_commons.jaxws.Assertions.assertJarNotContains;
import org.testng.annotations.Test;

/**
 *
 * @author Lukas Jungmann
 */
public class WsGenMojoITCase {

    private static final File PROJECTS_DIR = new File(System.getProperty("it.projects.dir"));
    private File project;

    public WsGenMojoITCase() {
    }

    @Test(enabled = false)
    public void wsgen217() throws IOException {
        project = new File(PROJECTS_DIR, "wsgen217");

        //check EchoService
        assertFilePresent(project, "target/custom/sources/org/jvnet/jax_ws_commons/jaxws/test/jaxws/EchoResponse.java");
        assertFilePresent(project, "target/custom/classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/Echo.class");
        assertFilePresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/EchoService.class");
        //-wsdl[...]
        assertFilePresent(project, "target/wsdl/EchoService.wsdl");
        assertFilePresent(project, "target/wsdl/EchoService_schema1.xsd");
        assertFileNotPresent(project, "target/jaxws/wsgen/wsdl/EchoService.wsdl");
        assertFileNotPresent(project, "target/generated-sources/wsdl/EchoService.wsdl");
        assertFileNotPresent(project, "target/generated-sources/test-wsdl/EchoService.wsdl");
        //-wsdl:Xsoap12 + -extension
        assertFileContains(project, "target/wsdl/EchoService.wsdl", "http://schemas.xmlsoap.org/wsdl/soap12/");
        //dependency on 2.1.7
        assertFileContains(project, "target/wsdl/EchoService.wsdl", "JAX-WS RI 2.1.7");

        //check AddService
        assertFilePresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/Add.class");
        assertFilePresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/AddService.class");
        assertFileNotPresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/Add.java");
        assertFileNotPresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/AddService.java");
        assertFileNotPresent(project, "target/wsdl/AddService.wsdl");
        assertFileNotPresent(project, "target/jaxws/wsgen/wsdl/AddService.wsdl");

        //check TService
        assertFilePresent(project, "target/generated-sources/test-wsdl/TService.wsdl");
        assertFilePresent(project, "target/generated-sources/test-wsdl/ExService.wsdl");
        assertFilePresent(project, "target/test-classes/org/jvnet/jax_ws_commons/jaxws/test/TService.class");
        assertFilePresent(project, "target/test-classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/HelloResponse.class");
        assertFilePresent(project, "target/generated-sources/test-wsgen/org/jvnet/jax_ws_commons/jaxws/test/jaxws/HelloResponse.java");
        assertFileNotPresent(project, "target/test-classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/HelloResponse.java");
        //dependency on 2.1.7
        assertFileContains(project, "target/generated-sources/test-wsdl/ExService.wsdl", "JAX-WS RI 2.1.7");
        //-portname
        assertFileContains(project, "target/generated-sources/test-wsdl/ExService.wsdl", "port name=\"ExPort\"");
        //-servicename
        assertFileContains(project, "target/generated-sources/test-wsdl/ExService.wsdl", "service name=\"ExService\"");

        //-encoding is not supported, warning should be present
        assertFileContains(project, "build.log", "'-encoding' is not supported by jaxws-tools:2.1.7");

        //package wsdl
        assertJarContains(project, "mojo.it.wsgentest217-2.2.6.jar", "META-INF/wsdl/EchoService.wsdl");
        assertJarContains(project, "mojo.it.wsgentest217-2.2.6.jar", "META-INF/wsdl/EchoService_schema1.xsd");
        assertJarNotContains(project, "mojo.it.wsgentest217-2.2.6.jar", "META-INF/EchoService_schema.xsd");
        assertJarNotContains(project, "mojo.it.wsgentest217-2.2.6.jar", "EchoService_schema.xsd");
        assertJarNotContains(project, "mojo.it.wsgentest217-2.2.6.jar", "META-INF/wsdl/ExService.wsdl");
        assertJarNotContains(project, "mojo.it.wsgentest217-2.2.6.jar", "ExService.wsdl");
    }

    @Test
    public void wsgen22() throws IOException {
        project = new File(PROJECTS_DIR, "wsgen22");
        String v = System.getProperty("jaxws-ri.version");
        //remove 'promoted-' from the version string if needed
        int i = v.indexOf('-');
        int j = v.lastIndexOf('-');
        String version = i != j ? v.substring(0, i) + v.substring(j) : v;

        //check EchoService
        assertFilePresent(project, "target/custom/sources/org/jvnet/jax_ws_commons/jaxws/test/jaxws/EchoResponse.java");
        assertFilePresent(project, "target/custom/classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/Echo.class");
        assertFilePresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/EchoService.class");
        //-wsdl[...]
        assertFilePresent(project, "target/wsdl/EchoService.wsdl");
        //-inlineSchemas
        assertFileContains(project, "target/wsdl/EchoService.wsdl", "xs:complexType");
        assertFileNotPresent(project, "target/wsdl/EchoService_schema1.xsd");
        assertFileNotPresent(project, "target/jaxws/wsgen/wsdl/EchoService.wsdl");
        assertFileNotPresent(project, "target/generated-sources/wsdl/EchoService.wsdl");
        assertFileNotPresent(project, "target/generated-sources/test-wsdl/EchoService.wsdl");
        //-wsdl:Xsoap12 + -extension
        assertFileContains(project, "target/wsdl/EchoService.wsdl", "http://schemas.xmlsoap.org/wsdl/soap12/");
        //default dependency on 2.2.x
        assertFileContains(project, "target/wsdl/EchoService.wsdl", "JAX-WS RI " + version);

        //check AddService
        assertFilePresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/Add.class");
        assertFilePresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/AddService.class");
        assertFileNotPresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/Add.java");
        assertFileNotPresent(project, "target/classes/org/jvnet/jax_ws_commons/jaxws/test/AddService.java");
        assertFileNotPresent(project, "target/wsdl/AddService.wsdl");
        assertFileNotPresent(project, "target/jaxws/wsgen/wsdl/AddService.wsdl");

        //check TService
        assertFilePresent(project, "target/generated-sources/test-wsdl/TService.wsdl");
        assertFilePresent(project, "target/generated-sources/test-wsdl/ExService.wsdl");
        assertFilePresent(project, "target/test-classes/org/jvnet/jax_ws_commons/jaxws/test/TService.class");
        assertFilePresent(project, "target/test-classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/HelloResponse.class");
        assertFilePresent(project, "target/generated-sources/test-wsgen/org/jvnet/jax_ws_commons/jaxws/test/jaxws/HelloResponse.java");
        assertFileNotPresent(project, "target/test-classes/org/jvnet/jax_ws_commons/jaxws/test/TService.java");
        assertFileNotPresent(project, "target/test-classes/org/jvnet/jax_ws_commons/jaxws/test/jaxws/HelloResponse.java");
        //default dependency on 2.2.x
        assertFileContains(project, "target/generated-sources/test-wsdl/ExService.wsdl", "JAX-WS RI " + version);
        //-portname
        assertFileContains(project, "target/generated-sources/test-wsdl/ExService.wsdl", "port name=\"ExPort\"");
        //-servicename
        assertFileContains(project, "target/generated-sources/test-wsdl/ExService.wsdl", "service name=\"ExService\"");

        //package wsdl
        assertJarContains(project, "mojo.it.wsgentest22-2.2.6.jar", "META-INF/wsdl/EchoService.wsdl");
        assertJarNotContains(project, "mojo.it.wsgentest22-2.2.6.jar", "META-INF/wsdl/EchoService_schema1.xsd");
        assertJarNotContains(project, "mojo.it.wsgentest22-2.2.6.jar", "META-INF/EchoService_schema.xsd");
        assertJarNotContains(project, "mojo.it.wsgentest22-2.2.6.jar", "EchoService_schema.xsd");
        assertJarNotContains(project, "mojo.it.wsgentest22-2.2.6.jar", "META-INF/wsdl/ExService.wsdl");
        assertJarNotContains(project, "mojo.it.wsgentest22-2.2.6.jar", "ExService.wsdl");
    }

    @Test
    public void jaxwscommons43() throws IOException {
        project = new File(PROJECTS_DIR, "jaxwscommons-43");

        assertFilePresent(project, "target/classes/tests/jaxwscommons43/jaxws/Bye.class");
        assertFilePresent(project, "target/generated-sources/wsdl/WsImplAService.wsdl");
        assertFilePresent(project, "target/generated-sources/wsdl/WsImplBService.wsdl");
        assertFileContains(project, "build.log", "No @javax.jws.WebService found.");
        assertFileContains(project, "build.log", "Skipping tests, nothing to do.");
    }

    @Test
    public void jaxwscommons3() throws IOException {
        project = new File(PROJECTS_DIR, "jaxwscommons-3");

        assertFilePresent(project, "target/cst/WEB-INF/wsdl/NewWebService.wsdl");
        assertJarContains(project, "jaxwscommons-3-1.0.war", "WEB-INF/wsdl/NewWebService.wsdl");
    }

    @Test
    public void jaxwscommons91() throws IOException {
        project = new File(PROJECTS_DIR, "jaxwscommons-91");

        assertFilePresent(project, "target/generated-sources/wsgen/com/mycompany/mavenproject2/jaxws/CustomExBean.java");
        assertFilePresent(project, "target/generated-sources/wsdl/NewWebService.wsdl");
    }

    @Test
    public void jaxwscommons103() throws IOException {
        project = new File(PROJECTS_DIR, "jaxwscommons-103/ws");

        assertJarContains(project, "jaxwscommons-103.ws.war", "WEB-INF/wsdl/EchoImplService.wsdl");
        assertJarContains(project, "jaxwscommons-103.ws.war", "WEB-INF/wsdl/EchoImplService_schema1.xsd");
        assertJarContains(project, "jaxwscommons-103.ws.war", "WEB-INF/classes/metadata-echo.xml");
    }

}
