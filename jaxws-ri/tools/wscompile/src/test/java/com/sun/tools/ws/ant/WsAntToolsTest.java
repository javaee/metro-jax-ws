/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.tools.ant.types.CommandlineJava;

/**
 *
 * @author lukas
 */
public class WsAntToolsTest extends TestCase {

    public WsAntToolsTest(String testName) {
        super(testName);
    }

    public void testWsGenForkedCommand() {
        String method = "setupForkCommand";
        String field = "cmd";
        String arg = "";
        //API jars are somewhere on the classpath
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "wsi2test");
        WsGen2 wsg2 = new WsGen2();
        wsg2.setFork(true);
        wsg2.setDestdir(new File(tmpDir, "dest"));
        wsg2.setSourcedestdir(new File(tmpDir, "srcDest"));
        CommandlineJava cmd = (CommandlineJava) run(WsGen2.class, wsg2, method, arg, field);
        verifyCommand(cmd.describeCommand());

        //API jars are defined using CLASSPATH environment variable (= System class loader)
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
            wsg2 = new WsGen2();
            wsg2.setFork(true);
            wsg2.setDestdir(new File(tmpDir, "dest"));
            wsg2.setSourcedestdir(new File(tmpDir, "srcDest"));
            cmd = (CommandlineJava) run(WsGen2.class, wsg2, method, arg, field);
            verifyCommand(cmd.describeCommand());
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public void testWsImportForkedCommand() {
        String method = "setupForkCommand";
        String field = "cmd";
        String arg = "";
        //API jars are somewhere on the classpath
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "wsi2test");
        WsImport2 wsi2 = new WsImport2();
        wsi2.setFork(true);
        wsi2.setDestdir(new File(tmpDir, "dest"));
        wsi2.setSourcedestdir(new File(tmpDir, "srcDest"));
        CommandlineJava cmd = (CommandlineJava) run(WsImport2.class, wsi2, method, arg, field);
        verifyCommand(cmd.describeCommand());

        //API jars are defined using CLASSPATH environment variable (= System class loader)
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
            wsi2 = new WsImport2();
            wsi2.setFork(true);
            wsi2.setDestdir(new File(tmpDir, "dest"));
            wsi2.setSourcedestdir(new File(tmpDir, "srcDest"));
            cmd = (CommandlineJava) run(WsImport2.class, wsi2, method, arg, field);
            verifyCommand(cmd.describeCommand());
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private Object run(Class<?> c, Object i, String method, String arg, String field) {
        runVoidMethod(c, i, method, arg);
        return getField(c, i, field);
    }

    private void verifyCommand(String command) {
        if (!WsAntTaskTestBase.is9()) {
            Assert.assertTrue("-Xbootclasspath/p not set: " + command, command.contains("-Xbootclasspath/p"));
        }

        String v = System.getProperty("jaxb-api.version");
        String jar = v != null ? "jaxb-api-" + v + ".jar" : "jaxb-api.jar";
        jar = fixIfSNAPSHOT(jar);
        Assert.assertTrue(jar + " not found " + command, command.contains(jar));

        v = System.getProperty("jaxws-api.version");
        jar = v != null ? "jaxws-api-" + v + ".jar" : "jaxws-api.jar";
        jar = fixIfSNAPSHOT(jar);
        Assert.assertTrue(jar + " not found " + command, command.contains(jar));
    }

    // translate maven timestamps to SNAPSHOT:
    //  jaxb-api-2.3.0-20150602.094817-2.jar
    //    >>
    //  jaxb-api-2.3.0-SNAPSHOT.jar
    private String fixIfSNAPSHOT(String jar) {
        return jar.replaceAll("(.*)-(\\d+\\.\\d+\\.\\d+)\\-?(\\d+\\.\\d+-\\d+).jar", "$1-$2-SNAPSHOT.jar");
    }


    private void runVoidMethod(Class<?> c, Object i, String name, String arg) {
        Method m = null;
        Class parent = c;
        do {
            try {
                m = parent.getDeclaredMethod(name, String.class);
                m.setAccessible(true);
                m.invoke(i, arg);
                return;
            } catch (Throwable t) {
                parent = parent.getSuperclass();
            } finally {
                if (m != null) {
                    m.setAccessible(false);
                }
            }
        } while (m == null && parent.getSuperclass() != null);
        if (m == null) {
            Assert.fail("cannot find method: '" + name + "'");
        }
    }

    private Object getField(Class<?> c, Object i, String name) {
        Field f = null;
        Class parent = c.getSuperclass();
        do {
            try {
                f = parent.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(i);
            } catch (Throwable t) {
                parent = parent.getSuperclass();
            } finally {
                if (f != null) {
                    f.setAccessible(false);
                }
            }
        } while (f == null && parent.getSuperclass() != null);
        if (f == null) {
            Assert.fail("cannot find field: '" + name + "'");
        }
        return null;
    }
}
