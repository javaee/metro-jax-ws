/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import com.sun.tools.ws.processor.ProcessorAction;
import com.sun.tools.ws.processor.ProcessorConstants;
import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.model.AbstractType;
import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.ModelVisitor;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Parameter;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Request;
import com.sun.tools.ws.processor.model.Response;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.jaxb.JAXBType;
import com.sun.tools.ws.processor.model.jaxb.JAXBTypeVisitor;
import com.sun.tools.ws.processor.model.jaxb.RpcLitStructure;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.ToolVersion;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;

/**
 *
 * @author WS Development Team
 */
public abstract class GeneratorBase
    implements
        GeneratorConstants,
        ProcessorAction,
        ModelVisitor,
        JAXBTypeVisitor {
    protected File sourceDir;
    protected File destDir;
    protected File nonclassDestDir;
    protected ProcessorEnvironment env;
    protected Model model;
    protected Service service;
    protected SOAPVersion curSOAPVersion;
    protected String targetVersion;
    protected boolean donotOverride;
    protected String servicePackage;
    protected JCodeModel cm;
    protected boolean printStackTrace;
    protected String wsdlLocation;

    private LocalizableMessageFactory messageFactory;

    public GeneratorBase() {
        sourceDir = null;
        destDir = null;
        nonclassDestDir = null;
        env = null;
        model = null;
    }

    public void perform(
        Model model,
        Configuration config,
        Properties properties) {

        GeneratorBase generator = getGenerator(model, config, properties);

        generator.doGeneration();
    }

    public abstract GeneratorBase getGenerator(
        Model model,
        Configuration config,
        Properties properties);
    public abstract GeneratorBase getGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver);

    protected GeneratorBase(
        Model model,
        Configuration config,
        Properties properties) {

        this.model = model;

        if(model.getJAXBModel().getS2JJAXBModel() != null)
            cm = model.getJAXBModel().getS2JJAXBModel().generateCode(null, new JAXBTypeGenerator.JAXBErrorListener());
        else
            cm = new JCodeModel();

        this.env = (ProcessorEnvironment) config.getEnvironment();
        String key = ProcessorOptions.DESTINATION_DIRECTORY_PROPERTY;
        String dirPath = properties.getProperty(key);
        this.destDir = new File(dirPath);
        key = ProcessorOptions.SOURCE_DIRECTORY_PROPERTY;
        String sourcePath = properties.getProperty(key);
        this.sourceDir = new File(sourcePath);
        key = ProcessorOptions.NONCLASS_DESTINATION_DIRECTORY_PROPERTY;
        String nonclassDestPath = properties.getProperty(key);
        this.nonclassDestDir = new File(nonclassDestPath);
        if (nonclassDestDir == null)
            nonclassDestDir = destDir;
        messageFactory =
            new LocalizableMessageFactory("com.sun.tools.ws.resources.generator");
        this.targetVersion =
            properties.getProperty(ProcessorOptions.JAXWS_SOURCE_VERSION);
        key = ProcessorOptions.DONOT_OVERRIDE_CLASSES;
        this.donotOverride =
            Boolean.valueOf(properties.getProperty(key)).booleanValue();
        this.printStackTrace = Boolean.valueOf(properties.getProperty(ProcessorOptions.PRINT_STACK_TRACE_PROPERTY));
        this.wsdlLocation = properties.getProperty(ProcessorOptions.WSDL_LOCATION);
    }

    protected void doGeneration() {
        try {
            model.accept(this);
        } catch (Exception e) {
            if (env.verbose())
                e.printStackTrace();
            throw new GeneratorException("generator.nestedGeneratorError",e);
        }
    }

    public void visit(Model model) throws Exception {
        preVisitModel(model);
        visitModel(model);
        postVisitModel(model);
    }

    protected void preVisitModel(Model model) throws Exception {
    }

    protected void visitModel(Model model) throws Exception {
        env.getNames().resetPrefixFactory();
        for (Service service : model.getServices()) {
            service.accept(this);
        }
    }

    protected void postVisitModel(Model model) throws Exception {
    }

    public void visit(Service service) throws Exception {
        preVisitService(service);
        visitService(service);
        postVisitService(service);
    }

    protected void preVisitService(Service service) throws Exception {
        servicePackage = Names.getPackageName(service);
    }

    protected void visitService(Service service) throws Exception {
        this.service = service;
//        Iterator ports = service.getPorts();
        for (Port port : service.getPorts()) {
            port.accept(this);
        }
        this.service = null;
    }

    protected void postVisitService(Service service) throws Exception {
        Iterator extraTypes = model.getExtraTypes();
        while (extraTypes.hasNext()) {
            AbstractType type = (AbstractType) extraTypes.next();
        }
        servicePackage = null;
    }

    public void visit(Port port) throws Exception {
        preVisitPort(port);
        visitPort(port);
        postVisitPort(port);
    }

    protected void preVisitPort(Port port) throws Exception {
        curSOAPVersion = port.getSOAPVersion();
    }

    protected void visitPort(Port port) throws Exception {
        for (Operation operation : port.getOperations()) {
            operation.accept(this);
        }
    }

    protected void postVisitPort(Port port) throws Exception {
        curSOAPVersion = null;
    }

    public void visit(Operation operation) throws Exception {
        preVisitOperation(operation);
        visitOperation(operation);
        postVisitOperation(operation);
    }

    protected void preVisitOperation(Operation operation) throws Exception {
    }

    protected void visitOperation(Operation operation) throws Exception {
        operation.getRequest().accept(this);
        if (operation.getResponse() != null)
            operation.getResponse().accept(this);
        Iterator faults = operation.getFaultsSet().iterator();
        if (faults != null) {
            Fault fault;
            while (faults.hasNext()) {
                fault = (Fault) faults.next();
                fault.accept(this);
            }
        }
    }

    protected void postVisitOperation(Operation operation) throws Exception {
    }

    public void visit(Parameter param) throws Exception {
        preVisitParameter(param);
        visitParameter(param);
        postVisitParameter(param);
    }

    protected void preVisitParameter(Parameter param) throws Exception {
    }

    protected void visitParameter(Parameter param) throws Exception {
    }

    protected void postVisitParameter(Parameter param) throws Exception {
    }

    public void visit(Block block) throws Exception {
        preVisitBlock(block);
        visitBlock(block);
        postVisitBlock(block);
    }

    protected void preVisitBlock(Block block) throws Exception {
    }

    protected void visitBlock(Block block) throws Exception {
    }

    protected void postVisitBlock(Block block) throws Exception {
    }

    public void visit(Response response) throws Exception {
        preVisitResponse(response);
        visitResponse(response);
        postVisitResponse(response);
    }

    protected void preVisitResponse(Response response) throws Exception {
    }

    protected void visitResponse(Response response) throws Exception {
        Iterator iter = response.getParameters();
        AbstractType type;
        Block block;
        while (iter.hasNext()) {
            ((Parameter) iter.next()).accept(this);
        }
        iter = response.getBodyBlocks();
        while (iter.hasNext()) {
            block = (Block) iter.next();
            type = block.getType();
            if(type instanceof JAXBType)
                ((JAXBType) type).accept(this);
            else if(type instanceof RpcLitStructure)
                ((RpcLitStructure) type).accept(this);
            
            responseBodyBlock(block);
        }
        iter = response.getHeaderBlocks();
        while (iter.hasNext()) {
            block = (Block) iter.next();
            type = block.getType();
            if(type instanceof JAXBType)
                ((JAXBType) type).accept(this);
            responseHeaderBlock(block);
        }

        //attachment
        iter = response.getAttachmentBlocks();
        while (iter.hasNext()) {
            block = (Block) iter.next();
            type = block.getType();
            if(type instanceof JAXBType)
                ((JAXBType) type).accept(this);
            responseAttachmentBlock(block);
        }

    }

    protected void responseBodyBlock(Block block) throws Exception {
    }

    protected void responseHeaderBlock(Block block) throws Exception {
    }

    protected void responseAttachmentBlock(Block block) throws Exception {
    }

    protected void postVisitResponse(Response response) throws Exception {
    }

    public void visit(Request request) throws Exception {
        preVisitRequest(request);
        visitRequest(request);
        postVisitRequest(request);
    }

    protected void preVisitRequest(Request request) throws Exception {
    }

    protected void visitRequest(Request request) throws Exception {
        Iterator iter = request.getParameters();
        AbstractType type;
        Block block;
        while (iter.hasNext()) {
            ((Parameter) iter.next()).accept(this);
        }
        iter = request.getBodyBlocks();
        while (iter.hasNext()) {
            block = (Block) iter.next();
            type = block.getType();
            if(type instanceof JAXBType)
                ((JAXBType) type).accept(this);
            else if(type instanceof RpcLitStructure)
                ((RpcLitStructure) type).accept(this);
            requestBodyBlock(block);
        }
        iter = request.getHeaderBlocks();
        while (iter.hasNext()) {
            block = (Block) iter.next();
            type = block.getType();
            if(type instanceof JAXBType)
                ((JAXBType) type).accept(this);
            requestHeaderBlock(block);
        }
    }

    protected void requestBodyBlock(Block block) throws Exception {
    }

    protected void requestHeaderBlock(Block block) throws Exception {
    }

    protected void postVisitRequest(Request request) throws Exception {
    }

    public void visit(Fault fault) throws Exception {
        preVisitFault(fault);
        visitFault(fault);
        postVisitFault(fault);
    }

    protected void preVisitFault(Fault fault) throws Exception {
    }

    protected void visitFault(Fault fault) throws Exception {
    }

    protected void postVisitFault(Fault fault) throws Exception {
    }

    protected void writeWarning(IndentingWriter p) throws IOException {
        writeWarning(p, targetVersion);
    }

    public List<String> getJAXWSClassComment(){
        return getJAXWSClassComment(targetVersion);
    }
    
    public static List<String> getJAXWSClassComment(String targetVersion) {
        List<String> comments = new ArrayList<String>();
        comments.add("This class was generated by the JAXWS SI.\n");
        comments.add(ToolVersion.VERSION.BUILD_VERSION+"\n");
        comments.add("Generated source version: " + targetVersion);
        return comments;        
    }

    public static void writeWarning(IndentingWriter p,
        String targetVersion) throws IOException {
        /*
         * Write boiler plate comment.
         */
        p.pln("// This class was generated by the JAX SI, do not edit.");
        p.pln("// Contents subject to change without notice.");
        p.pln("// " + ToolVersion.VERSION.BUILD_VERSION);
        p.pln("// Generated source version: " + targetVersion);
        p.pln();
    }

    public void writePackage(IndentingWriter p, String classNameStr)
        throws IOException {

        writePackage(p, classNameStr, targetVersion);
    }

    public static void writePackage(
        IndentingWriter p,
        String classNameStr,
        String sourceVersion)
        throws IOException {

        writeWarning(p, sourceVersion);
        writePackageOnly(p, classNameStr);
    }

    public static void writePackageOnly(IndentingWriter p, String classNameStr)
        throws IOException {
        int idx = classNameStr.lastIndexOf(".");
        if (idx > 0) {
            p.pln("package " + classNameStr.substring(0, idx) + ";");
            p.pln();
        }
    }
    
    
    protected JDefinedClass getClass(String className, ClassType type) {
        JDefinedClass cls = null;
        try {
            cls = cm._class(className, type);
        } catch (JClassAlreadyExistsException e){
            cls = cm._getClass(className);
        }        
        return cls;
    }    

    protected void log(String msg) {
        if (env.verbose()) {
            System.out.println(
                "["
                    + Names.stripQualifier(this.getClass().getName())
                    + ": "
                    + msg
                    + "]");
        }
    }

    protected void warn(String key) {
        env.warn(messageFactory.getMessage(key));
    }

    protected void warn(String key, String arg) {
        env.warn(messageFactory.getMessage(key, arg));
    }

    protected void warn(String key, Object[] args) {
        env.warn(messageFactory.getMessage(key, args));
    }

    protected void info(String key) {
        env.info(messageFactory.getMessage(key));
    }

    protected void info(String key, String arg) {
        env.info(messageFactory.getMessage(key, arg));
    }

    protected static void fail(String key) {
        throw new GeneratorException(key);
    }

    protected static void fail(String key, String arg) {
        throw new GeneratorException(key, arg);
    }

    protected static void fail(String key, String arg1, String arg2) {
        throw new GeneratorException(key, new Object[] { arg1, arg2 });
    }

    protected static void fail(Localizable arg) {
        throw new GeneratorException("generator.nestedGeneratorError", arg);
    }

    protected static void fail(Throwable arg) {
        throw new GeneratorException(
            "generator.nestedGeneratorError",
            arg);
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.processor.model.jaxb.JAXBTypeVisitor#visit(com.sun.xml.ws.processor.model.jaxb.JAXBType)
     */
    public void visit(JAXBType type) throws Exception {
        preVisitJAXBType(type);
        visitJAXBType(type);
        postVisitJAXBType(type);

    }

    /**
     * @param type
     */
    protected void postVisitJAXBType(JAXBType type) {
        // TODO Auto-generated method stub

    }

    /**
     * @param type
     */
    protected void visitJAXBType(JAXBType type) {
        // TODO Auto-generated method stub

    }

    /**
     * @param type
     */
    protected void preVisitJAXBType(JAXBType type) {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see com.sun.xml.ws.processor.model.jaxb.JAXBTypeVisitor#visit(com.sun.xml.ws.processor.model.jaxb.RpcLitStructure)
     */
    public void visit(RpcLitStructure type) throws Exception {
        // TODO Auto-generated method stub

    }

}
