/*
 * DO NOT ALTER OR REMOVE` COPYRIGHT NOTICES OR THIS HEADER.
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Lukas Jungmann
 */
public class WsGenTaskTest extends WsAntTaskTestBase {

    private static final File pkg = new File(srcDir, "test");

    private static final File generated = new File(projectDir, "generated");

    @Override
    public String getBuildScript() {
        return "wsgen.xml";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertTrue(pkg.mkdirs());
        assertTrue(generated.mkdirs());
    }

    @Override
    protected void tearDown() throws Exception {
        delDirs(generated);
        super.tearDown();
    }

    public void testWsGenLockJars() throws IOException, URISyntaxException {
        if (isOldJDK()) {
            Logger.getLogger(WsGenTaskTest.class.getName()).warning("Old JDK - 6+ is required - skipping jar locking test");
            return;
        }
        if (isAntPre18()) {
            Logger.getLogger(WsGenTaskTest.class.getName()).warning("Old Ant - 1.8+ is required - skipping jar locking test");
            return;
        }
        copy(pkg, "TestWs.java", WsGenTaskTest.class.getResourceAsStream("resources/TestWs.java_"));
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsgen-server", "clean"));
        List<String> files = listDirs(apiDir, libDir);
        assertTrue("Locked jars: " + files, files.isEmpty());
    }

    public void testEncoding() throws IOException, URISyntaxException {
        //UTF-16BE
        String enc = "UTF-16BE";
        copy(pkg, "TestWs.java", WsGenTaskTest.class.getResourceAsStream("resources/TestWs.java_"), enc);
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsgen-server-utf16be"));
        File f = new File(srcDir, "test/jaxws/Hello.java");
        FileInputStream fis = new FileInputStream(f);
        byte[] in = new byte[22];
        fis.read(in);
        fis.close();
        String inStr = new String(in, enc);
        assertTrue("Got: '" + inStr + "'", inStr.contains("package t"));
    }

    public void testInvalidEncoding() throws IOException, URISyntaxException {
        assertEquals(1, AntExecutor.exec(script, apiDir, "wsgen-server-encoding-invalid"));
    }

    public void testMemoryArgs() throws IOException, URISyntaxException {
        copy(pkg, "TestWs.java", WsGenTaskTest.class.getResourceAsStream("resources/TestWs.java_"));
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsgen-server-memory"));
    }

    public void testFork() throws IOException, URISyntaxException {
        copy(pkg, "TestWs.java", WsGenTaskTest.class.getResourceAsStream("resources/TestWs.java_"));
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsgen-fork"));
    }

    public void testJavac() throws IOException, URISyntaxException {
        copy(pkg, "TestWs.java", WsGenTaskTest.class.getResourceAsStream("resources/TestWs.java_"));
        assertEquals(0, AntExecutor.exec(script, apiDir, "wsgen-javac"));
        //wsgen compiled classes should be valid for java 5
        File f = new File(buildDir, "test/jaxws/Hello.class");
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        assertEquals(0xcafebabe, in.readInt());
        assertEquals(0, in.readUnsignedShort());
        assertEquals(49, in.readUnsignedShort());

        //ws class is compiled by default javac (6+)
        f = new File(srcDir, "test/TestWs.class");
        in = new DataInputStream(new FileInputStream(f));
        assertEquals(0xcafebabe, in.readInt());
        in.readUnsignedShort();
        assertTrue(49 != in.readUnsignedShort());
    }
}
