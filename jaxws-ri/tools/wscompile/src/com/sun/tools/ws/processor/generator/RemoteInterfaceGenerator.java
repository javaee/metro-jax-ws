/*
 * $Id: RemoteInterfaceGenerator.java,v 1.5 2005-07-24 01:35:09 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import com.sun.tools.ws.processor.ProcessorAction;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.model.*;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.jaxb.JAXBType;
import com.sun.tools.ws.processor.util.DirectoryUtil;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.wsdl.document.soap.SOAPUse;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author WS Development Team
 */
public class RemoteInterfaceGenerator extends GeneratorBase implements ProcessorAction {
    protected boolean isDocStyle;
    protected boolean isLiteralUse;
    protected boolean isWrapped;
    protected String SOAPBINDING_PKG = "com.sun.xml.ws";
    private String wsdlLocation;
    private WSDLModelInfo wsdlModelInfo;

    public RemoteInterfaceGenerator() {
        super();
    }

    protected RemoteInterfaceGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        super(model, config, properties);
        this.wsdlLocation = ((WSDLModelInfo)config.getModelInfo()).getLocation();
        this.wsdlModelInfo = (WSDLModelInfo)config.getModelInfo();
    }

    public GeneratorBase getGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        return new RemoteInterfaceGenerator(model, config, properties);
    }

    public GeneratorBase getGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver) {
        return new RemoteInterfaceGenerator(model, config, properties);
    }

    protected void doGeneration() {
        String modelerName =
            (String) model.getProperty(
                ModelProperties.PROPERTY_MODELER_NAME);
        if (modelerName != null
            && modelerName.equals(
                "com.sun.xml.rpc.processor.modeler.rmi.RmiModeler")) {
            // do not generate a remote interface if the model was produced by the RMI modeler
            return;
        }
        super.doGeneration();
    }

    protected void visitPort(Port port) {
        if (port.isProvider()) {
            return;                // Not generating for Provider based endpoint
        }
        JavaInterface intf = port.getJavaInterface();
        try {
            String className = env.getNames().customJavaTypeClassName(intf);
            if ((donotOverride && GeneratorUtil.classExists(env, className))) {
                log("Class " + className + " exists. Not overriding.");
                return;
            }
            File classFile =
                env.getNames().sourceFileForClass(
                    className,
                    className,
                    sourceDir,
                    env);

            /* adding the file name and its type */
            GeneratedFileInfo fi = new GeneratedFileInfo();
            fi.setFile(classFile);
            fi.setType(GeneratorConstants.FILE_TYPE_REMOTE_INTERFACE);
            env.addGeneratedFile(fi);

            IndentingWriter out =
                new IndentingWriter(
                    new OutputStreamWriter(new FileOutputStream(classFile)));
            writePackage(out, className);
            boolean canAnnotate = canAnnotate(port);
            writeImports(out, canAnnotate);
            writeWebService(out, port, service, canAnnotate);
            out.plnI(
                "public interface "
                    + Names.stripQualifier(className)
                    + " extends java.rmi.Remote {");

            for (Operation operation: port.getOperationsList()) {
                JavaMethod method = operation.getJavaMethod();
                writeWebMethod(out, operation, port, canAnnotate);
                out.p("public ");
                if (method.getReturnType() == null) {
                    out.p("void");
                } else {
                    Response resp = operation.getResponse();
                    if(resp != null){
                        for (Parameter parameter : operation.getResponse().getParametersList()) {
                            if (parameter.getParameterOrderPosition() == -1) {
                                out.pln();
                                writeJAXBTypeAnnotations(out, parameter);
                            }
                        }
                    }

                    out.p(method.getReturnType().getName());
                }
                out.p(" ");
                out.p(method.getName());
                out.p("(");
                boolean first = true;

                for (JavaParameter parameter: method.getParametersList()) {
                    if (!first) {
                        out.p(", ");
                    }
                    writeWebParam(out, parameter, operation, canAnnotate);
                    if (parameter.isHolder()) {
                        out.p(
                            env.getNames().holderClassName(
                                port,
                                parameter.getType()));
                    } else {
                        out.p(
                            env.getNames().typeClassName(parameter.getType()));
                    }
                    out.p(" ");
                    out.p(parameter.getName());
                    writePostWebParam(out, parameter, operation, canAnnotate);
                    first = false;
                }
                Iterator exceptions = method.getExceptions();
                if(exceptions.hasNext() || method.getThrowsRemoteException()){
                    out.plnI(") throws ");
                    String exception;
                    boolean firstException = true;
                    while (exceptions.hasNext()) {
                        exception = (String) exceptions.next();
                        if(!firstException)
                            out.p(", ");
                        else
                            firstException = false;
                        out.p(exception);
                    }
                    if(method.getThrowsRemoteException()){
                        if(!firstException)
                            out.p(", ");
                        out.pln(" java.rmi.RemoteException;");
                        out.pO();
                    }else{
                        out.pln(";");
                    }
                }else{
                    out.pln(");");
                }
                out.pln();
//                out.pO();
            }

            out.pOln("}");
            out.close();

        } catch (Exception e) {
            throw new GeneratorException(
                "generator.nestedGeneratorError",
                new LocalizableExceptionAdapter(e));
        }
    }

    protected boolean canAnnotate(Port port) {
 /*       boolean canAnnotate = true;
        boolean isDocStyle = false;
        boolean isLiteralUse = false;;
        boolean isWrapped = false;
        boolean first = true;
        for (Operation operation: port.getOperationsList()) {
            if (first) {
                isDocStyle = operation.getStyle().equals(SOAPStyle.DOCUMENT);
                isLiteralUse = operation.getUse().equals(SOAPUse.LITERAL);
                if (isDocStyle && isLiteralUse && isWrapped(operation)) {
                    isWrapped = true;
                }
                first = false;
            } else if ((operation.getStyle().equals(SOAPStyle.DOCUMENT) != isDocStyle ||
                       operation.getUse().equals(SOAPUse.LITERAL) != isLiteralUse) ||
                       (isDocStyle && isLiteralUse && isWrapped(operation) != isWrapped)) {
                canAnnotate = false;
            }
        }*/
        return true;
//        return canAnnotate;
    }

    protected void writeImports(IndentingWriter p, boolean canAnnotate) throws IOException {
/*        if (canAnnotate) {
            p.pln("import javax.jws.WebService;");
            p.pln("import javax.jws.OneWay;");
            p.pln("import javax.jws.WebMethod;");
            p.pln("import javax.jws.WebResult;");
            p.pln("import javax.jws.WebParam;");
            p.pln("import javax.jws.HandlerChain;");
            p.pln("import javax.jws.soap.SOAPBinding;");
            p.pln("import javax.jws.soap.SOAPMessageHandlers;");
            p.pln();
        }*/
    }

    protected void writeWebService(IndentingWriter p, Port port, Service service, boolean canAnnotate)
        throws IOException {

        if (!canAnnotate)
            return;

        String serviceName = Names.stripQualifier(env.getNames()
            .customJavaTypeClassName(service.getJavaInterface()));
        String name = Names.stripQualifier(env.getNames()
            .customJavaTypeClassName(port.getJavaInterface()));
        String targetNamespace = service.getName().getNamespaceURI();

        p.plnI("@javax.jws.WebService(");
        p.pln("name=\""+name+"\",");
        p.pln("serviceName=\""+serviceName+"\",");
        p.pln("targetNamespace=\""+targetNamespace+"\",");
        //how do we prevent it from generated on server SEI?
        p.pln("wsdlLocation=\"" + wsdlLocation +"\"");
        p.pOln(")");
        writeHandlerConfig(p, env.getNames()
                .customJavaTypeClassName(port.getJavaInterface()));
        writeSOAPBinding(p, port);
    }

    private void writeHandlerConfig(IndentingWriter p, String fullName) throws IOException {
        Element e = wsdlModelInfo.getHandlerConfig();
        if(e == null)
            return;
        NodeList nl = e.getElementsByTagNameNS("http://www.bea.com/xml/ns/jws", "handler-chain-name");
        if(nl.getLength() > 0){
            Element hn = (Element)nl.item(0);
            String fName = getHandlerConfigFileName(fullName);
            p.plnI("@javax.jws.HandlerChain(");
            p.pln("name=\""+hn.getTextContent()+"\",");
            p.pln("file=\""+fName+"\"");
            p.pOln(")");
            generateHandlerChainFile(e, fullName);
        }
    }

    private String getHandlerConfigFileName(String fullName){
        String name = Names.stripQualifier(fullName);
        return name+"_handler.xml";
    }

    private void generateHandlerChainFile(Element hc, String name) {
        String hcName = getHandlerConfigFileName(name);

        File packageDir = DirectoryUtil.getOutputDirectoryFor(name, destDir, env);
        File hcFile = new File(packageDir, hcName);

        /* adding the file name and its type */
        GeneratedFileInfo fi = new GeneratedFileInfo();
        fi.setFile(hcFile);
        fi.setType(FILE_TYPE_HANDLER_CONFIG);
        env.addGeneratedFile(fi);

        try {
            IndentingWriter p =
                new IndentingWriter(
                    new OutputStreamWriter(new FileOutputStream(hcFile)));
            Transformer it = TransformerFactory.newInstance().newTransformer();

            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "2");
            it.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            String pfix = hc.getPrefix();
            Element e = hc.getOwnerDocument().createElementNS("http://www.bea.com/xml/ns/jws", "handler-config");
            if(pfix != null)
                e.setPrefix(pfix);

            e.appendChild(hc);
            it.transform( new DOMSource(e), new StreamResult(p) );
        } catch (Exception e) {
            throw new GeneratorException(
                    "generator.nestedGeneratorError",
                    new LocalizableExceptionAdapter(e));
        }
    }

    protected void writeSOAPBinding(IndentingWriter p, Port port) throws IOException {
        isDocStyle = false;
        isLiteralUse = true;
        isWrapped = true;
        boolean first = true;
/*        for (Operation operation: port.getOperationsList()) {
            if (first) {
                isDocStyle = operation.getStyle().equals(SOAPStyle.DOCUMENT);
                isLiteralUse = operation.getUse().equals(SOAPUse.LITERAL);
                isWrapped = isWrapped(operation);
                first = false;
            }
        }*/
        isDocStyle = port.getStyle() != null ? port.getStyle().equals(SOAPStyle.DOCUMENT) : true;
        isWrapped = port.isWrapped() || !isDocStyle;
        String style = "javax.jws.soap.SOAPBinding.Style.";
        String use = "javax.jws.soap.SOAPBinding.Use.";
        String parameterStyle = "javax.jws.soap.SOAPBinding.ParameterStyle.";

        style +=  isDocStyle ? "DOCUMENT," : "RPC,";
        use +=  isLiteralUse ? "LITERAL" : "ENCODED";
        if (isWrapped) {
            parameterStyle += "WRAPPED";
        } else {
            parameterStyle += "BARE";
        }
        p.plnI("@javax.jws.soap.SOAPBinding(");
        p.pln("style="+style);
        p.p("use="+use);
//        if (isDocStyle) {
            p.pln(",");
            p.p("parameterStyle="+parameterStyle);
//        }
        p.pln(")");
        // TODO SOAPVersion
//        p.pln("version=\"1.1\")");
        p.pO();
    }


    protected void writeWebMethod(IndentingWriter p, Operation operation, Port port, boolean canAnnotate)
        throws IOException {
        if (!canAnnotate || !canAnnotate(operation))
            return;
        boolean isDocStyle = operation.getStyle().equals(SOAPStyle.DOCUMENT);
//      System.out.println("isDocStyle: " +isDocStyle);
        boolean isLiteralUse = operation.getUse().equals(SOAPUse.LITERAL);
//      System.out.println("isLiteralUse: " +isLiteralUse);
        boolean isWrapped = operation.isWrapped() || !isDocStyle;
        boolean sameStyle = operation.getStyle().equals(port.getStyle());
        boolean sameWrapped = isWrapped == port.isWrapped();

        JavaMethod method = operation.getJavaMethod();
        String requestWrapper = getMessageWrapper(operation.getRequest());
        String responseWrapper = null;
        Response response = operation.getResponse();

        if (response != null)
            responseWrapper = getMessageWrapper(response);

        String operationName = (operation instanceof AsyncOperation)?
                ((AsyncOperation)operation).getNormalOperation().getName().getLocalPart():
                operation.getName().getLocalPart();
        p.p("@javax.jws.WebMethod(operationName=\""+operationName+"\"");

        if (operation.getSOAPAction() != null && operation.getSOAPAction().length() > 0)
            p.pln(", action=\""+operation.getSOAPAction()+"\")");
        else
            p.pln(")");
        if (operation.getResponse() == null)
            p.pln("@javax.jws.Oneway");
        else if (!operation.getJavaMethod().getReturnType().getName().equals("void") &&
                 operation.getResponse().getBodyBlocks().hasNext()){
            Block block = operation.getResponse().getBodyBlocks().next();
            String resultName = block.getName().getLocalPart();
            String nsURI = block.getName().getNamespaceURI();
            for (Parameter parameter : operation.getResponse().getParametersList()) {
                if (parameter.getParameterOrderPosition() == -1) {
                    if(isWrapped){
                        resultName = parameter.getName();
                    }else if(isDocStyle){
                        JAXBType t = (JAXBType)parameter.getType();
                        resultName = t.getName().getLocalPart();
                        nsURI = t.getName().getNamespaceURI();
                    }
                }
            }
            if(!(operation instanceof AsyncOperation)){
                p.p("@javax.jws.WebResult(name=\""+resultName+"\"");
                if (operation.getStyle().equals(SOAPStyle.DOCUMENT)) {
                    p.p(", targetNamespace=\""+nsURI+"\"");
                }
                p.pln(")");
            }
        }


        if (!sameStyle || (!sameWrapped && isDocStyle)) {
            String style = SOAPBINDING_PKG+".SOAPBinding.Style.";
//            String use = "SOAPBinding.Use.";
            String parameterStyle =SOAPBINDING_PKG+".SOAPBinding.ParameterStyle.";
//            System.out.println("operation: "+operation.getName()+" isWrapped: "+isWrapped);
            style +=  isDocStyle ? "DOCUMENT" : "RPC";
//            use +=  isLiteralUse ? "LITERAL" : "ENCODED";
            if (isWrapped) {
                parameterStyle += "WRAPPED";
            } else {
                parameterStyle += "BARE";
            }
            p.plnI("@"+SOAPBINDING_PKG+".SOAPBinding(");
            if (!sameStyle)
                p.p("style="+style);
//            p.pln("use="+use);
            if (!sameWrapped) {
                if (!sameStyle)
                    p.pln(",");
                p.pln("parameterStyle="+parameterStyle+")");
            } else {
                p.pln(")");
            }
            p.pO();
        }
        if (operation.isWrapped() && operation.getStyle().equals(SOAPStyle.DOCUMENT)) {
            //String reqWrapper;
            //String resWrapper;
            //reqWrapper = ((Block)operation.getRequest().getBodyBlocks().next()).getType().getJavaType().getName();
            //p.p("@com.sun.xml.ws.WebWrapper(requestWrapper=\""+reqWrapper+"\"");
            Block reqBlock = operation.getRequest().getBodyBlocks().next();
            p.plnI("@com.sun.xml.ws.RequestWrapper(");
            p.pln("name=\""+reqBlock.getName().getLocalPart()+"\",");
            p.pln("namespace=\""+reqBlock.getName().getNamespaceURI()+"\",");
            p.pln("type=\""+reqBlock.getType().getJavaType().getName()+"\"");
            p.pOln(")");
            if (response != null) {
                Block resBlock = response.getBodyBlocks().next();
                p.plnI("@com.sun.xml.ws.ResponseWrapper(");
                p.pln("name=\""+resBlock.getName().getLocalPart()+"\",");
                p.pln("namespace=\""+resBlock.getName().getNamespaceURI()+"\",");
                p.pln("type=\""+resBlock.getType().getJavaType().getName()+"\"");
                p.pOln(")");
            }

//            if (response != null ) {
//                resWrapper = ((Block)response.getBodyBlocks().next()).getType().getJavaType().getName();
//                p.plnI(",");
//                p.pln("responseWrapper=\""+resWrapper+"\")");
//                p.pO();
//            } else {
//                p.pln(")");
//            }
        }
    }

    /**
     * @param operation
     * @return
     */
    private boolean canAnnotate(Operation operation) {
        return true;
        //return !(operation instanceof AsyncOperation);
    }

    private String getMessageWrapper(Message message){
        Iterator<Block> blockIter = message.getBodyBlocks();
        if(!blockIter.hasNext())
            return null;
        AbstractType msgType = blockIter.next().getType();
        if (msgType instanceof JAXBType) {
            JAXBType structType = (JAXBType)msgType;
            if (structType.isUnwrapped())
                return structType.getName().getLocalPart();
        }
        return null;
    }

    protected void writeWebParam(IndentingWriter p,
                               JavaParameter javaParameter,
                               Operation operation,
                               boolean canAnnotate)
        throws IOException {

        if (!canAnnotate || !canAnnotate(operation))
            return;
        Parameter param = javaParameter.getParameter();
        Request req = operation.getRequest();
        Response res = operation.getResponse();
        String mode = "javax.jws.WebParam.Mode.";
        boolean header = isHeaderParam(param, req) ||
            (res != null ? isHeaderParam(param, res) : false);
//        String name = javaParameter.getName();
        String name;
        boolean isWrapped = operation.isWrapped(); //isWrapped(operation);
        boolean writeMode = true;
/*        if (isDocStyle && !isWrapped) {
            name = param.getBlock().getType().getName().getLocalPart();
        } else {*/
//            name = param.getName();
//        }

        if((param.getBlock().getLocation() == Block.HEADER) || (isDocStyle && !isWrapped))
            name = param.getBlock().getName().getLocalPart();
        else
            name = param.getName();

        if (param.getLinkedParameter() != null)
            mode += "INOUT";
        else if (res != null && (isMessageParam(param, res) || isHeaderParam(param, res)))
            mode += "OUT";
        else {
            mode += "IN";
            writeMode = false;
        }
        String requestWrapper = getMessageWrapper(operation.getRequest());
        String responseWrapper = null;
        Response response = operation.getResponse();
        if (response != null)
            responseWrapper = getMessageWrapper(response);
        p.pln();
        p.pI();
        p.p("@javax.jws.WebParam(name=\""+name+"\"");
//        if (isDocStyle && ((responseWrapper == null && response==null) || requestWrapper == null)) {
        if (isDocStyle) { //((responseWrapper == null && response==null) || requestWrapper == null)) {
            String ns = param.getBlock().getName().getNamespaceURI(); // its bare nsuri
            if(isWrapped){
                ns = ((JAXBType)param.getType()).getName().getNamespaceURI();
            }
            p.p(", targetNamespace=\""+ns+"\"");            
        }else if(!isDocStyle && header){
            p.p(", targetNamespace=\""+param.getBlock().getName().getNamespaceURI()+"\"");
        }
        if (header) {
            p.p(", header=true");
        }
        if (writeMode)
            p.p(", mode="+mode+")");
        else
            p.p(")");
        p.pln();
        writeJAXBTypeAnnotations(p, param);
        p.pln();
    }

    protected void writeJAXBTypeAnnotations(IndentingWriter p, Parameter param) throws IOException{
        List<String> annotations = param.getAnnotations();
        if(annotations == null)
            return;
        for(String annotation:param.getAnnotations()){
            p.pln(annotation);
        }
    }

    protected void writePostWebParam(IndentingWriter p,
                               JavaParameter javaParameter,
                               Operation operation, boolean canAnnotate)
        throws IOException {
        if (!canAnnotate || !canAnnotate(operation))
            return;
        p.pO();
    }

    private boolean isHeaderParam(Parameter param, Message message) {
        if (message.getHeaderBlockCount() == 0)
            return false;

        for (Block headerBlock : message.getHeaderBlocksMap().values())
            if (param.getBlock().equals(headerBlock))
                return true;

        return false;
    }

    private boolean isMessageParam(Parameter param, Message message) {
        Block block = param.getBlock();

        return (message.getBodyBlockCount() > 0 && block.equals(message.getBodyBlocks().next())) ||
               (message.getHeaderBlockCount() > 0 &&
               block.equals(message.getHeaderBlocks().next()));
    }
}
