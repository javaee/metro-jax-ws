/**
 * $Id: WSDLGenerator.java,v 1.5 2005-06-01 22:47:14 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer;


import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.bind.api.SchemaOutputResolver;

import java.io.File;
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
import com.sun.xml.ws.model.soap.Style;
import com.sun.xml.ws.model.soap.Use;
import com.sun.xml.ws.wsdl.writer.document.Binding;
import com.sun.xml.ws.wsdl.writer.document.BindingOperationType;
import com.sun.xml.ws.wsdl.writer.document.Definitions;
import com.sun.xml.ws.wsdl.writer.document.Fault;
import com.sun.xml.ws.wsdl.writer.document.FaultType;
import com.sun.xml.ws.wsdl.writer.document.Message;
import com.sun.xml.ws.wsdl.writer.document.Operation;
import com.sun.xml.ws.wsdl.writer.document.ParamType;
import com.sun.xml.ws.wsdl.writer.document.Port;
import com.sun.xml.ws.wsdl.writer.document.PortType;
import com.sun.xml.ws.wsdl.writer.document.Service;
import com.sun.xml.ws.wsdl.writer.document.Types;
import com.sun.xml.ws.wsdl.writer.document.soap.SOAPAddress;
import com.sun.xml.ws.wsdl.writer.document.soap.Body;
import com.sun.xml.ws.wsdl.writer.document.soap.BodyType;
import com.sun.xml.ws.wsdl.writer.document.soap.SOAPFault;
import javax.xml.namespace.QName;

import java.util.Set;
import java.util.HashSet;
import javax.xml.ws.WebServiceException;


/**
 * Interface defining WSDL-related constants.
 *
 * @author Doug Kohlert
 */
public class WSDLGenerator {
    private JAXWSOutputSchemaResolver resolver;
    private WSDLOutputResolver wsdlResolver = null;
    private RuntimeModel model;
    private Definitions definitions;
    private Types types;
    public static final String DOT_WSDL         = ".wsdl";
    public static final String RESPONSE         = "Response";
    public static final String PARAMETERS       = "parameters";
    public static final String RESULT           = "result";
    public static final String WSDL_NAMESPACE   = "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_PREFIX      = "wsdl";
    public static final String XSD_NAMESPACE    = "http://www.w3.org/2001/XMLSchema";
    public static final String XSD_PREFIX       = "xsd";
    public static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String SOAP_PREFIX      = "soap";
    public static final String TNS_PREFIX       = "tns";
    public static final String BINDING          = "Binding";
    public static final String SOAP_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    public static final String DOCUMENT         = "document";
    public static final String RPC              = "rpc";
    public static final String LITERAL          = "literal";
    public static final String PORT             = "Port";
    public static final String REPLACE_WITH_ACTUAL_URL = "REPLACE_WITH_ACTUAL_URL";
    private Set<QName> processedExceptions = new HashSet<QName>();


    public WSDLGenerator(RuntimeModel model, WSDLOutputResolver wsdlResolver) {
        this.model = model;
        resolver = new JAXWSOutputSchemaResolver();
        this.wsdlResolver = wsdlResolver;
    }
    
    public void doGeneration() throws Exception {
//        File file = new File("test.wsdl");
//        FileOutputStream fileOutputStream = new FileOutputStream(file);
        OutputStream outputStream = null;
        Result result = wsdlResolver.getWSDLOutput(model.getServiceQName().getLocalPart()+DOT_WSDL);
        if (result instanceof StreamResult) {
            outputStream = ((StreamResult)result).getOutputStream();           
        } else {
            // TODO throw an exception
            throw new WebServiceException("unsupported result");
        }
        generateDocument(outputStream);
    }
    
    private void generateDocument(OutputStream stream) throws Exception {
        definitions = TXW.create(Definitions.class, new StreamSerializer(stream));
        definitions._namespace(WSDL_NAMESPACE, WSDL_PREFIX);
        definitions._namespace(XSD_NAMESPACE, XSD_PREFIX);
        definitions._namespace(SOAP11_NAMESPACE, SOAP_PREFIX);
        
        definitions.name(model.getServiceQName().getLocalPart());
        if (model.getTargetNamespace() != null) {
            definitions.targetNamespace(model.getTargetNamespace());
            definitions._namespace(model.getTargetNamespace(), TNS_PREFIX);
        }

        generateTypes();
        generateMessages();
        generatePortType();
        generateBinding();
        generateService();
    }    
    
    
    
    protected void generateTypes()
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
    
    protected void generateMessages()
        throws Exception {
        for (JavaMethod method : model.getJavaMethods()) {
            if (method.getBinding() instanceof SOAPBinding)
                generateSOAPMessages(method, (SOAPBinding)method.getBinding());
        }
    }
    
