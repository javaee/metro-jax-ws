/*
 * $Id: WSDLGenerator.java,v 1.3 2005-07-18 18:13:58 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.ProcessorAction;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.generator.JAXBTypeGenerator.JAXBErrorListener;
import com.sun.tools.ws.processor.model.AbstractType;
import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Message;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.ModelProperties;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Parameter;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Request;
import com.sun.tools.ws.processor.model.Response;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaException;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.jaxb.RpcLitMember;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.wsdl.document.Binding;
import com.sun.tools.ws.wsdl.document.BindingFault;
import com.sun.tools.ws.wsdl.document.BindingInput;
import com.sun.tools.ws.wsdl.document.BindingOperation;
import com.sun.tools.ws.wsdl.document.BindingOutput;
import com.sun.tools.ws.wsdl.document.Definitions;
import com.sun.tools.ws.wsdl.document.Input;
import com.sun.tools.ws.wsdl.document.MessagePart;
import com.sun.tools.ws.wsdl.document.OperationStyle;
import com.sun.tools.ws.wsdl.document.Output;
import com.sun.tools.ws.wsdl.document.PortType;
import com.sun.tools.ws.wsdl.document.WSDLDocument;
import com.sun.tools.ws.wsdl.document.schema.Schema;
import com.sun.tools.ws.wsdl.document.schema.SchemaConstants;
import com.sun.tools.ws.wsdl.document.schema.SchemaElement;
import com.sun.tools.ws.wsdl.document.schema.SchemaKinds;
import com.sun.tools.ws.wsdl.document.soap.SOAP12Constants;
import com.sun.tools.ws.wsdl.document.soap.SOAPAddress;
import com.sun.tools.ws.wsdl.document.soap.SOAPBinding;
import com.sun.tools.ws.wsdl.document.soap.SOAPBody;
import com.sun.tools.ws.wsdl.document.soap.SOAPConstants;
import com.sun.tools.ws.wsdl.document.soap.SOAPFault;
import com.sun.tools.ws.wsdl.document.soap.SOAPHeader;
import com.sun.tools.ws.wsdl.document.soap.SOAPOperation;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.wsdl.document.soap.SOAPUse;
import com.sun.tools.ws.wsdl.document.Types;
import com.sun.tools.ws.wsdl.framework.DuplicateEntityException;
import com.sun.tools.ws.wsdl.parser.Constants;
import com.sun.tools.ws.wsdl.parser.WSDLWriter;
import com.sun.xml.ws.encoding.soap.SOAPVersion;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import com.sun.tools.xjc.api.ErrorListener;
import com.sun.xml.bind.api.SchemaOutputResolver;

import java.io.File;
import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.namespace.QName;

import com.sun.mirror.apt.Filer.Location;
import com.sun.tools.ws.processor.model.jaxb.JAXBType;
import com.sun.tools.ws.processor.model.jaxb.JAXBModel;

/**
 *
 * @author WS Development Team
 */
public class WSDLGenerator extends GeneratorBase20 implements Constants, ProcessorAction {
    private boolean doneGeneration;
    private Definitions definitions;
    private WSDLDocument wsdlDocument;
    private Map nsSchemaMap;
    private JAXWSOutputSchemaResolver resolver;
    private Properties options;

    public WSDLGenerator() {
        super();
        nsSchemaMap = new HashMap();        
        resolver = new JAXWSOutputSchemaResolver();
    }

    protected WSDLGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver) {
        super(model, config, properties);
        options = properties;
        nsSchemaMap = new HashMap();        
        resolver = new JAXWSOutputSchemaResolver();
    }

    public GeneratorBase20 getGenerator(
        Model model,
        Configuration config,
        Properties properties) {
        return new WSDLGenerator(model, config, properties, SOAPVersion.SOAP_11);
    }

    public GeneratorBase20 getGenerator(
        Model model,
        Configuration config,
        Properties properties,
        SOAPVersion ver) {
        return new WSDLGenerator(model, config, properties, ver);
    }

