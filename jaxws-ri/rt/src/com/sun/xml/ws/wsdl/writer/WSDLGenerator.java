/**
 * $Id: WSDLGenerator.java,v 1.1 2005-06-01 00:12:42 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer;


import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.bind.api.SchemaOutputResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.StreamSerializer;
import com.sun.xml.ws.model.CheckedException;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.xml.ws.wsdl.writer.document.Definitions;
import com.sun.xml.ws.wsdl.writer.document.FaultType;
import com.sun.xml.ws.wsdl.writer.document.Message;
import com.sun.xml.ws.wsdl.writer.document.Operation;
import com.sun.xml.ws.wsdl.writer.document.ParamType;
import com.sun.xml.ws.wsdl.writer.document.PortType;
import com.sun.xml.ws.wsdl.writer.document.Types;
import javax.xml.namespace.QName;

import java.util.Set;
import java.util.HashSet;


/**
 * Interface defining WSDL-related constants.
 *
 * @author Doug Kohlert
 */
public class WSDLGenerator {
    private JAXWSOutputSchemaResolver resolver;
    private RuntimeModel model;
    private Types types;
    public static final String RESPONSE         = "Response";
    public static final String PARAMETERS       = "parameters";
    public static final String RESULT           = "result";
    private Set<QName> processedExceptions = new HashSet<QName>();

    public WSDLGenerator() {
        resolver = new JAXWSOutputSchemaResolver();        
    }

    public WSDLGenerator(RuntimeModel model) {
        this.model = model;
        resolver = new JAXWSOutputSchemaResolver();
        
        try {
            Definitions def = doGeneration();
            def.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Definitions doGeneration() throws Exception {
        File file = new File("test.wsdl");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        System.out.println("test file output: "+file.getAbsolutePath());
        return generateDocument(model, fileOutputStream);
    }
    
    private Definitions generateDocument(RuntimeModel model, OutputStream stream) throws Exception {
        Definitions definitions = TXW.create(Definitions.class, new StreamSerializer(stream));
        definitions._namespace("http://schemas.xmlsoap.org/wsdl/","wsdl");
        definitions._namespace("http://www.w3.org/2001/XMLSchema","xsd");
        definitions._namespace("http://schemas.xmlsoap.org/wsdl/soap/","soap");
        
        if (model.getServiceQName() != null)
            definitions.name(model.getServiceQName().getLocalPart());
        if (model.getTargetNamespace() != null) {
            definitions.targetNamespace(model.getTargetNamespace());
            definitions._namespace(model.getTargetNamespace(),"tns");
        }

        generateTypes(model, definitions);
        generateMessages(model, definitions);
        generatePortTypes(model, definitions);
        generateBindings(model, definitions);
//        generateServices(model, definitions);

        return definitions;
    }    
    
    
    
    protected void generateTypes(RuntimeModel model, Definitions definitions)
        throws Exception {
        types = definitions.types();
        if (model.getJAXBContext() != null) {
            model.getJAXBContext().generateSchema(resolver);
        }
//        JAXBModel jaxbModel = model.getJAXBModel();
//        if (jaxbModel != null) {
//            jaxbModel.getJ2SJAXBModel().generateSchema(resolver, new JAXBErrorListener());
//        }
    }
    
    protected void generateMessages(RuntimeModel model, Definitions definitions)
        throws Exception {
        for (JavaMethod method : model.getJavaMethods()) {
            if (method.getBinding() instanceof SOAPBinding)
                generateSOAPMessages(method, (SOAPBinding)method.getBinding(), definitions);
        }
    }
    
    protected void generateSOAPMessages(JavaMethod method, SOAPBinding binding, Definitions definitions) {
        boolean isDoclit = binding.isDocLit();
        Message message = definitions.message().name(method.getOperationName());
        com.sun.xml.ws.wsdl.writer.document.Part part;
        for (Parameter param : method.getRequestParameters()) {
            if (isDoclit) {
                if (param.isWrapperStyle()) {
                    part = message.part().name(PARAMETERS);
                    part.element(param.getName());
                } else {
                    part = message.part().name(param.getName().getLocalPart());
                    part.element(param.getName());
                }
            }
        }                

        message = definitions.message().name(method.getOperationName()+RESPONSE);
        for (Parameter param : method.getResponseParameters()) {
            if (isDoclit) {
                if (param.isWrapperStyle()) {
                    part = message.part().name(RESULT);
                    part.element(param.getName());
                } else {
                    part = message.part().name(param.getName().getLocalPart());
                    part.element(param.getName());                    
                }
            }
        }                
        for (CheckedException exception : method.getCheckedExceptions()) {
            // TODO fill this in
            QName tagName = exception.getDetailType().tagName;
            if (processedExceptions.contains(tagName))
                continue;
            message = definitions.message().name(tagName.getLocalPart());
            part = message.part().name(tagName.getLocalPart());
            part.element(tagName);
            processedExceptions.add(tagName);
        }
    }
    
    protected void generatePortTypes(RuntimeModel model, Definitions definitions)
        throws Exception {
        
        PortType portType = definitions.portType().name(model.getPortQName().getLocalPart());
        for (JavaMethod method : model.getJavaMethods()) {
            Operation operation = portType.operation().name(method.getOperationName());
            switch (method.getMEP()) {
                case MessageStruct.REQUEST_RESPONSE_MEP: 
                    // input message
                    generateInputMessage(model, operation, method);
                    // output message
                    generateOutputMessage(model, operation, method);
                    break;
                case MessageStruct.ONE_WAY_MEP:
                    generateInputMessage(model, operation, method);
                    break;   
            }
            // faults
            for (CheckedException exception : method.getCheckedExceptions()) {
                QName tagName = exception.getDetailType().tagName;
                QName messageName = new QName(model.getTargetNamespace(), tagName.getLocalPart());
                FaultType paramType = operation.fault().message(messageName);
            }
        }                
    }    
    
    protected void generateBindings(RuntimeModel model, Definitions definitions)
        throws Exception {
    }    
    
    protected void generateInputMessage(RuntimeModel model, Operation operation, JavaMethod method) {
        ParamType paramType = operation.input();//.name();
        paramType.message(new QName(model.getTargetNamespace(),method.getOperationName()));        
    }

    protected void generateOutputMessage(RuntimeModel model, Operation operation, JavaMethod method) {
        ParamType paramType = operation.output();//.name();
        paramType.message(new QName(model.getTargetNamespace(),method.getOperationName()+RESPONSE));        
    }
    
    public Result createOutputFile(String namespaceUri, String suggestedFileName) throws IOException {
        File schemaFile;
        com.sun.xml.ws.wsdl.writer.document.xsd.Import _import = types.schema()._import().namespace(namespaceUri);
        _import.schemaLocation(suggestedFileName);
/*
        SchemaElement importElement = new SchemaElement(SchemaConstants.QNAME_IMPORT);
        if (namespaceUri.length() > 0) {
            importElement.addAttribute(
                            com.sun.tools.ws.wsdl.parser.Constants.ATTR_NAMESPACE,
                            namespaceUri);
        } else {
//            System.out.println("import namespaceURI: "+namespaceUri);
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
*/
        StreamResult result;
        File outputFile;
        schemaFile = new File(suggestedFileName);
        result = new StreamResult(schemaFile);

        return result;
    }
    
    
    protected class JAXWSOutputSchemaResolver extends SchemaOutputResolver {
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return createOutputFile(namespaceUri, suggestedFileName);
        }
    }    
}
