/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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


package com.sun.tools.ws.wscompile;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.tools.ws.ToolVersion;
import com.sun.tools.ws.api.WsgenExtension;
import com.sun.tools.ws.api.WsgenProtocol;
import com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext;
import com.sun.tools.ws.processor.modeler.annotation.WebServiceAP;
import com.sun.tools.ws.processor.modeler.wsdl.ConsoleErrorReporter;
import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.xjc.util.NullStream;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.txw2.output.StreamSerializer;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.binding.SOAPBindingImpl;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.RuntimeModeler;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;
import com.sun.xml.ws.wsdl.writer.WSDLResolver;
import com.sun.istack.tools.ParallelWorldClassLoader;
import org.xml.sax.SAXParseException;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Holder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Vivek Pandey
 */
public class WsgenTool implements AnnotationProcessorFactory {
    private final PrintStream out;
    private final WsgenOptions options = new WsgenOptions();


    public WsgenTool(OutputStream out, Container container) {
        this.out = (out instanceof PrintStream)?(PrintStream)out:new PrintStream(out);
        this.container = container;
    }


    public WsgenTool(OutputStream out) {
        this(out, null);
    }

    public boolean run(String[] args){
        final Listener listener = new Listener();
        for (String arg : args) {
            if (arg.equals("-version")) {
                listener.message(ToolVersion.VERSION.BUILD_VERSION);
                return true;
            }
        }
        try {
            options.parseArguments(args);
            options.validate();
            if(!buildModel(options.endpoint.getName(), listener)){
                return false;
            }
        }catch (Options.WeAreDone done){
            usage((WsgenOptions)done.getOptions());
        }catch (BadCommandLineException e) {
            if(e.getMessage()!=null) {
                System.out.println(e.getMessage());
                System.out.println();
            }
            usage((WsgenOptions)e.getOptions());
            return false;
        }catch(AbortException e){
            //error might have been reported
        }finally{
            if(!options.keep){
                options.removeGeneratedFiles();
            }
        }
        return true;
    }

    private AnnotationProcessorContext context;
    private final Container container;

    private WebServiceAP webServiceAP;

    private int round = 0;