/*    public void perform(
        Model model,
        Configuration config,
        Properties properties) {
        this.perform(model, config, properties, SOAPVersion.SOAP_11);
    }
*/
    protected void doGeneration() {
        try {
            doGeneration(model);
        } catch (Exception e) {
            throw new GeneratorException(
                "generator.nestedGeneratorError",
                new LocalizableExceptionAdapter(e));
        }
    }

    private void doGeneration(Model model) throws Exception {

        String modelerName =
            (String) model.getProperty(ModelProperties.PROPERTY_MODELER_NAME);
        //TODO: checking modelername with the hardcoded WSDLModeler class name. Requres some better way to do it!
        if (modelerName != null
            && modelerName.equals(ModelProperties.WSDL_MODELER_NAME)) {
            //modelerName.equals("com.sun.xml.rpc.processor.modeler.wsdl.WSDLModeler")) {

            // do not generate a WSDL if the model was produced by the WSDL modeler
            // we should use the original WSDL document instead
            return;
        }

        File wsdlFile =
            new File(nonclassDestDir, model.getName().getLocalPart() + ".wsdl");
        log("WSDL File: "+ wsdlFile);
        WSDLDocument document = generateDocument(model);

        try {
            WSDLWriter writer = new WSDLWriter();
            OutputStream fos; // = new FileOutputStream(wsdlFile);

/*            if (env.getFiler() != null) {
                String relPath = computeRelativePath(destDir, nonclassDestDir);
                wsdlFile =
                    new File(relPath, model.getName().getLocalPart() + ".wsdl");
                fos = env.getFiler().createBinaryFile(Location.CLASS_TREE,
                                                   "",  wsdlFile);
//                fos = System.out;
            } else {*/
                fos = new FileOutputStream(wsdlFile);
//            }

            /* adding the file name and its type */
            GeneratedFileInfo fi = new GeneratedFileInfo();
            fi.setFile(wsdlFile);
            fi.setType(GeneratorConstants.FILE_TYPE_WSDL);
            env.addGeneratedFile(fi);

            writer.write(document, fos);
            fos.close();
        } catch (IOException e) {
            fail("generator.cant.write", wsdlFile.toString());
        }
    }

    protected static String computeRelativePath(File path1, File path2) {
        String relPath = "";
        if (path1.equals(path2)) {
            return ".";
        } else if (path1.getAbsolutePath().startsWith(path2.getAbsolutePath())) {
            String tmp = path1.getAbsolutePath();
            int idx = path2.getAbsolutePath().length();
            tmp = tmp.substring(idx);
            StringTokenizer tokenizer = new StringTokenizer(tmp, File.separator);
            while (tokenizer.hasMoreTokens()) {
                relPath += ".."+File.separator;
                tokenizer.nextToken();
            }
        } else if (path2.getAbsolutePath().startsWith(path1.getAbsolutePath())) {
            String tmp = path2.getAbsolutePath();
            int idx = path1.getAbsolutePath().length()+1;
            relPath = tmp.substring(idx);
        }
        return relPath;
    }

    private WSDLDocument generateDocument(Model model) throws Exception {
        WSDLDocument document = new WSDLDocument();
        Definitions definitions = new Definitions(document);
        definitions.setName(model.getName().getLocalPart());
        if (model.getTargetNamespaceURI() != null)
            definitions.setTargetNamespaceURI(model.getTargetNamespaceURI());
        document.setDefinitions(definitions);

        // TODO - most of the following methods visit the model to collect
        //        the information they need; perhaps we can reorganize
        //        them a little bit and avoid at least some of the visits

        generateTypes(model, document);
        generateMessages(model, definitions);
        generatePortTypes(model, definitions);
        generateBindings(model, definitions);
        generateServices(model, definitions);

        return document;
    }

    protected void generateTypes(Model model, WSDLDocument document)
        throws Exception {
        Types types = new Types();
        wsdlDocument = document;

        this.definitions = document.getDefinitions();
        definitions.setTypes(types);
        doneGeneration = false;
        visit(model);
        wsdlDocument = null;
        JAXBModel jaxbModel = model.getJAXBModel();
        if (jaxbModel != null) {
            jaxbModel.getJ2SJAXBModel().generateSchema(resolver, new JAXBErrorListener());
        }
    }

    protected String getSOAPEncodingNamespace(Port port) {
        if (port.getSOAPVersion().equals(SOAPVersion.SOAP_12.toString()))
            return SOAP12Constants.NS_SOAP_ENCODING;
        else
            return SOAPConstants.NS_SOAP_ENCODING;
    }

    protected String getSOAPTransportHttpURI(Port port) {
        if (port.getSOAPVersion().equals(SOAPVersion.SOAP_12.toString()))
            return SOAP12Constants.URI_SOAP_TRANSPORT_HTTP;
        else
            return SOAPConstants.URI_SOAP_TRANSPORT_HTTP;
    }

    private void generateMessages(Model model, Definitions definitions)
        throws Exception {
        for (Iterator services = model.getServices(); services.hasNext();) {
            Service service = (Service) services.next();

            for (Iterator ports = service.getPorts(); ports.hasNext();) {
                Port port = (Port) ports.next();

                PortType wsdlPortType = new PortType(definitions);
                wsdlPortType.setName(getWSDLPortTypeName(port));

                for (Iterator operations = port.getOperations();
                    operations.hasNext();
                    ) {
                    Operation operation = (Operation) operations.next();

                    String localOperationName =
                        operation.getName().getLocalPart();

                    // NOTE - this code assumes that all parameters go into the body
                    // TODO - fix it
                    Request request = operation.getRequest();
                    com.sun.tools.ws.wsdl.document.Message wsdlRequestMessage =
                        new com.sun.tools.ws.wsdl.document.Message(definitions);
                    wsdlRequestMessage.setName(
                        getWSDLInputMessageName(operation));

                    fillInMessageParts(
                        request,
                        wsdlRequestMessage,
                        true,
                        operation);
                    definitions.add(wsdlRequestMessage);

                    Response response = operation.getResponse();

                    // Check if it is a doc/lit unwrapped with holders
                    boolean doclit_bare_holders = false;
                    if (response != null && operation.getStyle() == SOAPStyle.DOCUMENT &&
                        response.getParametersList().size() == 1) {
                        for (Parameter param : response.getParametersList()) {
//System.out.println("param:" +param.getName()+" javaType: " + para)
/*                            if (param.getJavaParameter().isHolder()) {
                                doclit_wrapped_holders = true;
                                break;
                            }*/
                            doclit_bare_holders = param.getLinkedParameter() != null;
                            break;
                        }
                    }
                    if (response != null) {
                        com.sun.tools.ws.wsdl.document
                            .Message wsdlResponseMessage =
                            new com.sun.tools.ws.wsdl.document.Message(
                                definitions);
                        wsdlResponseMessage.setName(
                            getWSDLOutputMessageName(operation));
                        fillInMessageParts(
                            response,
                            wsdlResponseMessage,
                            doclit_bare_holders,
                            operation);
                        definitions.add(wsdlResponseMessage);
                    }

                    Set faultSet =
                        new TreeSet(new GeneratorUtil.FaultComparator());
                    faultSet.addAll(operation.getFaultsSet());
                    for (Iterator faults = faultSet.iterator();
                        faults.hasNext();
                        ) {
                        Fault fault = (Fault) faults.next();
                        com.sun.tools.ws.wsdl.document.Message wsdlFaultMessage =
                            new com.sun.tools.ws.wsdl.document.Message(
                                definitions);
                        wsdlFaultMessage.setName(
                            getWSDLFaultMessageName(fault));
                        MessagePart part = new MessagePart();
                        part.setName(fault.getBlock().getName().getLocalPart());
                        JavaException javaException = fault.getJavaException();
                        AbstractType type = fault.getBlock().getType();
                        if (type.isSOAPType()) {
                            part.setDescriptorKind(SchemaKinds.XSD_TYPE);
                            if (fault.getSubfaults() != null) {
                                QName ownerName =
                                    ((AbstractType) javaException.getOwner())
                                        .getName();
                                part.setDescriptor(ownerName);
                            } else {
                                part.setDescriptor(type.getName());
                            }
                        } else { //if (type.isLiteralType()) {
                            part.setDescriptorKind(SchemaKinds.XSD_ELEMENT);
                            part.setDescriptor(fault.getElementName());
                        }
                        wsdlFaultMessage.add(part);

                        try {
                            definitions.add(wsdlFaultMessage);
                        } catch (DuplicateEntityException e) {
                            // don't worry about it right now
                            // TODO - should we let this exception through?
                        }
                    }
                }
            }
        }
    }

    private void fillInMessageParts(
        Message message,
        com.sun.tools.ws.wsdl.document.Message wsdlMessage,
        boolean isRequest,
        Operation operation)
        throws Exception {
        SOAPStyle style = operation.getStyle();

        if (message == null) {
            // one-way operation
            return;
        }
        if (style == SOAPStyle.RPC) {
            for (Iterator parameters = message.getParameters();
                parameters.hasNext();
                ) {
                Block bodyBlock = message.getBodyBlocks().next();
                Parameter parameter = (Parameter) parameters.next();
                MessagePart part = new MessagePart();
                part.setName(parameter.getName());
                AbstractType type = parameter.getType();
                if (type.getName() == null) {
                    // a void return type results in a dummy type in the model
                    continue;
                }
                if (type.isSOAPType() || (
                    style == SOAPStyle.RPC && parameter.getBlock() == bodyBlock) ) {
                    part.setDescriptorKind(SchemaKinds.XSD_TYPE);
                    if (type instanceof RpcLitMember) {
                        RpcLitMember member = (RpcLitMember)type;
                        part.setDescriptor(member.getSchemaTypeName());
                    } else {
                        part.setDescriptor(type.getName());
                    }
                } else if (type.isLiteralType()) {
                    part.setDescriptorKind(SchemaKinds.XSD_ELEMENT);
                    part.setDescriptor(type.getName());
                }
                wsdlMessage.add(part);
            }
        } else {
            // body is literal
            Iterator iter = message.getBodyBlocks();
            if (iter.hasNext()) {
                Block bodyBlock = (Block) iter.next();
                MessagePart part = new MessagePart();
                String partName = isRequest
                        ? PART_NAME_LITERAL_REQUEST_WRAPPER
                        : PART_NAME_LITERAL_RESPONSE_WRAPPER;
                if (!operation.isWrapped()) {
                    partName = bodyBlock.getName().getLocalPart();
                }
                part.setName(partName);
                part.setDescriptorKind(SchemaKinds.XSD_ELEMENT);
                part.setDescriptor(bodyBlock.getName());
                wsdlMessage.add(part);
            }
            iter = message.getHeaderBlocks();
            while (iter.hasNext()) {
                Block headerBlock = (Block) iter.next();
                MessagePart part = new MessagePart();
                part.setName(headerBlock.getName().getLocalPart());
                part.setDescriptorKind(SchemaKinds.XSD_ELEMENT);
                part.setDescriptor(headerBlock.getName());
                wsdlMessage.add(part);
            }
        }
    }

    private void generatePortTypes(Model model, Definitions definitions)
        throws Exception {

        for (Iterator services = model.getServices(); services.hasNext();) {
            Service service = (Service) services.next();

            for (Iterator ports = service.getPorts(); ports.hasNext();) {
                Port port = (Port) ports.next();

                PortType wsdlPortType = new PortType(definitions);
                wsdlPortType.setName(getWSDLPortTypeName(port));
                generateJAXRPCBinding(wsdlPortType, port);

                for (Iterator operations = port.getOperations();
                    operations.hasNext();
                    ) {
                    Operation operation = (Operation) operations.next();

                    String localOperationName =
                        operation.getName().getLocalPart();

                    com.sun.tools.ws.wsdl.document.Operation wsdlOperation =
                        new com.sun.tools.ws.wsdl.document.Operation();
                    wsdlOperation.setName(localOperationName);
                    wsdlOperation.setStyle(OperationStyle.REQUEST_RESPONSE);

                    // fix for bug 4844538
                    if (operation.getStyle().equals(SOAPStyle.RPC)
                        && operation.getResponse() != null) {
                        // no paramOrder for one way
                        String paramOrder = "";
/*                        Iterator parameters =
                            operation.getRequest().getParameters();
                        Parameter parameter;
                        for (int i = 0; parameters.hasNext(); i++) {
                            if (i > 0)
                                paramOrder += " ";
                            parameter = (Parameter) parameters.next();
                            paramOrder += parameter.getName();
                        }*/
//                        JavaMethod javaMethod = ;
                        int i = 0;
                        for (JavaParameter parameter : operation.getJavaMethod().getParametersList()) {
                            if (i++ > 0)
                                paramOrder += " ";
                            paramOrder += parameter.getParameter().getName();
                        }
                        wsdlOperation.setParameterOrder(paramOrder);
                    } else if (operation.getResponse() != null) {
                        generateDocumentParameterOrder(operation, wsdlOperation);
                    }

                    Input input = new Input();
                    input.setMessage(
                        new QName(
                            model.getTargetNamespaceURI(),
                            getWSDLInputMessageName(operation)));
                    wsdlOperation.setInput(input);

                    if (getWSDLOutputMessageName(operation) != null) {
                        Output output = new Output();
                        output.setMessage(
                            new QName(
                                model.getTargetNamespaceURI(),
                                getWSDLOutputMessageName(operation)));
                        wsdlOperation.setOutput(output);
                    }

                    Set faultSet =
                        new TreeSet(new GeneratorUtil.FaultComparator());
                    faultSet.addAll(operation.getFaultsSet());
                    for (Iterator faults = faultSet.iterator();
                        faults.hasNext();
                        ) {
                        Fault fault = (Fault) faults.next();

                        com.sun.tools.ws.wsdl.document.Fault wsdlFault =
                            new com.sun.tools.ws.wsdl.document.Fault();
                        wsdlFault.setName(fault.getName());
                        wsdlFault.setMessage(
                            new QName(
                                model.getTargetNamespaceURI(),
                                getWSDLFaultMessageName(fault)));
                        wsdlOperation.addFault(wsdlFault);
                    }

                    wsdlPortType.add(wsdlOperation);
                }

                definitions.add(wsdlPortType);
            }
        }
    }

    protected boolean isHeaderParameter(Parameter parameter, Operation operation) {
        return isMessageHeaderParameter(parameter, operation.getRequest()) ||
               isMessageHeaderParameter(parameter, operation.getResponse());
    }

    protected boolean isMessageHeaderParameter(Parameter parameter, Message message) {
        if (message == null)
            return false;
        for (Block block : message.getHeaderBlockCollection())
            if (parameter.getBlock().equals(block))
                return true;

        return false;
    }

    protected boolean isMessageParameter(Parameter parameter, Message message) {
        boolean retval = message.getParametersList().contains(parameter);
        return retval;
    }

    private void generateBindings(Model model, Definitions definitions)
        throws Exception {
        for (Iterator services = model.getServices(); services.hasNext();) {
            Service service = (Service) services.next();

            for (Iterator ports = service.getPorts(); ports.hasNext();) {
                Port port = (Port) ports.next();

                // try to determine default style
                boolean isMixed = false;
                SOAPStyle defaultStyle = null;
                for (Iterator operations = port.getOperations();
                    operations.hasNext();
                    ) {
                    Operation operation = (Operation) operations.next();

                    if (operation.getStyle() == null) {
                        operation.setStyle(SOAPStyle.RPC);
                    }

                    if (defaultStyle == null) {
                        defaultStyle = operation.getStyle();
                    } else {
                        if (defaultStyle != operation.getStyle()) {
                            isMixed = true;
                        }
                    }
                }

                String localPortName = port.getName().getLocalPart();
                Binding wsdlBinding = new Binding(definitions);
                wsdlBinding.setName(getWSDLBindingName(port));
                wsdlBinding.setPortType(
                    new QName(
                        model.getTargetNamespaceURI(),
                        getWSDLPortTypeName(port)));
                SOAPBinding soapBinding = new SOAPBinding();
                if (defaultStyle != null && !isMixed) {
                    soapBinding.setStyle(defaultStyle);
                }
                soapBinding.setTransport(getSOAPTransportHttpURI(port));
                wsdlBinding.addExtension(soapBinding);

                for (Iterator operations = port.getOperations();
                    operations.hasNext();
                    ) {
                    Operation operation = (Operation) operations.next();

                    BindingOperation wsdlOperation = new BindingOperation();
                    wsdlOperation.setName(operation.getName().getLocalPart());
                    wsdlOperation.setStyle(OperationStyle.REQUEST_RESPONSE);
                    SOAPOperation soapOperation = new SOAPOperation();
                    soapOperation.setSOAPAction(operation.getSOAPAction());
                    if (!operation.getStyle().equals(defaultStyle)) {
                        soapOperation.setStyle(operation.getStyle());
                    }
                    wsdlOperation.addExtension(soapOperation);
                    Request request = operation.getRequest();
                    BindingInput input = new BindingInput();
                    SOAPBody soapBody = new SOAPBody();
                    soapBody.setUse(operation.getUse());
                    if (operation.getUse() == SOAPUse.ENCODED)
                        soapBody.setEncodingStyle(
                            getSOAPEncodingNamespace(port));
                    if (operation.getStyle() == SOAPStyle.RPC) {
                        soapBody.setNamespace(model.getTargetNamespaceURI());
                    }
                    input.addExtension(soapBody);
                    if (request.getHeaderBlockCount() > 0) {
                        String partName = "";
                        if (operation.getStyle() == SOAPStyle.DOCUMENT) {
                            if (operation.isWrapped())
                                soapBody.setParts(PART_NAME_LITERAL_RESPONSE_WRAPPER);
                            else {
                                Block block = request.getBodyBlocks().next();
                                partName += block.getName().getLocalPart();
                            }
                        } else {
                            int i = 0;
                            for (Parameter param : operation.getRequest().getParametersList()) {
                                if (!isMessageHeaderParameter(param, operation.getRequest())) {
                                    if (i++ > 0)
                                        partName += " ";
                                    partName += param.getName();
                                }
                            }
                        }
                        soapBody.setParts(partName);
                        for (Block headerBlock : request.getHeaderBlocksMap().values()) {
                            SOAPHeader soapHeader = new SOAPHeader();
                            soapHeader.setUse(operation.getUse());
                            soapHeader.setPart(headerBlock.getName().getLocalPart());
                            soapHeader.setMessage(new QName(
                                    model.getTargetNamespaceURI(),getWSDLInputMessageName(operation)));
                            if (operation.getUse() == SOAPUse.ENCODED)
                                soapHeader.setEncodingStyle(
                                    getSOAPEncodingNamespace(port));
    //                            if (operation.getStyle() == SOAPStyle.RPC) {
    //                                soapHeader.setNamespace(model.getTargetNamespaceURI());
    //                            }
                            input.addExtension(soapHeader);
                        }
                    }
                    wsdlOperation.setInput(input);

                    Response response = operation.getResponse();
                    if (response != null) {
                        BindingOutput output = new BindingOutput();
                        soapBody = new SOAPBody();
                        soapBody.setUse(operation.getUse());
                        if (operation.getUse() == SOAPUse.ENCODED)
                            soapBody.setEncodingStyle(
                                getSOAPEncodingNamespace(port));
                        if (operation.getStyle() == SOAPStyle.RPC) {
                            soapBody.setNamespace(
                                model.getTargetNamespaceURI());
                        }
                        output.addExtension(soapBody);

                        if (response.getHeaderBlockCount() > 0) {
                            String partName = "";
                            if (operation.getStyle() == SOAPStyle.DOCUMENT) {
                                if (operation.isWrapped())
                                    soapBody.setParts(PART_NAME_LITERAL_RESPONSE_WRAPPER);
                                else {
                                    Block block = response.getBodyBlocks().next();
                                    partName += block.getName().getLocalPart();
                                }
                            } else {
                                int i = 0;
                                for (Parameter param : operation.getResponse().getParametersList()) {
                                    if (!isMessageHeaderParameter(param, operation.getResponse())) {                                         if (i++ > 0)
                                            partName += " ";
                                        partName += param.getName();
                                    }
                                }
                            }
                            soapBody.setParts(partName);
                            for (Block headerBlock : response.getHeaderBlocksMap().values()) {
                                SOAPHeader soapHeader = new SOAPHeader();
                                soapHeader.setUse(operation.getUse());
                                soapHeader.setPart(headerBlock.getName().getLocalPart());
                                soapHeader.setMessage(new QName(
                                        model.getTargetNamespaceURI(),getWSDLOutputMessageName(operation)));
                                if (operation.getUse() == SOAPUse.ENCODED)
                                    soapHeader.setEncodingStyle(
                                        getSOAPEncodingNamespace(port));
//                                if (operation.getStyle() == SOAPStyle.RPC) {
//                                    soapHeader.setNamespace(model.getTargetNamespaceURI());
//                                }
                                output.addExtension(soapHeader);
                            }
                        }
                        wsdlOperation.setOutput(output);
                    }

                    Set faultSet =
                        new TreeSet(new GeneratorUtil.FaultComparator());
                    faultSet.addAll(operation.getFaultsSet());
                    for (Iterator faults = faultSet.iterator();
                        faults.hasNext();
                        ) {
                        Fault fault = (Fault) faults.next();
                        BindingFault bindingFault = new BindingFault();
                        bindingFault.setName(fault.getName());
                        SOAPFault soapFault = new SOAPFault();
                        soapFault.setName(fault.getName());
                        if (fault.getBlock().getType().isSOAPType()) {
                            soapFault.setUse(SOAPUse.ENCODED);
                            soapFault.setEncodingStyle(
                                getSOAPEncodingNamespace(port));
                            soapFault.setNamespace(
                                model.getTargetNamespaceURI());
                        } else {
                            soapFault.setUse(SOAPUse.LITERAL);
                        }
                        bindingFault.addExtension(soapFault);
                        wsdlOperation.addFault(bindingFault);
                    }

                    wsdlBinding.add(wsdlOperation);
                }

                definitions.add(wsdlBinding);
            }
        }
    }

    private void generateServices(Model model, Definitions definitions)
        throws Exception {

        for (Iterator services = model.getServices(); services.hasNext();) {
            Service service = (Service) services.next();
            com.sun.tools.ws.wsdl.document.Service wsdlService =
                new com.sun.tools.ws.wsdl.document.Service(definitions);
            wsdlService.setName(service.getName().getLocalPart());
            for (Iterator ports = service.getPorts(); ports.hasNext();) {
                Port port = (Port) ports.next();
                String localPortName = port.getName().getLocalPart();
                com.sun.tools.ws.wsdl.document.Port wsdlPort =
                    new com.sun.tools.ws.wsdl.document.Port(definitions);
                wsdlPort.setName(getWSDLPortName(port));
                wsdlPort.setBinding(
                    new QName(
                        model.getTargetNamespaceURI(),
                        getWSDLBindingName(port)));
                SOAPAddress soapAddress = new SOAPAddress();
                soapAddress.setLocation(
                    port.getAddress() == null
                        ? "REPLACE_WITH_ACTUAL_URL"
                        : port.getAddress());
                wsdlPort.addExtension(soapAddress);
                wsdlService.add(wsdlPort);
            }

            definitions.add(wsdlService);
        }
    }

    private String getWSDLBaseName(Port port) {
        return port.getName().getLocalPart();
    }

    private String getWSDLPortName(Port port) {
        QName value =
            (QName) port.getProperty(ModelProperties.PROPERTY_WSDL_PORT_NAME);
        if (value != null) {
            return value.getLocalPart();
        } else {
            return getWSDLBaseName(port) + "Port";
        }
    }

    private String getWSDLBindingName(Port port) {
        QName value =
            (QName) port.getProperty(
                ModelProperties.PROPERTY_WSDL_BINDING_NAME);
        if (value != null) {
            return value.getLocalPart();
        } else {
            return getWSDLBaseName(port) + "Binding";
        }
    }

    private String getWSDLPortTypeName(Port port) {
        QName value =
            (QName) port.getProperty(
                ModelProperties.PROPERTY_WSDL_PORT_TYPE_NAME);
        if (value != null) {
            return value.getLocalPart();
        } else {
            return port.getName().getLocalPart();
        }
    }

    private String getWSDLInputMessageName(Operation operation) {
        QName value =
            (QName) operation.getRequest().getProperty(
                ModelProperties.PROPERTY_WSDL_MESSAGE_NAME);
        if (value != null) {
            return value.getLocalPart();
        } else {
            return operation.getName().getLocalPart();
        }
    }

    private String getWSDLOutputMessageName(Operation operation) {
        if (operation.getResponse() == null) {
            // one way operation
            return null;
        }
        QName value =
            (QName) operation.getResponse().getProperty(
                ModelProperties.PROPERTY_WSDL_MESSAGE_NAME);
        if (value != null) {
            return value.getLocalPart();
        } else {
            return operation.getName().getLocalPart() + "Response";
        }
    }

    private String getWSDLFaultMessageName(Fault fault) {
        QName value =
            (QName) fault.getProperty(
                ModelProperties.PROPERTY_WSDL_MESSAGE_NAME);
        if (value != null) {
            return value.getLocalPart();
        } else {
            return fault.getName();
        }
    }
    
    protected void generateDocumentParameterOrder(Operation operation, com.sun.tools.ws.wsdl.document.Operation wsdlOperation) {
        String partName = "";
        String paramOrder = "";
        Set<String> partNames = new HashSet<String>();
        int i = 0;
//        log("operation: "+operation.getName());
        for (JavaParameter parameter : operation.getJavaMethod().getParametersList()) {
            Parameter param = parameter.getParameter();
            if (operation.isWrapped() && !isHeaderParameter(param, operation)) {
                if (isMessageParameter(param, operation.getRequest()))
                    partName = PART_NAME_LITERAL_REQUEST_WRAPPER;
                else
                    partName = PART_NAME_LITERAL_RESPONSE_WRAPPER;
            } else {
               partName = param.getBlock().getName().getLocalPart();
            }
//            log("partName: "+partName);
            if (!partNames.contains(partName)) {
                if (i++ > 0)
                    paramOrder += " ";
                paramOrder += partName;
//                log("paramOrder: "+paramOrder);
                partNames.add(partName);
            }
        }
        if (i>1) {
            log("setting paramOrder: "+paramOrder);
            wsdlOperation.setParameterOrder(paramOrder);
        }
    }

