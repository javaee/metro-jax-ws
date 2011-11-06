/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.tools.ws.ant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

/**
 *
 * @author Lukas Jungmann
 */
public class ApTest extends TestCase {

    public void testSetupAptArgs() throws URISyntaxException {
        Ap ap = getAp();
        String vmArg = "-Xmx2048M";
        String testedMethod = "setupAptArgs";
        ap.setFork(false);
        ap.createCompilerArg().setValue(vmArg);
        Commandline cmd = getCommandLine(testedMethod, ap);
        assertTrue("Option for runtime system must be prefixed with '-J'", containsArgument(cmd.getArguments(), "-J" + vmArg));
        ap.setFork(true);
        cmd = getCommandLine(testedMethod, ap);
        assertFalse("Option for forked process should not be prefixed with '-J'", containsArgument(cmd.getArguments(), "-J" + vmArg));
    }

    private Ap getAp() {
        Ap ap = new Ap();
        Project p = new Project();
        p.init();
        Path src = new Path(p);
        src.setPath(System.getProperty("java.io.tmpdir"));
        ap.setSourcepath(src);
        return ap;
    }
    
    private static Commandline getCommandLine(String name, Object a) {
        final Method m = getMethod(name);
        assertNotNull(m);
        try {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                public Void run() {
                    m.setAccessible(true);
                    return null;
                }
            });

            return (Commandline) m.invoke(a);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ApTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ApTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ApTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (m != null && m.isAccessible()) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {

                    public Void run() {
                        m.setAccessible(false);
                        return null;
                    }
                });
            }
        }
        return null;
    }

    private static Method getMethod(String name) {
        try {
            return Ap.class.getDeclaredMethod(name);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ApTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ApTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static boolean containsArgument(String[] args, String arg) {
        for (String s : args) {
            if (s.equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
