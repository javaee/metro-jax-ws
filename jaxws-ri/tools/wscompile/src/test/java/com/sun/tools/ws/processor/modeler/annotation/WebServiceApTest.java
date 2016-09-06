/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.wscompile.WsgenOptions;
import com.sun.tools.ws.wscompile.WsgenTool;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author lukas
 */

public class WebServiceApTest extends TestCase {

    private File destDir;
    private List<String> options;

    public WebServiceApTest(String name) {
        super(name);
    }

    @Override
    public void setUp() {
        destDir = new File(System.getProperty("java.io.tmpdir"), WebServiceApTest.class.getSimpleName() + "-" + getName());
        destDir.mkdirs();
        options = new ArrayList<>();
        options.add("-d");
        options.add(destDir.getAbsolutePath());
        options.add("-s");
        options.add(destDir.getAbsolutePath());
    }

    public void testRemoteIfaceArg() {
        options.add("-proc:only");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        WsgenOptions wsgenOptions = new WsgenOptions();
        wsgenOptions.verbose = true;

        JavaCompiler.CompilationTask task = getCompilationTask("com.sun.tools.ws.processor.modeler.annotation.RemoteArgTestWS",
                options, wsgenOptions, diagnostics);
        try {
            task.call();
        } catch (RuntimeException re) {
            if (!(re.getCause() instanceof ModelerException)) {
                fail(ModelerException.class.getName() + " should have been thrown - spec requirement");
            }
        }
    }

    public void testRemoteIfaceReturn() {
        options.add("-proc:only");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        WsgenOptions wsgenOptions = new WsgenOptions();
        wsgenOptions.verbose = true;

        JavaCompiler.CompilationTask task = getCompilationTask("com.sun.tools.ws.processor.modeler.annotation.RemoteReturnTestWS",
                options, wsgenOptions, diagnostics);
        try {
            task.call();
        } catch (RuntimeException re) {
            if (!(re.getCause() instanceof ModelerException)) {
                fail(ModelerException.class.getName() + " should have been thrown - spec requirement");
            }
        }
    }

    public void testRemoteException() {
        options.add("-r");
        options.add(destDir.getAbsolutePath());
        options.add("-wsdl");
        options.add("-inlineSchemas");
        options.add("-verbose");
        options.add("com.sun.tools.ws.processor.modeler.annotation.RMITestWs");

        WsgenTool wsgen = new WsgenTool(System.out);
        wsgen.run(options.toArray(new String[options.size()]));

        WsgenOptions opts = getOptions(wsgen);

        int count = 0;
        for (File f : opts.getGeneratedFiles()) {
            count++;
        }
        Assert.assertEquals(11, count);

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(destDir, "RMITestWsService.wsdl"));
            NodeList faults = doc.getElementsByTagName("fault");
            Assert.assertEquals("CustomEx", faults.item(1).getAttributes().getNamedItem("name").getTextContent());
            Assert.assertEquals(faults.item(0).getAttributes().getNamedItem("name").getTextContent(),
                    faults.item(1).getAttributes().getNamedItem("name").getTextContent());
            Assert.assertEquals(2, faults.getLength());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
           throw new RuntimeException(ex);
        }

    }

    /**
     * Test if -Averbose=true is propagated from javac to processor.
     */
    public void testVerboseArg() {
        options.add("-Averbose=true");

        final WebServiceAp ap = new WebServiceAp();
        final boolean result = runCompiler(options, ap);

        Assert.assertTrue(ap.getOptions().verbose);
        Assert.assertTrue(result);
    }

    /**
     * Tests -s is used by annotation processor code generation.
     */
    public void testOutputDirectory() {
        final WebServiceAp ap = new WebServiceAp();
        final boolean result = runCompiler(options, ap);

        Assert.assertTrue(result);
        File generatedSourceDirFiles = new File(destDir, "com/sun/tools/ws/processor/modeler/annotation/jaxws");
        final File[] contents = generatedSourceDirFiles.listFiles();
        Assert.assertTrue(4 == contents.length);

        final List<String> expectedSourceNames = Arrays.asList("SayHello.java", "SayHello.class", "SayHelloResponse.java", "SayHelloResponse.class");
        for (File generated : contents) {
            Assert.assertTrue(expectedSourceNames.contains(generated.getName()));
        }
    }

    private JavaCompiler.CompilationTask getCompilationTask(String compilationUnit, List<String> javacOptions,
            WsgenOptions wsgenOptions, DiagnosticCollector<JavaFileObject> diagnostics) {
        Iterable<? extends JavaFileObject> compilationUnits = new HashSet<JavaFileObject>() {
            {
                add(new JavaSourceFromString(compilationUnit));
            }
        };
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                null,
                diagnostics,
                javacOptions,
                null,
                compilationUnits);
        WebServiceAp wsap = new WebServiceAp(wsgenOptions, System.out);
        task.setProcessors(Collections.singleton(wsap));
        return task;
    }

    private boolean runCompiler(List<String> args, WebServiceAp ap) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Iterable<? extends JavaFileObject> compilationUnits = new HashSet<JavaFileObject>() {
            {
                add(new JavaSourceFromString("com.sun.tools.ws.processor.modeler.annotation.EndpointJavaSource"));
            }
        };
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                null,
                diagnostics,
                args,
                null,
                compilationUnits);

        task.setProcessors(Collections.singleton(ap));

        return task.call();
    }

    private static WsgenOptions getOptions(WsgenTool tool) {
        Field field = null;
        try {
            field = tool.getClass().getDeclaredField("options");
            field.setAccessible(true);
            return (WsgenOptions) field.get(tool);
        } catch (ReflectiveOperationException | SecurityException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (field != null) {
                field.setAccessible(false);
            }
        }
    }

    private static class JavaSourceFromString extends SimpleJavaFileObject {

        private final String resource;

        JavaSourceFromString(String name) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
                    Kind.SOURCE);
            resource = '/' + name.replace('.', '/') + ".java";

        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            try (InputStream s = JavaSourceFromString.class.getResourceAsStream(resource)) {
                return new Scanner(s).useDelimiter("\\A").next();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
