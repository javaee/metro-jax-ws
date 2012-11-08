/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;


import junit.framework.TestCase;

public class AptTaskTest extends TestCase {
    public AptTaskTest(String testName) {
        super(testName);
    }
    
    public void testMultipleWs() throws Exception {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "testMultipleWs");
        tmpDir.mkdirs();
        File srcDir = new File(tmpDir, "src");
        srcDir.mkdirs();
        File outDir = new File(tmpDir, "out");
        outDir.mkdirs();
        WsAntTaskTestBase.copy(srcDir, "WSStyle1.java", AptTaskTest.class.getResourceAsStream("resources/WSStyle1.java_"));
        WsAntTaskTestBase.copy(srcDir, "WSStyle2.java", AptTaskTest.class.getResourceAsStream("resources/WSStyle2.java_"));


        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
			
            Project project = new Project();
			project.setBasedir(tmpDir.getPath());
			project.setSystemProperties();
			
			DefaultLogger dl = new DefaultLogger();
			dl.setOutputPrintStream(System.out);
			dl.setErrorPrintStream(System.err);
			dl.setMessageOutputLevel(5);
			project.addBuildListener(dl);
            
			AnnotationProcessingTask apt = new AnnotationProcessingTask();
			apt.setProject(project);
			apt.setDestdir(outDir);
			apt.setSourceDestDir(outDir);
			
			Path sourcepath = new Path(project);
			sourcepath.setLocation(srcDir);
			FileSet jwsFS = new FileSet();
			jwsFS.setDir(tmpDir);
			jwsFS.createInclude().setName("src");
			sourcepath.addFileset(jwsFS);			
			
			apt.setDebug(true);
			
			apt.setSrcdir(sourcepath);
			apt.execute();         
        } finally {
            Thread.currentThread().setContextClassLoader(cl);            
        }
    }
    
}
