/**
 * $Id: WebServiceAP.java,v 1.9 2005-08-24 15:19:55 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import com.sun.tools.ws.processor.ProcessorNotificationListener;
import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.generator.GeneratorUtil;
import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.ModelProperties;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.jaxb.JAXBModel;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext.SEIContext;
import com.sun.tools.ws.processor.util.ClientProcessorEnvironment;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.tools.ws.util.ToolBase;
import com.sun.tools.xjc.api.JavaCompiler;
import com.sun.tools.xjc.api.Reference;
import com.sun.tools.xjc.api.XJC;
import com.sun.xml.ws.util.Version;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessage;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;



/**
 * WebServiceAP is a APT AnnotationProcessor for processing javax.jws.* and 
 * javax.xml.ws.* annotations. This class is used either by the WsGen (CompileTool) tool or 
 *    idirectly via the {@link com.sun.istack.ws.WSAP WSAP} when invoked by APT.
 *
 * @author WS Development Team
 */
public class WebServiceAP extends ToolBase implements AnnotationProcessor, ModelBuilder, WebServiceConstants,
    ProcessorNotificationListener {

    protected AnnotationProcessorEnvironment apEnv;
    protected ProcessorEnvironment env;

//    private String wsdlUri;
    private File sourceDir;

    // the model being build
    private Model model;

    private TypeDeclaration remoteDecl;
    private TypeDeclaration remoteExceptionDecl;
    private TypeDeclaration exceptionDecl;
    private TypeDeclaration defHolderDecl;
    private Service service;
    private Port port;
    protected AnnotationProcessorContext context;
    private Set<TypeDeclaration> processedTypeDecls = new HashSet<TypeDeclaration>();
    protected Messager messager;
    private ByteArrayOutputStream output;
    private ToolBase tool;
    private boolean donotOverride = false;
    private boolean wrapperGenerated = false;


    public void run() {
    }

    protected  boolean parseArguments(String[] args) {
       return true;
    }

    public WebServiceAP(ToolBase tool, ProcessorEnvironment env, Properties options,  AnnotationProcessorContext context) {
        super(System.out, "WebServiceAP");
        this.context = context;
        this.tool = tool;
        this.env = env;
        if (options != null) {
            sourceDir = new File(options.getProperty(ProcessorOptions.SOURCE_DIRECTORY_PROPERTY));
            String key = ProcessorOptions.DONOT_OVERRIDE_CLASSES;
            this.donotOverride =
                Boolean.valueOf(options.getProperty(key)).booleanValue();
        }
    }

    public void init(AnnotationProcessorEnvironment apEnv) {
        this.apEnv = apEnv;
        remoteDecl = this.apEnv.getTypeDeclaration(REMOTE_CLASSNAME);
        remoteExceptionDecl = this.apEnv.getTypeDeclaration(REMOTE_EXCEPTION_CLASSNAME);
        exceptionDecl = this.apEnv.getTypeDeclaration(EXCEPTION_CLASSNAME);
        defHolderDecl = this.apEnv.getTypeDeclaration(HOLDER_CLASSNAME);

        if (env == null) {
            Map<String, String> apOptions = apEnv.getOptions();
            output = new ByteArrayOutputStream();
            String classDir = apOptions.get("-d");
            if (apOptions.get("-s") != null)
                sourceDir = new File(apOptions.get("-s"));
            String cp = apOptions.get("-classpath");
            String cpath = classDir +
                    File.pathSeparator +
                    cp + File.pathSeparator +
                    System.getProperty("java.class.path");
            env = new ClientProcessorEnvironment(output, cpath, this);
            ((ClientProcessorEnvironment) env).setNames(new Names());
            boolean setVerbose = false;
            for (String key : apOptions.keySet()) {
                if (key.equals("-verbose"))
                    setVerbose=true;
/*                if (key.startsWith("-Averbose")) {
                    if (key.equals("-Averbose")) {
                        setVerbose = true;
                        break;
                    } else {
                        String value = key.substring(key.indexOf('=')+1);
                        if (value.equals("true")) {
                            setVerbose = true;
                            break;
                        }
                    }
                }*/
            }
            if (setVerbose) {
                env.setFlags(ProcessorEnvironment.F_VERBOSE);
            }
            messager = apEnv.getMessager();
        }
        env.setFiler(apEnv.getFiler());
    }

    public AnnotationProcessorEnvironment getAPEnv() {
        return apEnv;
    }

    public ProcessorEnvironment getEnvironment() {
        return env;
    }

    public ProcessorEnvironment getProcessorEnvironment() {
        return env;
    }

    public File getSourceDir() {
        return sourceDir;
    }

    public void onError(String key) {
        onError(new LocalizableMessage(getResourceBundleName(), key));
    }

    public void onError(String key, Object[] args) throws ModelerException {
        onError(new LocalizableMessage(getResourceBundleName(), key, args));
    }

    public void onError(Localizable msg) throws ModelerException {
        if (messager != null) {
            messager.printError(localizer.localize(msg));
        } else {
            throw new ModelerException(msg);
        }
    }

    public void onWarning(Localizable msg) {
        String message = localizer.localize(getMessage("webserviceap.warning", localizer.localize(msg)));
        if (messager != null) {
            messager.printWarning(message);
        } else {
            report(message);
        }
    }
    public void onInfo(Localizable msg) {
        if (messager != null) {
            String message = localizer.localize(msg);
            messager.printNotice(message);
        } else {
            String message = localizer.localize(getMessage("webserviceap.info", localizer.localize(msg)));
            report(message);
        }
    }

    public void process() {
        if (context.getRound() == 1) {
            buildModel();
        }
        if (!wrapperGenerated  || // the wrappers already exist
            context.getRound() == 2 ||
            context.allEncoded()) {
            if ((context.getRound() == 2 || !wrapperGenerated) && !context.isModelCompleted()) {
                completeModel();
                context.setModelCompleted(true);
            }
            try {
                for (SEIContext seiContext : context.getSEIContexts()) {
                    if (!seiContext.getModelCompiled()) {

                        runProcessorActions(seiContext.getModel());
                        seiContext.setModelCompiled(true);
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                if (messager != null && output != null && output.size() > 0) {
                    messager.printNotice(output.toString());
                }
            }
        }
        context.incrementRound();
    }

    public boolean checkAndSetProcessed(TypeDeclaration typeDecl) {
        if (!processedTypeDecls.contains(typeDecl)) {
            processedTypeDecls.add(typeDecl);
            return false;
        }
        return true;
    }

    public void clearProcessed() {
        processedTypeDecls.clear();
    }

    protected void runProcessorActions(Model model) throws Exception {
        if (tool != null)
            tool.runProcessorActions();
    }


    protected String getGenericErrorMessage() {
        return "webserviceap.error";
    }

    protected String getResourceBundleName() {
        return "com.sun.tools.ws.resources.webserviceap";
    }

    public Localizable getVersion() {
        return getMessage("webserviceap.version",
            Version.PRODUCT_NAME,
            Version.VERSION_NUMBER,
            Version.BUILD_NUMBER);
    }

    public String getVersionString() {
        return localizer.localize(getVersion());
    }

    public void createModel(TypeDeclaration d, QName modelName, String targetNamespace,
        String modelerClassName){

        SEIContext seiContext = context.getSEIContext(d);
        if (seiContext.getModel() != null) {
            onError("webserviceap.model.already.exists");
            return;
        }
        log("creating model: " + modelName);
        model = new Model(modelName);
        model.setTargetNamespaceURI(targetNamespace);
        model.setProperty(
            ModelProperties.PROPERTY_MODELER_NAME,
            modelerClassName);
        seiContext.setModel(model);
    }

    public void setService(Service service) {
        this.service = service;
        model.addService(service);
    }

    public void setPort(Port port) {
        this.port = port;
        service.addPort(port);
    }

    public void addOperation(Operation operation) {
        port.addOperation(operation);
    }

    public void setWrapperGenerated(boolean wrapperGenerated) {
        this.wrapperGenerated = wrapperGenerated;
    }

    public TypeDeclaration getTypeDeclaration(String typeName) {
        return apEnv.getTypeDeclaration(typeName);
    }

    private void buildModel() {
        WebService webService;
        WebServiceVisitor wrapperGenerator = createWrapperGenerator();
        boolean processedEndpoint = false;
        for (TypeDeclaration typedecl: apEnv.getSpecifiedTypeDeclarations()) {
            if (!(typedecl instanceof ClassDeclaration))
                continue;
            webService = typedecl.getAnnotation(WebService.class);
            if (!shouldProcessWebService(webService))
                continue;
            typedecl.accept(wrapperGenerator);
            processedEndpoint = true;
        }
        if (!processedEndpoint) {
            onError("webserviceap.no.webservice.endpoint.found");
        }
    }

    protected WebServiceVisitor createWrapperGenerator() {
        return new WebServiceWrapperGenerator(this, context);
    }

    protected WebServiceVisitor createReferenceCollector() {
        return new WebServiceReferenceCollector(this, context);
    }

    protected boolean shouldProcessWebService(WebService webService) {
        return webService != null;
    }


    private void completeModel() {
        clearProcessed();
        JavaCompiler javaC = XJC.createJavaCompiler();
        JAXBModel jaxBModel;
        WebServiceVisitor referenceCollector = createReferenceCollector();
        for (SEIContext seiContext : context.getSEIContexts()) {
            log("completing model for endpoint: "+seiContext.getSEIImplName());
            TypeDeclaration decl = apEnv.getTypeDeclaration(seiContext.getSEIImplName());
            if (decl == null)
                onError("webserviceap.could.not.find.typedecl",
                         new Object[] {seiContext.getSEIImplName(), context.getRound()});
            decl.accept(referenceCollector);
        }
        clearProcessed();
        for (SEIContext seiContext : context.getSEIContexts()) {
            TypeDeclaration decl = apEnv.getTypeDeclaration(seiContext.getSEIName());
            Collection<Reference> schemaMirrors = seiContext.getSchemaReferences(this);
/*
            System.out.println("schemaMirrors count: " + schemaMirrors.size());
            for (Reference reference : schemaMirrors) {System.out.println("reference: "+reference.type);}
        System.out.println("schemaElementMap count: "+ seiContext.getSchemaElementMap(this).entrySet().size());
            for (Map.Entry<QName, ? extends Reference> entry : seiContext.getSchemaElementMap(this).entrySet()) {
               System.out.println("name: " + entry.getKey()+" value: "+entry.getValue().type);
            }
*/
//            System.out.println("setting default namespaceURI: "+seiContext.getNamespaceURI());
            jaxBModel = new JAXBModel(javaC.bind(schemaMirrors, seiContext.getSchemaElementMap(this),
                seiContext.getNamespaceURI(), apEnv));
//            for (JAXBMapping map : jaxBModel.getMappings()) {System.out.println("map.getClazz: "+map.getClazz());}
            seiContext.setJAXBModel(jaxBModel);
            //decl.accept(webServiceModeler);
        }
    }

    public boolean isException(TypeDeclaration typeDecl) {
        return isSubtype(typeDecl, exceptionDecl);
    }

    public boolean isRemoteException(TypeDeclaration typeDecl) {
        return isSubtype(typeDecl, remoteExceptionDecl);
    }

    public boolean isRemote(TypeDeclaration typeDecl) {
        return isSubtype(typeDecl, remoteDecl);
    }


    public static boolean isSubtype(TypeDeclaration d1, TypeDeclaration d2) {
        if (d1.equals(d2))
            return true;
        ClassDeclaration superClassDecl = null;
        if (d1 instanceof ClassDeclaration) {
            ClassType superClass = ((ClassDeclaration)d1).getSuperclass();
            if (superClass != null) {
                superClassDecl = superClass.getDeclaration();
                if (superClassDecl.equals(d2))
                    return true;
            }
        }
        InterfaceDeclaration superIntf = null;
        Iterator<InterfaceType> interfaces = d1.getSuperinterfaces().iterator();
        while (interfaces.hasNext()) {
            superIntf = interfaces.next().getDeclaration();
            if (superIntf.equals(d2))
                return true;
        }
        if (superIntf != null && isSubtype(superIntf, d2)) {
            return true;
        } else if (superClassDecl != null && isSubtype(superClassDecl, d2)) {
            return true;
        }
        return false;
    }


    public static String getMethodSig(MethodDeclaration method) {
        StringBuffer buf = new StringBuffer(method.getSimpleName() + "(");
        Iterator<TypeParameterDeclaration> params = method.getFormalTypeParameters().iterator();
        TypeParameterDeclaration param;
        for (int i =0; params.hasNext(); i++) {
            if (i > 0)
                buf.append(", ");
            param = params.next();
            buf.append(param.getSimpleName());
        }
        buf.append(")");
        return buf.toString();
    }

    public String getOperationName(String messageName) {
        return messageName;
    }

    public String getResponseName(String operationName) {
        return env.getNames().getResponseName(operationName);
    }


    public TypeMirror getHolderValueType(TypeMirror type) {
        return TypeModeler.getHolderValueType(type, defHolderDecl, apEnv);
    }

    public boolean canOverWriteClass(String className) {
        return !((donotOverride && GeneratorUtil.classExists(env, className)));
    }

    public void log(String msg) {
        if (env != null && env.verbose()) {
            String message = "[" + msg + "]";
            if (messager != null) {
                messager.printNotice(message);
            } else {
                System.out.println(message);
            }
        }
    }

    public String getXMLName(String javaName) {
        return javaName;
    }

    // these methods added so that the WebService modeler can pick the names
    // it wants for the WSDL artifacts associated with ports and operations

/*    public QName getWSDLPortName(String portName) {
        return new QName(wsdlUri, getXMLName(portName + PORT));
    }

    public QName getWSDLBindingName(String portName) {
        return new QName(wsdlUri, getXMLName(portName + BINDING));
    }

    public QName getWSDLPortTypeName(String portName) {
        return new QName(wsdlUri, getXMLName(portName));
    }*/
}



