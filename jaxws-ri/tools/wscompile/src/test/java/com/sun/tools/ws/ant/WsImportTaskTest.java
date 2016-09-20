/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2016 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.tools.ws.ant;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Lukas Jungmann
 */
public class WsImportTaskTest extends WsAntTaskTestBase {

    private File wsdl;
    private static final File pkg = new File(srcDir, "test");
    private static final File metainf = new File(buildDir, "META-INF");

    @Override
    public String getBuildScript() {
        return "wsimport.xml";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wsdl = copy(projectDir, "hello.wsdl", WsImportTaskTest.class.getResourceAsStream("resources/hello.wsdl"));
        assertTrue(pkg.mkdirs());
        assertTrue(metainf.mkdirs());
    }

    @Override
    protected void tearDown() throws Exception {
        wsdl.delete();
        super.tearDown();
    }

    public void testWsImportLockJars() throws IOException, URISyntaxException {
        if (isOldJDK()) {
            Logger.getLogger(WsImportTaskTest.class.getName()).warning("Old JDK - 6+ is required - skipping jar locking test");
            return;
        }
        if (isAntPre18()) {
            Logger.getLogger(WsImportTaskTest.class.getName()).warning("Old Ant - 1.8+ is required - skipping jar locking test");
            return;
        }
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsimport-client", "clean"));
        List<String> files = listDirs(apiDir, libDir);
        assertTrue("Locked jars: " + files, files.isEmpty());
    }

    /**
     * Verify (@code module-info.java} generation with JDK9.
     */
    /*public void testWsImportModuleGeneration() throws IOException, URISyntaxException {
        // TODO: JDK 9
        if (ModuleHelper.isModularJDK()) {
            assertEquals(0, AntExecutor.exec(script, apiDir, "wsimport-client-module", "clean"));
        }
    }*/

    public void testWsImportLockJarURLs() throws IOException, URISyntaxException {
        if (isAntPre18()) {
            Logger.getLogger(WsImportTaskTest.class.getName()).warning("Old Ant - 1.8+ is required - skipping jar locking test");
            return;
        }
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsimport-client-jarurl", "clean"));
        List<String> files = listDirs(apiDir, libDir);
        assertTrue("Locked jars: " + files, files.isEmpty());
    }

    public void testEncoding() throws IOException {
        //this fails because one task uses invalid attributte
        assertEquals(1, AntExecutor.exec(script, apiDir, "wsimport-client-encoding"));
        //UTF-8
        File f = new File(buildDir, "client/utf8/Hello.java");
        FileInputStream fis = new FileInputStream(f);
        byte[] in = new byte[11];
        fis.read(in);
        fis.close();
        String inStr = new String(in, "UTF-8");
        assertTrue("Got: '" + inStr + "'", inStr.contains("package c"));

        //UTF-16LE
        f = new File(buildDir, "client/utf16LE/Hello.java");
        fis = new FileInputStream(f);
        in = new byte[22];
        fis.read(in);
        fis.close();
        inStr = new String(in, "UTF-16LE");
        assertTrue("Got: '" + inStr + "'", inStr.contains("package c"));

        //UTF-74
        assertFalse(new File(buildDir, "client/invalid").exists());
    }

    public void testPlugin() throws IOException {
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsimport-plugin"));
        File f = new File(buildDir, "test/Hello_Service.java");
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        boolean found = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("@Generated(value = \"com.sun.tools.ws.wscompile.WsimportTool\", ")) {
                found = true;
                break;
            }
        }
        br.close();
        assertFalse("Plugin invoked", found);
        f = new File(srcDir, "test/Hello_Service.java");
        br = new BufferedReader(new FileReader(f));
        while ((line = br.readLine()) != null) {
            if (line.contains("@Generated(value = \"com.sun.tools.ws.wscompile.WsimportTool\", ")) {
                found = true;
                break;
            }
        }
        br.close();
        assertTrue("Plugin not invoked", found);
    }

    public void testFork() throws FileNotFoundException, IOException {
        copy(pkg,  "MyExtension.java", WsImportTaskTest.class.getResourceAsStream("resources/MyExtension.java_"));
        copy(buildDir,  "META-INF/com.sun.tools.ws.api.wsdl.TWSDLExtensionHandler", WsImportTaskTest.class.getResourceAsStream("resources/TWSDLExtensionHandler"));
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsimport-fork"));
    }

    public void testJavac() throws IOException {
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsimport-javac"));
        //wsimport compiled classes should be valid for java 5
        File f = new File(buildDir, "test/types/HelloType.class");
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        assertEquals(0xcafebabe, in.readInt());
        assertEquals(0, in.readUnsignedShort());
        assertEquals(49, in.readUnsignedShort());

        f = new File(buildDir, "test/Hello.class");
        in = new DataInputStream(new FileInputStream(f));
        assertEquals(0xcafebabe, in.readInt());
        assertEquals(0, in.readUnsignedShort());
        assertEquals(49, in.readUnsignedShort());
    }

}
