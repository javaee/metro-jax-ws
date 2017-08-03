/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package testutil.benchmark;

//import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
//import org.kohsuke.args4j.Argument;
//import org.kohsuke.args4j.CmdLineException;
//import org.kohsuke.args4j.CmdLineParser;
//import org.kohsuke.args4j.Option;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Benchmark test driver.
 *
 * <p>
 * For historical reasons, many of the properties can be configured
 * both from system properties and command line options.
 *
 * @author JAX-RPC RI Development Team
 */
public class BenchmarkTests {

//    @Argument
//    private List<String> tests = new ArrayList<String>();
//
//    @Option(name="-l",usage="test the local transport")
//    private boolean uselocal;
//
//    private final PrintStream out = System.out;
//
//    private XMLStreamWriter writer = null;
//    private String version = "2.0";
//
//    @Option(name="-p",usage="package name of the benchmark. either 'doclit' or 'rpclit'")
//    private String testPackage = System.getProperty("package.prefix");
//
//    @Option(name="-i",usage="number of iteration for the test")
//    private int iterations = getIntProperty("iterations", 100);
//
//    @Option(name="-w",usage="number of iteration for the warm up")
//    private int warmup = getIntProperty("warmup",100);
//
//    private String state = System.getProperty("state", "first");
//
//    private final String classPrefix = "Echo";
//    private final String classSuffix = "Benchmark";
//    private final String xmlLog = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "jaxrpc-benchmark.xml";
//    private final String htmlLog = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "jaxrpc-benchmark.html";
//
//    public static void main(String[] args) throws Exception {
//        System.exit(new BenchmarkTests().run(args));
//    }
//
    public BenchmarkTests() {
//        String temp = System.getProperty("uselocal");
//        uselocal = temp != null && temp.equals("true");
    }
//
//    public int run(String[] args) throws Exception {
//        CmdLineParser p = new CmdLineParser(this);
//        try {
//            p.parseArgument(args);
//        } catch (CmdLineException e) {
//            System.err.println(e);
//            p.printUsage(System.err);
//            return -1;
//        }
//
//        if(testPackage==null) {
//            System.err.println("-p option is mandatory");
//            return -1;
//        }
//
//        // ugly workaround. test code refers to this system property
//        // everywhere. If you are serious about the transport independence,
//        // you should be able to set a custom transport without relying
//        // on a system property! But unless that is fixed, we need this.
//        System.setProperty("uselocal",Boolean.toString(uselocal));
//
//        boolean first = state.equalsIgnoreCase("first");
//        FileOutputStream fos = new FileOutputStream(xmlLog, !first);
//        writer = XMLStreamWriterFactory.create(fos, "UTF-8");
//        if (first) {
//            writer.writeStartElement("benchmark");
//            writeEnvironment(writer);
//        }
//        writer.writeStartElement(testPackage);
//        writer.writeAttribute("jaxrpc-version", version);
//        writer.writeAttribute("jdk-version", System.getProperty("java.vm.version"));
//
//        // run the tests
//        if(tests.isEmpty()) {
//            System.err.println("No test to run");
//            return -1;
//        }
//
//        for( String test : tests )
//            invoke(test);
//
//        writer.writeEndElement();
//        writer.close();
//        fos.close();
//        boolean last = state.equalsIgnoreCase("last");
//        if (last) {
//            fos = new FileOutputStream(xmlLog, true);
//            fos.write("</benchmark>".getBytes());
//            fos.flush();
//            fos.close();
//            out.println("******* XML log generated in \"" + xmlLog + "\"");
//            convertToHTML();
//        }
//
//        return 0;
//    }
//
//    private void invoke(String classType) throws Exception {
//	if (classType.equals("\"\"")) {
//		classType = "";
//	}
//        String className = "benchmark." + testPackage + ".client." + classPrefix + classType + classSuffix;
//        Class classObject = Class.forName(className);
//        Constructor ctor = classObject.getConstructor(String.class);
//        Benchmark benchmark = (Benchmark)ctor.newInstance(className);
//
//        double returnValue = run(benchmark,out);
//
//        out.println(className + " : " + returnValue + " ns");
//        writer.writeStartElement(classType.equals("") ? "All" : classType);
//        writer.writeCharacters(String.valueOf(returnValue));
//        writer.writeEndElement();
//        cleanup();
//        out.println();
//    }
//
//    /**
//     * Run a {@link Benchmark} and returns the time it took to run.
//     */
//    public long run(Benchmark benchmark, PrintStream out) throws Exception {
//        System.out.println("Iterations: " + iterations);
//        System.out.println("Warmup: " + warmup);
//        for (int i = 0; i < warmup; ++i) {
//            benchmark.testOnce();
//        }
//
//        cleanup();
//
//        long startTime = System.nanoTime();
//        for (int i = 0; i < iterations; ++i) {
//            benchmark.testOnce();
//        }
//        long endTime = System.nanoTime();
//
//        long totalTime = endTime - startTime;
//        return totalTime / iterations;
//    }
//
//    private void cleanup() throws InterruptedException {
//        Runtime.getRuntime().gc();
//        Thread.sleep(100);
//        Runtime.getRuntime().gc();
//    }
//
//    private void convertToHTML()
//            throws TransformerException,
//                   IOException {
//        StreamSource xslSource = new StreamSource(new File(System.getProperty("benchmark.stylesheet")));
//        Transformer trans = TransformerFactory.newInstance().newTransformer(xslSource);
//        Properties props = new Properties();
//        props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
//        trans.setOutputProperties(props);
//        trans.transform(new StreamSource(new File(xmlLog)), new StreamResult(new FileOutputStream(htmlLog)));
//        out.println("******* Benchmark results are available in \"" + htmlLog + "\"");
//    }
//
//    private void writeEnvironment(XMLStreamWriter writer) throws Exception {
//        writer.writeStartElement("environment");
//
//        writer.writeStartElement("date");
//        writer.writeCharacters(new Date().toString());
//        writer.writeEndElement();
//
//        writer.writeStartElement("os");
//        writer.writeCharacters(System.getProperty("os.name") + " " +
//            System.getProperty("os.arch") + " " +System.getProperty("os.version"));
//        writer.writeEndElement();
//
//        writer.writeStartElement("memory");
//        writer.writeCharacters(""+Runtime.getRuntime().maxMemory());
//        writer.writeEndElement();
//
//        writer.writeStartElement("warmup");
//        writer.writeCharacters(System.getProperty("warmup"));
//        writer.writeEndElement();
//
//        writer.writeStartElement("iterations");
//        writer.writeCharacters(System.getProperty("iterations"));
//        writer.writeEndElement();
//
//        writer.writeStartElement("local-http");
//        writer.writeCharacters(uselocal ? "Local" : "HTTP");
//        writer.writeEndElement();
//
//        writer.writeEndElement();
//    }
//
//    /**
//     * Gets the int value from system property.
//     */
//    private static int getIntProperty(String key, int defaultValue) {
//        if (System.getProperty(key) == null) {
//            return defaultValue;
//        } else
//            return new Integer(System.getProperty(key));
//    }

}
