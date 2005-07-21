/**
 * $Id: CompileTool.java,v 1.7 2005-07-21 01:52:35 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wscompile;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.tools.ws.processor.*;
import com.sun.tools.ws.processor.config.ClassModelInfo;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.config.parser.Reader;
import com.sun.tools.ws.processor.generator.CustomExceptionGenerator;
import com.sun.tools.ws.processor.generator.SeiGenerator;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext;
import com.sun.tools.ws.processor.modeler.annotation.WebServiceAP;
import com.sun.tools.ws.processor.util.ClientProcessorEnvironment;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.processor.util.ProcessorEnvironmentBase;
import com.sun.tools.ws.util.JAXRPCUtils;
import com.sun.tools.ws.util.JavaCompilerHelper;
import com.sun.tools.ws.util.ToolBase;
import com.sun.xml.ws.util.Version;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Vivek Pandey
 * 
 */
public class CompileTool extends ToolBase implements ProcessorNotificationListener,
        AnnotationProcessorFactory {

    public CompileTool(OutputStream out, String program) {
        super(out, program);
        listener = this;
    }

    protected boolean parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("")) {
                args[i] = null;
            } else if (args[i].equals("-g")) {
                compilerDebug = true;
                args[i] = null;
            } /*else if (args[i].equals("-O")) {
                compilerOptimize = true;
                args[i] = null;
            }*/ else if (args[i].equals("-verbose")) {
                verbose = true;
                args[i] = null;
            }else if (args[i].equals("-b")) {
                if(program.equals("wsgen"))
                    continue;
                if ((i + 1) < args.length) {
                    args[i] = null;
                    String file = args[++i];
                    args[i] = null;
                    bindingFiles.add(JAXRPCUtils.absolutize(JAXRPCUtils.getFileOrURLName(file)));
                }
            } else if (args[i].equals("-version")) {
                report(getVersion());
                doNothing = true;
                args[i] = null;
                return true;
            } else if (args[i].equals("-keep")) {
                keepGenerated = true;
                args[i] = null;
            } else if (args[i].equals("-d")) {
                if ((i + 1) < args.length) {
                    if (destDir != null) {
                        onError(getMessage("wscompile.duplicateOption", "-d"));
                        usage();
                        return false;
                    }
                    args[i] = null;
                    destDir = new File(args[++i]);
                    args[i] = null;
                    if (!destDir.exists()) {
                        onError(getMessage("wscompile.noSuchDirectory", destDir.getPath()));
                        usage();
                        return false;
                    }
                } else {
                    onError(getMessage("wscompile.missingOptionArgument", "-d"));
                    usage();
                    return false;
                }
            } else if (args[i].equals("-nd")) {
                if (program.equals("wsimport"))
                    continue;
                if ((i + 1) < args.length) {
                    if (nonclassDestDir != null) {
                        onError(getMessage("wscompile.duplicateOption", "-nd"));
                        usage();
                        return false;
                    }
                    args[i] = null;
                    nonclassDestDir = new File(args[++i]);
                    args[i] = null;
                    if (!nonclassDestDir.exists()) {
                        onError(getMessage("wscompile.noSuchDirectory", nonclassDestDir.getPath()));
                        usage();
                        return false;
                    }
                } else {
                    onError(getMessage("wscompile.missingOptionArgument", "-nd"));
                    usage();
                    return false;
                }
            } else if (args[i].equals("-s")) {
                if ((i + 1) < args.length) {
                    if (sourceDir != null) {
                        onError(getMessage("wscompile.duplicateOption", "-s"));
                        usage();
                        return false;
                    }
                    args[i] = null;
                    sourceDir = new File(args[++i]);
                    args[i] = null;
                    if (!sourceDir.exists()) {
                        onError(getMessage("wscompile.noSuchDirectory", sourceDir.getPath()));
                        usage();
                        return false;
                    }
                } else {
                    onError(getMessage("wscompile.missingOptionArgument", "-s"));
                    usage();
                    return false;
                }
            } else if (args[i].equals("-classpath") || args[i].equals("-cp")) {
                if (program.equals("wsimport"))
                    continue;
                if ((i + 1) < args.length) {
                    if (userClasspath != null) {
                        onError(getMessage("wscompile.duplicateOption", args[i]));
                        usage();
                        return false;
                    }
                    args[i] = null;
                    userClasspath = args[++i];
                    args[i] = null;
                }
            } else if (args[i].startsWith("-httpproxy:")) {
                if(program.equals("wsgen"))
                    continue;
                String value = args[i].substring(11);
                if (value.length() == 0) {
                    onError(getMessage("wscompile.invalidOption", args[i]));
                    usage();
                    return false;
                }
                int index = value.indexOf(':');
                if (index == -1) {
                    System.setProperty("proxySet", TRUE);
                    System.setProperty("proxyHost", value);
                    System.setProperty("proxyPort", "8080");
                } else {
                    System.setProperty("proxySet", TRUE);
                    System.setProperty("proxyHost", value.substring(0, index));
                    System.setProperty("proxyPort", value.substring(index + 1));
                }
                args[i] = null;
            } else if (args[i].startsWith("-wsdl")) {
                if (program.equals("wsimport")) 
                    continue;
                genWsdl = true;
                String value = args[i].substring(5);
                int index = value.indexOf(':');
                if (index == 0) {
                    value = value.substring(1);
                    index = value.indexOf('/');
                    if (index == -1) {
                        protocol = value;
                        transport = HTTP;
                    } else {
                        protocol = value.substring(0, index);
                        transport = value.substring(index + 1);
                    }
                    if (!isValidProtocol(protocol)) {
                        onError(getMessage("wsgen.invalid.protocol", protocol, VALID_PROTOCOLS));
                    }
                    if (!isValidTransport(transport)) {
                        onError(getMessage("wsgen.invalid.transport", transport, VALID_TRANSPORTS));
                    }               
                }
                args[i] = null;
            } else if (args[i].startsWith("-help")) {
                help();
                return false;
            }
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                if (args[i].startsWith("-")) {
                    onError(getMessage("wscompile.invalidOption", args[i]));
                    usage();
                    return false;
                }

                // the input source could be a local file or a URL,get the
                // abolutized URL string
                String fileName = args[i];
                if (program.equals("wsgen")) {
                    if (!isClass(fileName)) {
                        onError(getMessage("wsgen.class.not.found", fileName));
                        return false;
                    }
                } else {
                    fileName = JAXRPCUtils.absolutize(JAXRPCUtils.getFileOrURLName(args[i]));
                }
                inputFiles.add(fileName);
            }
        }

        if (inputFiles.isEmpty()) {
            onError(getMessage(program+".missingFile"));
            usage();
            return false;
        }
        // put jaxrpc and jaxb binding files
        properties.put(ProcessorOptions.BINDING_FILES, bindingFiles);
        return true;
    }

    static public boolean isValidProtocol(String protocol) {
        return (protocol.equals(SOAP11) ||   
                protocol.equals(SOAP12));
    }
    
    static public boolean isValidTransport(String transport) {
        return (transport.equals(HTTP));
    }
    
    public Localizable getVersion() {
        return getMessage("wscompile.version", Version.PRODUCT_NAME, Version.VERSION_NUMBER,
                Version.BUILD_NUMBER);
    }

    protected void run() throws Exception {
        if (doNothing) {
            return;
        }
        try {
            beforeHook();
            environment = createEnvironment();
            configuration = (Configuration) createConfiguration();
            if (configuration.getModelInfo() instanceof ClassModelInfo) {
                buildModel(((ClassModelInfo) configuration.getModelInfo()).getClassName());
            } else {
                processor = new Processor(configuration, properties);
                setEnvironmentValues(environment);
                processor.runModeler();
                withModelHook();
                registerProcessorActions(processor);
                processor.runActions();
                if (environment.getErrorCount() == 0) {
                    compileGeneratedClasses();
                }
            }
            afterHook();
        } finally {
            if (!keepGenerated) {
                removeGeneratedFiles();
            }
            if (environment != null) {
                environment.shutdown();
            }
        }
    }

    protected void setEnvironmentValues(ProcessorEnvironment env) {
        int envFlags = env.getFlags();
        envFlags |= ProcessorEnvironment.F_WARNINGS;
        if (verbose) {
            envFlags |= ProcessorEnvironment.F_VERBOSE;
        }
        env.setFlags(envFlags);
    }

    protected void initialize() {
        super.initialize();
        properties = new Properties();
        actions = new HashMap();
        actions.put(ActionConstants.ACTION_SERVICE_INTERFACE_GENERATOR,
                new com.sun.tools.ws.processor.generator.ServiceInterfaceGenerator());
        actions.put(ActionConstants.ACTION_REMOTE_INTERFACE_GENERATOR,
                new SeiGenerator());
        actions.put(ActionConstants.ACTION_CUSTOM_EXCEPTION_GENERATOR,
                new CustomExceptionGenerator());
        actions.put(ActionConstants.ACTION_JAXB_TYPE_GENERATOR,
                new com.sun.tools.ws.processor.generator.JAXBTypeGenerator());
//        actions.put(ActionConstants.ACTION_WSDL_GENERATOR, new com.sun.tools.ws.processor.generator.WSDLGenerator());
    }

    public void removeGeneratedFiles() {
        environment.deleteGeneratedFiles();
    }

    public void buildModel(String endpoint) {
        context = new AnnotationProcessorContext();
        webServiceAP = new WebServiceAP(this, environment, properties, context);

        String classpath = environment.getClassPath();

        String key = ProcessorOptions.DESTINATION_DIRECTORY_PROPERTY;
        String[] args = new String[8];
        args[0] = "-d";
        args[1] = destDir.getAbsolutePath();
        args[2] = "-classpath";
        args[3] = classpath; 
        args[4] = "-s";
        args[5] = sourceDir.getAbsolutePath();
        args[6] = "-XclassesAsDecls";
        args[7] = endpoint;

        int result = com.sun.tools.apt.Main.process(this, args);
        if (result != 0) {
            environment.error(getMessage("wscompile.compilationFailed"));
            return;
        }
        if (genWsdl) {
            String tmpPath = destDir.getAbsolutePath()+File.pathSeparator+classpath;
            ClassLoader classLoader = new URLClassLoader(ProcessorEnvironmentBase.pathToURLs(tmpPath));
            Class endpointClass = null;

            try {
                endpointClass = classLoader.loadClass(endpoint);
            } catch (ClassNotFoundException e) {
                // this should never happen
                environment.error(getMessage("wsgen.class.not.found", endpoint));
            }
            String bindingID = getBindingID(protocol);
            com.sun.xml.ws.modeler.RuntimeModeler rtModeler = 
                    new com.sun.xml.ws.modeler.RuntimeModeler(endpointClass, bindingID);
            rtModeler.setClassLoader(classLoader);
            com.sun.xml.ws.model.RuntimeModel rtModel = rtModeler.buildRuntimeModel();
            WSDLGenerator wsdlGenerator = new WSDLGenerator(rtModel,
                    new com.sun.xml.ws.wsdl.writer.WSDLOutputResolver() {
                        public Result getWSDLOutput(String suggestedFilename) {
                            File wsdlFile =
                                new File(nonclassDestDir, suggestedFilename);
                            
                            Result result = new StreamResult();
                            try {
                                result = new StreamResult(new FileOutputStream(wsdlFile));
                                result.setSystemId(wsdlFile.toString().replace('\\', '/'));                            
                            } catch (FileNotFoundException e) {
                                environment.error(getMessage("wsgen.could.not.create.file", wsdlFile.toString()));                                
                            }
                            return result;
                        }
                        public Result getSchemaOutput(String namespace, String suggestedFilename) {
                            if (namespace.equals(""))
                                return null;
                            return getWSDLOutput(suggestedFilename);
                        }                        
                    }, bindingID);
            wsdlGenerator.doGeneration();        
        }
    }
    
    static public String getBindingID(String protocol) {
        if (protocol.equals(SOAP11))
            return SOAP11_ID;
        if (protocol.equals(SOAP12))
            return SOAP12_ID;
        return null;
    }

    public void runProcessorActions() {
        if (!(configuration.getModelInfo() instanceof ClassModelInfo)) {
            onError(getMessage("wscompile.classmodelinfo.expected", new Object[] { configuration
                    .getModelInfo() }));
            return;
        }
        Model model = context.getSEIContext(
                ((ClassModelInfo) configuration.getModelInfo()).getClassName()).getModel();
        processor = new Processor(configuration, properties, model);
        withModelHook();
        registerProcessorActions(processor);
        processor.runActions();
        if (environment.getErrorCount() != 0) {
            // TODO throw an error
            // compileGeneratedClasses();
        }

    }

    protected void withModelHook() {
    }

    protected void afterHook() {
    }

    protected void compileGeneratedClasses() {
        List sourceFiles = new ArrayList();

        for (Iterator iter = environment.getGeneratedFiles(); iter.hasNext();) {
            GeneratedFileInfo fileInfo = (GeneratedFileInfo) iter.next();
            File f = fileInfo.getFile();
            if (f.exists() && f.getName().endsWith(".java")) {
                sourceFiles.add(f.getAbsolutePath());
            }
        }

        if (sourceFiles.size() > 0) {
            String classDir = destDir.getAbsolutePath();
            String classpathString = createClasspathString();
            String[] args = new String[4 + (compilerDebug == true ? 1 : 0)
                    + (compilerOptimize == true ? 1 : 0) + sourceFiles.size()];
            args[0] = "-d";
            args[1] = classDir;
            args[2] = "-classpath";
            args[3] = classpathString;
            int baseIndex = 4;
            if (compilerDebug) {
                args[baseIndex++] = "-g";
            }
            if (compilerOptimize) {
                args[baseIndex++] = "-O";
            }
            for (int i = 0; i < sourceFiles.size(); ++i) {
                args[baseIndex + i] = (String) sourceFiles.get(i);
            }

            // ByteArrayOutputStream javacOutput = new ByteArrayOutputStream();
            JavaCompilerHelper compilerHelper = new JavaCompilerHelper(out);
            boolean result = compilerHelper.compile(args);
            if (!result) {
                environment.error(getMessage("wscompile.compilationFailed"));
            }
        }
    }

    protected ProcessorAction getAction(String name) {
        return (ProcessorAction) actions.get(name);
    }

    protected String createClasspathString() {
        if (userClasspath == null) {
            userClasspath = "";
        }
        return userClasspath + File.pathSeparator + System.getProperty("java.class.path");
    }

    protected void registerProcessorActions(Processor processor) {
        register(processor);
    }

    protected void register(Processor processor) {
        boolean genServiceInterface = false;
        boolean genInterface = false;
        //boolean genInterfaceTemplate = false;
        boolean genCustomClasses = false;

        if (configuration.getModelInfo() instanceof WSDLModelInfo) {
            genInterface = true;
            //genInterfaceTemplate = true;
            genServiceInterface = true;
            genCustomClasses = true;
        }

        if (genServiceInterface) {
            processor.add(getAction(ActionConstants.ACTION_SERVICE_INTERFACE_GENERATOR));
        }

        if (genCustomClasses) {
            processor.add(getAction(ActionConstants.ACTION_JAXB_TYPE_GENERATOR));
        }

        if (genInterface) {
            processor.add(getAction(ActionConstants.ACTION_CUSTOM_EXCEPTION_GENERATOR));
            processor.add(getAction(ActionConstants.ACTION_REMOTE_INTERFACE_GENERATOR));
        }


//        if (genWsdl) {
//            processor.add(getAction(ActionConstants.ACTION_WSDL_GENERATOR));
//        }
    }

    public String getVersionString() {
        return localizer.localize(getVersion());
    }

    protected Configuration createConfiguration() throws Exception {
        if (environment == null)
            environment = createEnvironment();
        Reader reader = new Reader(environment, properties);
        return reader.parse(inputFiles);
    }

    protected void beforeHook() {
        if (destDir == null) {
            destDir = new File(".");
        }
        if (sourceDir == null) {
            sourceDir = destDir;
        }
        if (nonclassDestDir == null) {
            nonclassDestDir = destDir;
        }

        properties.setProperty(ProcessorConstants.JAXWS_VERSION, getVersionString());
        properties.setProperty(ProcessorOptions.SOURCE_DIRECTORY_PROPERTY, sourceDir
                .getAbsolutePath());
        properties.setProperty(ProcessorOptions.DESTINATION_DIRECTORY_PROPERTY, destDir
                .getAbsolutePath());
        properties.setProperty(ProcessorOptions.NONCLASS_DESTINATION_DIRECTORY_PROPERTY,
                nonclassDestDir.getAbsolutePath());
        properties.setProperty(ProcessorOptions.EXTENSION, (extension ? "true" : "false"));
        properties.setProperty(ProcessorOptions.PRINT_STACK_TRACE_PROPERTY,
                (verbose ? TRUE : FALSE));
        properties.setProperty(ProcessorOptions.PROTOCOL, protocol);
        properties.setProperty(ProcessorOptions.TRANSPORT, transport);
    }

    protected String getGenericErrorMessage() {
        return "wscompile.error";
    }

    protected String getResourceBundleName() {
        return "com.sun.tools.ws.resources.wscompile";
    }

    public Collection<String> supportedOptions() {
        return supportedOptions;
    }

    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }

    public void onError(Localizable msg) {
        report(getMessage("wscompile.error", localizer.localize(msg)));
    }

    public void onWarning(Localizable msg) {
        report(getMessage("wscompile.warning", localizer.localize(msg)));
    }

    public void onInfo(Localizable msg) {
        report(getMessage("wscompile.info", localizer.localize(msg)));
    }

    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment apEnv) {
        if (verbose)
            apEnv.getMessager().printNotice("\tap round: " + ++round);
        webServiceAP.init(apEnv);
        return webServiceAP;
    }

    private boolean isClass(String className) {
        try {
            ProcessorEnvironment env = createEnvironment();
            env.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private ProcessorEnvironment createEnvironment() throws Exception {
        String cpath = userClasspath + File.pathSeparator + System.getProperty("java.class.path");
        ProcessorEnvironment env = new ClientProcessorEnvironment(System.out, cpath, listener);
        return env;
    }

    protected void usage() {
        help();
        //report(getMessage(program+".usage", program));
    }

    protected void help() {
        report(getMessage(program+".help", program));
        report(getMessage(program+".usage.examples"));
    }

    /*
     * Processor doesn't examine any options.
     */
    static final Collection<String> supportedOptions = Collections
            .unmodifiableSet(new HashSet<String>());

    /*
     * All annotation types are supported.
     */
    static Collection<String> supportedAnnotations;
    static {
        Collection<String> types = new HashSet<String>();
        types.add("*");
        types.add("javax.jws.*");
        types.add("javax.jws.soap.*");
        supportedAnnotations = Collections.unmodifiableCollection(types);
    }

    private AnnotationProcessorContext context;

    private WebServiceAP webServiceAP;

    private int round = 0;

    // End AnnotationProcessorFactory stuff
    // -----------------------------------------------------------------------------

    protected Properties properties;
    protected ProcessorEnvironment environment;
    protected Configuration configuration;
    protected ProcessorNotificationListener listener;
    protected Processor processor;
    protected Map actions;
    protected List<String> inputFiles = new ArrayList<String>();
    protected File sourceDir;
    protected File destDir;
    protected File nonclassDestDir;
    protected boolean doNothing = false;
    protected boolean compilerDebug = false;
    protected boolean compilerOptimize = false;
    protected boolean verbose = false;
    protected boolean keepGenerated = false;
    protected boolean extension = false;
    protected String userClasspath = null;
    protected Set<String> bindingFiles = new HashSet<String>();
    protected boolean genWsdl = false;
    protected String protocol = SOAP11;
    protected String transport = HTTP;
    protected static final String SOAP11 = "soap11";
    protected static final String SOAP12 = "soap12";
    protected static final String HTTP   = "http";    
    protected static final String SOAP11_ID = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;
    protected static final String SOAP12_ID = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;
    protected static final String VALID_PROTOCOLS = "soap11, soap12";
    protected static final String VALID_TRANSPORTS = "http";
}