    protected void generateSOAPMessages(JavaMethod method, SOAPBinding binding) {
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
            QName tagName = exception.getDetailType().tagName;
            if (processedExceptions.contains(tagName))
                continue;
            message = definitions.message().name(tagName.getLocalPart());
            part = message.part().name(tagName.getLocalPart());
            part.element(tagName);
            processedExceptions.add(tagName);
        }
    }
    
    protected void generatePortType() throws Exception {
        
        PortType portType = definitions.portType().name(model.getPortQName().getLocalPart());
        for (JavaMethod method : model.getJavaMethods()) {
            Operation operation = portType.operation().name(method.getOperationName());

            switch (method.getMEP()) {
                case MessageStruct.REQUEST_RESPONSE_MEP: 
                    // input message
                    generateInputMessage(operation, method);
                    // output message
                    generateOutputMessage(operation, method);
                    break;
                case MessageStruct.ONE_WAY_MEP:
                    generateInputMessage(operation, method);
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
    
    protected void generateBinding() throws Exception {
        Binding binding = definitions.binding().name(model.getPortQName().getLocalPart()+BINDING);
        binding.type(model.getPortQName());
        boolean first = true;
        for (JavaMethod method : model.getJavaMethods()) {
            if (first) {
                if (method.getBinding() instanceof SOAPBinding) {
                    SOAPBinding sBinding = (SOAPBinding)method.getBinding();
                    com.sun.xml.ws.wsdl.writer.document.soap.SOAPBinding soapBinding = binding.soapBinding();
                    soapBinding.transport(SOAP_HTTP_TRANSPORT);
                    if (sBinding.getStyle().equals(Style.DOCUMENT)) 
                        soapBinding.style(DOCUMENT);
                    else
                        soapBinding.style(RPC);
                }
                first = false;
            }
            generateBindingOperation(method, binding);
        }
    }    
    
    protected void generateBindingOperation(JavaMethod method, Binding binding) {
        BindingOperationType operation = binding.operation().name(method.getOperationName());
        if (method.getBinding() instanceof SOAPBinding) {
            SOAPBinding soapBinding = (SOAPBinding)method.getBinding();
            operation.soapOperation().soapAction(soapBinding.getSOAPAction());
            // input
            BodyType body = operation.input()._element(Body.class);
            if (soapBinding.getUse().equals(Use.LITERAL)) {
                body.use(LITERAL);
                if (soapBinding.getStyle().equals(Style.RPC)) {
                    
                }                
            } else {
                // TODO throw an error, we don't do encoded
            }
            // output
            body = operation.output()._element(Body.class);
            body.use(LITERAL);
            if (soapBinding.getStyle().equals(Style.RPC)) {
                   
            }                
            for (CheckedException exception : method.getCheckedExceptions()) {
                QName tagName = exception.getDetailType().tagName;
                Fault fault = operation.fault().name(tagName.getLocalPart());
                SOAPFault soapFault = fault._element(SOAPFault.class).name(tagName.getLocalPart());
                soapFault.use(LITERAL);
            }
        }
    }
    
    protected void generateService() throws Exception {
        Service service = definitions.service().name(model.getServiceQName().getLocalPart());
        QName portQName = model.getPortQName();
        Port port = service.port().name(portQName.getLocalPart()+PORT);
        port.binding(new QName(portQName.getNamespaceURI(), portQName.getLocalPart()+BINDING));
        if (model.getJavaMethods().iterator().next().getBinding() instanceof SOAPBinding) {
            SOAPAddress address = port._element(SOAPAddress.class);
            address.location(REPLACE_WITH_ACTUAL_URL);
        }
    }
    
    protected void generateInputMessage(Operation operation, JavaMethod method) {
        ParamType paramType = operation.input();//.name();
        paramType.message(new QName(model.getTargetNamespace(), method.getOperationName()));        
    }

    protected void generateOutputMessage(Operation operation, JavaMethod method) {
        ParamType paramType = operation.output();//.name();
        paramType.message(new QName(model.getTargetNamespace(), method.getOperationName()+RESPONSE));        
    }
    
    public Result createOutputFile(String namespaceUri, String suggestedFileName) throws IOException {
//        File schemaFile;
        com.sun.xml.ws.wsdl.writer.document.xsd.Import _import = types.schema()._import().namespace(namespaceUri);
//        _import.schemaLocation(suggestedFileName);

        Result result = wsdlResolver.getSchemaOutput(namespaceUri, suggestedFileName);
        _import.schemaLocation(result.getSystemId());
//        schemaFile = new File(suggestedFileName);
//        result = new StreamResult(schemaFile);
        return result;
    }
    
    
    protected class JAXWSOutputSchemaResolver extends SchemaOutputResolver {
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return createOutputFile(namespaceUri, suggestedFileName);
        }
    }    
}