    // Workaround for bug 6499165 on jax-ws,
    // Original bug with JDK 6500594 , 6500594 when compiled with debug option,
    private void workAroundJavacDebug() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            final Class aptMain = cl.loadClass("com.sun.tools.apt.main.Main");
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    try {
                        Field forcedOpts = aptMain.getDeclaredField("forcedOpts");
                        forcedOpts.setAccessible(true);
                        forcedOpts.set(null, new String[]{});
                    } catch (NoSuchFieldException e) {
                        if(options.verbose)
                            e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        if(options.verbose)
                            e.printStackTrace();
                    }
                    return null;
                }
            });
        } catch (ClassNotFoundException e) {
            if(options.verbose)
                e.printStackTrace();
        }
    }

    /*
     * To take care of JDK6-JDK6u3, where 2.1 API classes are not there
     */
    private static boolean useBootClasspath(Class clazz) {
        try {
            ParallelWorldClassLoader.toJarUrl(clazz.getResource('/'+clazz.getName().replace('.','/')+".class"));
            return true;
        } catch(Exception e) {
            return false;
        }
    }


    public boolean buildModel(String endpoint, Listener listener) throws BadCommandLineException {
        final ErrorReceiverFilter errReceiver = new ErrorReceiverFilter(listener);
        context = new AnnotationProcessorContext();
        webServiceAP = new WebServiceAP(options, context, errReceiver, out);

        boolean bootCP = useBootClasspath(EndpointReference.class) || useBootClasspath(XmlSeeAlso.class);

        String[] args = new String[8 + (bootCP ? 1 :0) + (options.nocompile?1:0)];
        int i = 0;
        args[i++] = "-d";
        args[i++] = options.destDir.getAbsolutePath();
        args[i++] = "-classpath";
        args[i++] = options.classpath;
        args[i++] = "-s";
        args[i++] = options.sourceDir.getAbsolutePath();
        args[i++] = "-XclassesAsDecls";
        if(options.nocompile) {
            args[i++] = "-nocompile";
        }
        args[i++] = endpoint;
        if (bootCP) {
            args[i++] = "-Xbootclasspath/p:"+JavaCompilerHelper.getJarFile(EndpointReference.class)+File.pathSeparator+JavaCompilerHelper.getJarFile(XmlSeeAlso.class);
        }

        // Workaround for bug 6499165: issue with javac debug option
        workAroundJavacDebug();
        int result = com.sun.tools.apt.Main.process(this, args);
        if (result != 0) {
            out.println(WscompileMessages.WSCOMPILE_ERROR(WscompileMessages.WSCOMPILE_COMPILATION_FAILED()));
            return false;
        }
        if (options.genWsdl) {
            String tmpPath = options.destDir.getAbsolutePath()+ File.pathSeparator+options.classpath;
            ClassLoader classLoader = new URLClassLoader(Options.pathToURLs(tmpPath),
                    this.getClass().getClassLoader());
            Class<?> endpointClass;
            try {
                endpointClass = classLoader.loadClass(endpoint);
            } catch (ClassNotFoundException e) {
                throw new BadCommandLineException(WscompileMessages.WSGEN_CLASS_NOT_FOUND(endpoint));
            }

            BindingID bindingID = options.getBindingID(options.protocol);
            if (!options.protocolSet) {
                bindingID = BindingID.parse(endpointClass);
            }
            WebServiceFeatureList wsfeatures = new WebServiceFeatureList(endpointClass);
            RuntimeModeler rtModeler = new RuntimeModeler(endpointClass, options.serviceName, bindingID, wsfeatures.toArray());
            rtModeler.setClassLoader(classLoader);
            if (options.portName != null)
                rtModeler.setPortName(options.portName);
            AbstractSEIModelImpl rtModel = rtModeler.buildRuntimeModel();

            final File[] wsdlFileName = new File[1]; // used to capture the generated WSDL file.
            final Map<String,File> schemaFiles = new HashMap<String,File>();

            WSDLGenerator wsdlGenerator = new WSDLGenerator(rtModel,
                    new WSDLResolver() {
                        private File toFile(String suggestedFilename) {
                            return new File(options.nonclassDestDir, suggestedFilename);
                        }
                        private Result toResult(File file) {
                            Result result;
                            try {
                                result = new StreamResult(new FileOutputStream(file));
                                result.setSystemId(file.getPath().replace('\\', '/'));
                            } catch (FileNotFoundException e) {
                                errReceiver.error(e);
                                return null;
                            }
                            return result;
                        }

                        public Result getWSDL(String suggestedFilename) {
                            File f = toFile(suggestedFilename);
                            wsdlFileName[0] = f;
                            return toResult(f);
                        }
                        public Result getSchemaOutput(String namespace, String suggestedFilename) {
                            if (namespace.equals(""))
                                return null;
                            File f = toFile(suggestedFilename);
                            schemaFiles.put(namespace,f);
                            return toResult(f);
                        }
                        public Result getAbstractWSDL(Holder<String> filename) {
                            return toResult(toFile(filename.value));
                        }
                        public Result getSchemaOutput(String namespace, Holder<String> filename) {
                            return getSchemaOutput(namespace, filename.value);
                        }
                        // TODO pass correct impl's class name
                    }, bindingID.createBinding(wsfeatures.toArray()), container,
                    endpointClass, options.inlineSchemas, ServiceFinder.find(WSDLGeneratorExtension.class).toArray());
            wsdlGenerator.doGeneration();

            if(options.wsgenReport!=null)
                generateWsgenReport(endpointClass,rtModel,wsdlFileName[0],schemaFiles);
        }
        return true;
    }

    /**
     * Generates a small XML file that captures the key activity of wsgen,
     * so that test harness can pick up artifacts.
     */
    private void generateWsgenReport(Class<?> endpointClass, AbstractSEIModelImpl rtModel, File wsdlFile, Map<String,File> schemaFiles) {
        try {
            ReportOutput.Report report = TXW.create(ReportOutput.Report.class,
                new StreamSerializer(new BufferedOutputStream(new FileOutputStream(options.wsgenReport))));

            report.wsdl(wsdlFile.getAbsolutePath());
            ReportOutput.writeQName(rtModel.getServiceQName(), report.service());
            ReportOutput.writeQName(rtModel.getPortName(), report.port());
            ReportOutput.writeQName(rtModel.getPortTypeName(), report.portType());

            report.implClass(endpointClass.getName());

            for (Map.Entry<String,File> e : schemaFiles.entrySet()) {
                ReportOutput.Schema s = report.schema();
                s.ns(e.getKey());
                s.location(e.getValue().getAbsolutePath());
            }

            report.commit();
        } catch (IOException e) {
            // this is code for the test, so we can be lousy in the error handling
            throw new Error(e);
        }
    }

    /**
     * "Namespace" for code needed to generate the report file.
     */
    static class ReportOutput {
        @XmlElement("report")
        interface Report extends TypedXmlWriter {
            @XmlElement
            void wsdl(String file); // location of WSDL
            @XmlElement
            QualifiedName portType();
            @XmlElement
            QualifiedName service();
            @XmlElement
            QualifiedName port();

            /**
             * Name of the class that has {@link javax.jws.WebService}.
             */
            @XmlElement
            void implClass(String name);

            @XmlElement
            Schema schema();
        }

        interface QualifiedName extends TypedXmlWriter {
            @XmlAttribute
            void uri(String ns);
            @XmlAttribute
            void localName(String localName);
        }

        interface Schema extends TypedXmlWriter {
            @XmlAttribute
            void ns(String ns);
            @XmlAttribute
            void location(String filePath);
        }

        private static void writeQName( QName n, QualifiedName w ) {
            w.uri(n.getNamespaceURI());
            w.localName(n.getLocalPart());
        }
    }

    protected void usage(WsgenOptions options) {
        // Just don't see any point in passing WsgenOptions
        // BadCommandLineException also shouldn't have options
        if (options == null)
            options = this.options;
        System.out.println(WscompileMessages.WSGEN_HELP("WSGEN", options.protocols, options.nonstdProtocols.keySet()));
        System.out.println(WscompileMessages.WSGEN_USAGE_EXAMPLES());
    }

    public Collection<String> supportedOptions() {
        return supportedOptions;
    }

    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> set, AnnotationProcessorEnvironment apEnv) {
        if (options.verbose)
            apEnv.getMessager().printNotice("\tap round: " + ++round);
        webServiceAP.init(apEnv);
        return webServiceAP;
    }

    class Listener extends WsimportListener {
        ConsoleErrorReporter cer = new ConsoleErrorReporter(out == null ? new PrintStream(new NullStream()) : out);

        @Override
        public void generatedFile(String fileName) {
            message(fileName);
        }

        @Override
        public void message(String msg) {
            out.println(msg);
        }

        @Override
        public void error(SAXParseException exception) {
            cer.error(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) {
            cer.fatalError(exception);
        }

        @Override
        public void warning(SAXParseException exception) {
            cer.warning(exception);
        }

        @Override
        public void info(SAXParseException exception) {
            cer.info(exception);
        }
    }

    /*
     * Processor doesn't examine any options.
     */
    static final Collection<String> supportedOptions = Collections
            .unmodifiableSet(new HashSet<String>());

    /*
     * All annotation types are supported.
     */
    static final Collection<String> supportedAnnotations;
    static {
        Collection<String> types = new HashSet<String>();
        types.add("*");
        types.add("javax.jws.*");
        types.add("javax.jws.soap.*");
        supportedAnnotations = Collections.unmodifiableCollection(types);
    }
}