/*    public void visit(JAXBType type) throws Exception {
        if (doneGeneration)
            return;
        JAXBModel model = type.getJaxbModel() != null ? type.getJaxbModel().getRawJAXBModel() : null;
        if (model == null)
            return;
        Map<QName, TypeMirror> map = new HashMap<QName, TypeMirror>();
        model.generateSchema(map, this, new JAXBErrorListener());
        doneGeneration = true;
    }
*/

    public Result createOutputFile(String namespaceUri, String suggestedFileName) throws IOException {
        File schemaFile;

        SchemaElement importElement = new SchemaElement(SchemaConstants.QNAME_IMPORT);
        if (namespaceUri.length() > 0) {
            importElement.addAttribute(
                            com.sun.tools.ws.wsdl.parser.Constants.ATTR_NAMESPACE,
                            namespaceUri);
        } else {
            System.out.println("import namespaceURI: "+namespaceUri);
            warn("generator.schema.import.namespaces.should.not.be.empty");
        }
        importElement.addAttribute(
            com
                .sun
                .tools.ws
                .wsdl
                .parser
                .Constants
                .ATTR_SCHEMA_LOCATION,
                suggestedFileName);

        SchemaElement schemaContent;
        Schema schema =
            (Schema) nsSchemaMap.get(namespaceUri);

        if (schema == null) {
            schema = new Schema(wsdlDocument);
            schemaContent =
                new SchemaElement(SchemaConstants.QNAME_SCHEMA);
            schema.setContent(schemaContent);
            definitions.getTypes().addExtension(schema);
            nsSchemaMap.put(namespaceUri, schema);
        }
        schemaContent = schema.getContent();
        schemaContent.insertChildAtTop(importElement);

        StreamResult result;
        File outputFile;
/*        if (env.getFiler() != null) {
            String relPath = computeRelativePath(destDir, nonclassDestDir);
            schemaFile = new File(relPath, suggestedFileName);
            result = new StreamResult(env.getFiler().createBinaryFile(Location.CLASS_TREE,
                                                   "",  schemaFile));
        } else {*/
            schemaFile = new File(nonclassDestDir, suggestedFileName);
            result = new StreamResult(schemaFile);
//        }

        GeneratedFileInfo fi = new GeneratedFileInfo();
        fi.setFile(schemaFile);
        fi.setType(GeneratorConstants.FILE_TYPE_WSDL);
        env.addGeneratedFile(fi);


        return result;
    }


    protected void generateJAXRPCBinding(PortType wsdlPortType, Port port) {
//        System.out.println("generateJAXRPCBinding wrapped: "+port.isWrapped()+
//                           " isDoc: "+port.getStyle().equals(SOAPStyle.DOCUMENT));
/*        if (!port.isWrapped() && port.getStyle().equals(SOAPStyle.DOCUMENT)) {
            JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();
            jaxrpcBinding.setEnableWrapperStyle(port.isWrapped());
            wsdlPortType.addExtension(jaxrpcBinding);
        }
        */
    }
    

    protected static final String PART_NAME_LITERAL_REQUEST_WRAPPER =
        "parameters";
    protected static final String PART_NAME_LITERAL_RESPONSE_WRAPPER = "parameters";
    
    protected class JAXWSOutputSchemaResolver extends SchemaOutputResolver {
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return createOutputFile(namespaceUri, suggestedFileName);
        }
    }
}
