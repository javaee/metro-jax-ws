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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.tools.ant.launch.Launcher;

/**
 *
 * @author Lukas Jungmann
 */
public class AntExecutor {

    private static final File JAX_WS_RI_HOME = init();
    private static boolean DEBUG = Boolean.getBoolean("anttasks.debug");
    private static String DEBUG_PORT = "5432";
    private static boolean PROFILE = Boolean.getBoolean("anttasks.profile");

    private static File init() {
        if (System.getProperty("jaxws-ri.home") != null) {
            return new File(System.getProperty("jaxws-ri.home"));
        }
        try {
            //see: https://issues.apache.org/bugzilla/show_bug.cgi?id=20174
            //File f = new File(AntExecutor.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File f = new File(AntExecutor.class.getResource("AntExecutor.class").toURI());
            while (!"wscompile".equals(f.getName())) {
                f = f.getParentFile();
            }
            return f.getParentFile().getParentFile();
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    public static int exec(File script, File endorsedDir, String... targets) throws IOException {
        File launcher = null;
        try {
            launcher = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            //should not happen
            return -1;
        }
        File heapDump = null;
        List<String> cmd = new ArrayList<String>();
        cmd.add("java");
        if (DEBUG) {
            cmd.add("-Xdebug");
            cmd.add("-Xnoagent");
            cmd.add("-Xrunjdwp:transport=dt_socket,address=" + DEBUG_PORT + ",server=y,suspend=y");
        } else if (PROFILE) {
            heapDump = File.createTempFile(script.getName(), ".hprof", new File(System.getProperty("user.home")));
            cmd.add("-agentlib:hprof=heap=dump,file=" + heapDump.getAbsolutePath() + ",format=b");
        }
        cmd.add("-Dant.home=" + launcher.getParentFile().getParentFile().getAbsolutePath());
        cmd.add("-Djava.endorsed.dirs=" + endorsedDir.getAbsolutePath());
        cmd.add("-Djaxp.debug=true");
        cmd.add("-cp");
        cmd.add(launcher.getAbsolutePath());
        cmd.add("org.apache.tools.ant.launch.Launcher");
        cmd.add("-v");
        cmd.add("-f");
        cmd.add(script.getName());
        cmd.add("-Djaxws-ri.home=" + JAX_WS_RI_HOME.getAbsolutePath());
        cmd.addAll(Arrays.asList(targets));
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(script.getParentFile());
        Process p = pb.start();
        new InOut(p.getInputStream(), System.out).start();
        new InOut(p.getErrorStream(), System.out).start();
        try {
            p.waitFor();
        } catch (InterruptedException ex) {
            // ignore
        }
        if (PROFILE) {
            System.out.println("Heap dump (in binary format): " + heapDump.getAbsolutePath());
        }
        return p.exitValue();
    }

    private static class InOut extends Thread {

        private InputStream is;
        private OutputStream out;

        public InOut(InputStream is, OutputStream out) {
            this.is = is;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                byte[] b = new byte[8096];
                int len = -1;
                while ((len = is.read(b)) > 0) {
                    out.write(b, 0, len);
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
