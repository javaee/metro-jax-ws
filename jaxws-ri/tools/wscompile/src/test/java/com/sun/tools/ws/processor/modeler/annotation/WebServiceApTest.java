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

package com.sun.tools.ws.processor.modeler.annotation;

import junit.framework.Assert;
import junit.framework.TestCase;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests annotation processor, when run outside wsgen - without {@link com.sun.tools.ws.wscompile.WsgenOptions} passed through constructor.
 *
 * @author Roman Grigoriadi
 */
public class WebServiceApTest extends TestCase {

    private URI executionPath;

    @Override
    protected void setUp() throws Exception {
        try {
            final URL executionUrl = WebServiceApTest.class.getResource(".");
            executionPath = executionUrl.toURI();
        } catch (URISyntaxException e) {
            fail("Cant resolve execution path: "+executionPath);
        }
    }

    /**
     * Test if -Averbose=true is propagated from javac to processor.
     */
    public void testVerboseArg() {
        final List<String> args = new ArrayList<>();
        args.add("-Averbose=true");

        final WebServiceAp ap = new WebServiceAp();
        final boolean result = runCompiler(args, ap);

        Assert.assertTrue(ap.getOptions().verbose);
        Assert.assertTrue(result);
    }

    /**
     * Tests -s is used by annotation processor code generation.
     */
    public void testOutputDirectory() {
        String path = executionPath.getPath();
        path = path.substring(0, path.lastIndexOf("test-classes"));
        File generatedSourceDir = new File(path + "generated");
        if (!generatedSourceDir.exists()) {
            if (!generatedSourceDir.mkdir()) {
                fail("Generated source directory cannot be created");
            }
        }

        final List<String> args = new ArrayList<>();

        args.add("-s");
        args.add(generatedSourceDir.getAbsolutePath());

        final WebServiceAp ap = new WebServiceAp();
        final boolean result = runCompiler(args, ap);

        Assert.assertTrue(result);
        File generatedSourceDirFiles = new File(generatedSourceDir.toURI().getPath() + "com/sun/tools/ws/processor/modeler/annotation/jaxws");
        final File[] contents = generatedSourceDirFiles.listFiles();
        Assert.assertTrue(4 == contents.length);

        final List<String> expectedSourceNames = Arrays.asList("SayHello.java", "SayHello.class", "SayHelloResponse.java", "SayHelloResponse.class");
        for (File generated : contents) {
            Assert.assertTrue(expectedSourceNames.contains(generated.getName()));
        }
    }

    private boolean runCompiler(List<String> args, WebServiceAp ap) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        File endpointJavaFile = new File(executionPath.getPath() + "EndpointJavaSource.java");
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                args,
                null,
                fileManager.getJavaFileObjects(endpointJavaFile));

        task.setProcessors(Collections.singleton(ap));

        return task.call();
    }

}
