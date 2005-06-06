/**
 * $Id: WSDLGenerator.java,v 1.16 2005-06-06 22:01:17 vivekp Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer;


import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.SchemaOutputResolver;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.output.StreamSerializer;
import com.sun.xml.ws.model.CheckedException;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.model.soap.SOAPBinding;
import com.sun.xml.ws.model.soap.SOAPBlock;
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
import com.sun.xml.ws.wsdl.writer.document.soap.Header;
import com.sun.xml.ws.wsdl.writer.document.soap.SOAPFault;
import com.sun.xml.ws.encoding.soap.SOAPVersion;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import javax.xml.namespace.QName;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
    public static final String SOAP12_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String SOAP_PREFIX      = "soap";
    public static final String SOAP12_PREFIX      = "soap12";
    public static final String TNS_PREFIX       = "tns";
    public static final String BINDING          = "Binding";
    public static final String SOAP_HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    public static final String SOAP12_HTTP_TRANSPORT = "http://www.w3.org/2003/05/soap/bindings/HTTP/";
    public static final String DOCUMENT         = "document";
    public static final String RPC              = "rpc";
    public static final String LITERAL          = "literal";
//    public static final String PORT             = "Port";
    public static final String REPLACE_WITH_ACTUAL_URL = "REPLACE_WITH_ACTUAL_URL";
    private Set<QName> processedExceptions = new HashSet<QName>();
    private String bindingId;


    public WSDLGenerator(RuntimeModel model, WSDLOutputResolver wsdlResolver, String bindingId) {
        this.model = model;
        resolver = new JAXWSOutputSchemaResolver();
        this.wsdlResolver = wsdlResolver;
        this.bindingId = bindingId;
    }
    
    public void doGeneration() {
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
    
    private void generateDocument(OutputStream stream) {
        definitions = TXW.create(Definitions.class, new StreamSerializer(stream));
        definitions._namespace(WSDL_NAMESPACE, "");//WSDL_PREFIX);
        definitions._namespace(XSD_NAMESPACE, XSD_PREFIX);
        if(bindingId.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING))
            definitions._namespace(SOAP12_NAMESPACE, SOAP12_PREFIX);
        else
            definitions._namespace(SOAP11_NAMESPACE, SOAP_PREFIX);

//        definitions._namespace(WSDL_NAMESPACE, false);
        
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
        definitions.commit();
    }    
    
    
    
    protected void generateTypes() {
        types = definitions.types();
        if (model.getJAXBContext() != null) {
            try {
                model.getJAXBContext().generateSchema(resolver);
            } catch (IOException e) {
                // TODO locallize and wrap this
                e.printStackTrace();
                throw new WebServiceException(e.getMessage());
            }
        }
    }
    
    protected void generateMessages() {
        for (JavaMethod method : model.getJavaMethods()) {
            if (method.getBinding() instanceof SOAPBinding)
                generateSOAPMessages(method, (SOAPBinding)method.getBinding());
        }
    }
    
    protected void generateSOAPMessages(JavaMethod method, SOAPBinding binding) {
        boolean isDoclit = binding.isDocLit();
        Message message = definitions.message().name(method.getOperationName());
        com.sun.xml.ws.wsdl.writer.document.Part part;
        JAXBRIContext jaxbContext = model.getJAXBContext();
        for (Parameter param : method.getRequestParameters()) {
            if (isDoclit) {
                if (param.isWrapperStyle()) {
                    part = message.part().name(PARAMETERS);
                    part.element(param.getName());
                } else {
                    part = message.part().name(param.getName().getLocalPart());
                    part.element(param.getName());
                }
            } else {
                if (param.isWrapperStyle()) {
                    for (Parameter childParam : ((WrapperParameter)param).getWrapperChildren()) {
                        part = message.part().name(childParam.getName().getLocalPart());
                        part.type(jaxbContext.getTypeName(childParam.getTypeReference()));
                    }
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
            } else {
                if (param.isWrapperStyle()) {
                    for (Parameter childParam : ((WrapperParameter)param).getWrapperChildren()) {
                        part = message.part().name(childParam.getName().getLocalPart());
                        part.type(jaxbContext.getTypeName(childParam.getTypeReference()));
                    }
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
    
    protected void generatePortType() {
        
        PortType portType = definitions.portType().name(model.getPortQName().getLocalPart());
        for (JavaMethod method : model.getJavaMethods()) {
            Operation operation = portType.operation().name(method.getOperationName());
            generateParameterOrder(operation, method);
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
                FaultType paramType = operation.fault().name(tagName.getLocalPart()).message(messageName);
            }
        }
    }    
    
    protected boolean isWrapperStyle(JavaMethod method) {
        if (method.getRequestParameters().size() > 0) {
            Parameter param = method.getRequestParameters().iterator().next();
            return param.isWrapperStyle();
        }        
        return false;
    }
            
    protected boolean isRpcLit(JavaMethod method) {
        if (method.getBinding() instanceof SOAPBinding) {
            if (((SOAPBinding)method.getBinding()).getStyle().equals(Style.RPC))
                return true;
        }
        return false;
    }
    
    protected void generateParameterOrder(Operation operation, JavaMethod method) {
        if (method.getMEP() == MessageStruct.ONE_WAY_MEP)
            return;
        if (isRpcLit(method))
            generateRpcParameterOrder(operation, method);
        else
            generateDocumentParameterOrder(operation, method);
    }

    protected void generateRpcParameterOrder(Operation operation, JavaMethod method) {
        String partName = "";
        String paramOrder = "";
        Set<String> partNames = new HashSet<String>();
        List<Parameter> sortedParams = sortMethodParameters(method);
        int i = 0;
        for (Parameter parameter : sortedParams) {
            if (parameter.getIndex() >= 0) {
               partName = parameter.getName().getLocalPart();
                if (!partNames.contains(partName)) {
                    if (i++ > 0)
                        paramOrder += " ";
                    paramOrder += partName;
                    partNames.add(partName);
                }
            }
        }
        operation.parameterOrder(paramOrder);
    }
    
    
    protected void generateDocumentParameterOrder(Operation operation, JavaMethod method) {
        String partName = "";
        String paramOrder = "";
        Set<String> partNames = new HashSet<String>();
        List<Parameter> sortedParams = sortMethodParameters(method);
        boolean isWrapperStyle = isWrapperStyle(method);
        int i = 0;
        for (Parameter parameter : sortedParams) {
            if (parameter.getIndex() < 0)
                continue;
            if (isWrapperStyle && isBodyParameter(parameter)) {
                if (method.getRequestParameters().contains(parameter))
                    partName = PARAMETERS;
                else
                    partName = RESPONSE;
            } else {
               partName = parameter.getName().getLocalPart();
            }
            if (!partNames.contains(partName)) {
                if (i++ > 0)
                    paramOrder += " ";
                paramOrder += partName;
                partNames.add(partName);
            }
        }
        if (i>1) {
            operation.parameterOrder(paramOrder);
        }
    }
    
    protected List<Parameter> sortMethodParameters(JavaMethod method) {
        Set<Parameter> paramSet = new HashSet<Parameter>();
        List<Parameter> sortedParams = new ArrayList<Parameter>();
        if (isRpcLit(method)) {
            for (Parameter param : method.getRequestParameters()) {
                if (param instanceof WrapperParameter) {
                    paramSet.addAll(((WrapperParameter)param).getWrapperChildren());
                } else {
                    paramSet.add(param);
                }
            }
            for (Parameter param : method.getResponseParameters()) {
                if (param instanceof WrapperParameter) {
                    paramSet.addAll(((WrapperParameter)param).getWrapperChildren());
                } else {
                    paramSet.add(param);
                }
            }            
        } else  {
            paramSet.addAll(method.getRequestParameters());
            paramSet.addAll(method.getResponseParameters());            
        }
        Iterator<Parameter>params = paramSet.iterator();
        if (paramSet.size() == 0)
            return sortedParams;
        Parameter param = params.next();
        sortedParams.add(param);
        int pos;
        for (int i=1; i<paramSet.size();i++) {
            param = params.next();
            for (pos=0; pos<i; pos++) {
                if (param.getIndex() < sortedParams.get(pos).getIndex()) {
                    break;
                }
            }            
            sortedParams.add(pos, param);
        }
        return sortedParams;
    }
    
    protected boolean isBodyParameter(Parameter parameter) {
        SOAPBlock paramBinding = (SOAPBlock) parameter.getBinding();
        return paramBinding.isBody();
    }

    protected boolean isHeaderParameter(Parameter parameter) {
        SOAPBlock paramBinding = (SOAPBlock) parameter.getBinding();
        return paramBinding.isHeader();
    }
    
    protected boolean isAttachmentParameter(Parameter parameter) {
        SOAPBlock paramBinding = (SOAPBlock) parameter.getBinding();
        return paramBinding.isAttachment();
    }
    
    
    protected void generateBinding() {
        Binding binding = definitions.binding().name(model.getPortQName().getLocalPart()+BINDING);
        binding.type(model.getPortQName());
        boolean first = true;
        for (JavaMethod method : model.getJavaMethods()) {
            if (first) {
                if (method.getBinding() instanceof SOAPBinding) {
                    SOAPBinding sBinding = (SOAPBinding)method.getBinding();
                    SOAPVersion soapVersion = sBinding.getSOAPVersion();

                    if(soapVersion.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)){
                        com.sun.xml.ws.wsdl.writer.document.soap12.SOAPBinding soapBinding = binding.soap12Binding();
                        soapBinding.transport(SOAP12_HTTP_TRANSPORT);
                        if (sBinding.getStyle().equals(Style.DOCUMENT))
                            soapBinding.style(DOCUMENT);
                        else
                            soapBinding.style(RPC);
                    }else{
                        com.sun.xml.ws.wsdl.writer.document.soap.SOAPBinding soapBinding = binding.soapBinding();
                        soapBinding.transport(SOAP_HTTP_TRANSPORT);
                        if (sBinding.getStyle().equals(Style.DOCUMENT))
                            soapBinding.style(DOCUMENT);
                        else
                            soapBinding.style(RPC);
                    }
                }
                first = false;
            }
            if(bindingId.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING))
                generateSOAP12BindingOperation(method, binding);
            else
                generateBindingOperation(method, binding);
        }
    }    
    
    protected void generateBindingOperation(JavaMethod method, Binding binding) {
        BindingOperationType operation = binding.operation().name(method.getOperationName());
        String targetNamespace = model.getTargetNamespace();
        QName requestMessage = new QName(targetNamespace, method.getOperationName());
        QName responseMessage = new QName(targetNamespace, method.getOperationName()+RESPONSE);
        if (method.getBinding() instanceof SOAPBinding) {
            List<Parameter> bodyParams = new ArrayList<Parameter>();
            List<Parameter> headerParams = new ArrayList<Parameter>();
            splitParameters(bodyParams, headerParams, method.getRequestParameters());
            SOAPBinding soapBinding = (SOAPBinding)method.getBinding();
            operation.soapOperation().soapAction(soapBinding.getSOAPAction());

            // input
            TypedXmlWriter input = operation.input();
            BodyType body = input._element(Body.class);
            boolean isRpc = soapBinding.getStyle().equals(Style.RPC);
            if (soapBinding.getUse().equals(Use.LITERAL)) {
                body.use(LITERAL);
                if (headerParams.size() > 0) {
                    Parameter param = bodyParams.iterator().next();
                    if (isRpc) {
                        String parts = "";
                        int i=0;
                        for (Parameter parameter : ((WrapperParameter)param).getWrapperChildren()) {
                            if (i++>0)
                                parts += " ";
                            parts += parameter.getName().getLocalPart();
                        }
                        body.parts(parts);
                    } else if (param.isWrapperStyle()) {
                        body.parts(PARAMETERS);
                    } else {
                       body.parts(param.getName().getLocalPart());
                    }
                    generateSOAPHeaders(input, headerParams, requestMessage);
                }    
                if (isRpc) {
                    body.namespace(method.getRequestParameters().iterator().next().getName().getNamespaceURI());
                }                  
            } else {
                // TODO localize this
                throw new WebServiceException("encoded use is not supported");
            }
            // output
            bodyParams.clear();
            headerParams.clear();
            splitParameters(bodyParams, headerParams, method.getResponseParameters());
            TypedXmlWriter output = operation.output();
            body = output._element(Body.class);
            body.use(LITERAL);
            if (headerParams.size() > 0) {
                Parameter param = bodyParams.iterator().next();
                if (isRpc) {
                    String parts = "";
                    int i=0;
                    for (Parameter parameter : ((WrapperParameter)param).getWrapperChildren()) {
                        if (i++>0)
                            parts += " ";
                        parts += parameter.getName().getLocalPart();
                    }
                    body.parts(parts);
                } else if (param.isWrapperStyle()) {
                    body.parts(RESULT);
                } else {
                    body.parts(param.getName().getLocalPart());
                }
                generateSOAPHeaders(output, headerParams, responseMessage);
            }                
            if (isRpc) {
                body.namespace(method.getRequestParameters().iterator().next().getName().getNamespaceURI());
            }                  
            
            for (CheckedException exception : method.getCheckedExceptions()) {
                QName tagName = exception.getDetailType().tagName;
                Fault fault = operation.fault().name(tagName.getLocalPart());
                SOAPFault soapFault = fault._element(SOAPFault.class).name(tagName.getLocalPart());
                soapFault.use(LITERAL);
            }
        }
    }

    protected void generateSOAP12BindingOperation(JavaMethod method, Binding binding) {
        BindingOperationType operation = binding.operation().name(method.getOperationName());
        String targetNamespace = model.getTargetNamespace();
        QName requestMessage = new QName(targetNamespace, method.getOperationName());
        QName responseMessage = new QName(targetNamespace, method.getOperationName()+RESPONSE);
        if (method.getBinding() instanceof SOAPBinding) {
            List<Parameter> bodyParams = new ArrayList<Parameter>();
            List<Parameter> headerParams = new ArrayList<Parameter>();
            splitParameters(bodyParams, headerParams, method.getRequestParameters());
            SOAPBinding soapBinding = (SOAPBinding)method.getBinding();
            operation.soap12Operation().soapAction(soapBinding.getSOAPAction());

            // input
            TypedXmlWriter input = operation.input();

            com.sun.xml.ws.wsdl.writer.document.soap12.BodyType body = input._element(com.sun.xml.ws.wsdl.writer.document.soap12.Body.class);
            boolean isRpc = soapBinding.getStyle().equals(Style.RPC);
            if (soapBinding.getUse().equals(Use.LITERAL)) {
                body.use(LITERAL);
                if (headerParams.size() > 0) {
                    Parameter param = bodyParams.iterator().next();
                    if (isRpc) {
                        String parts = "";
                        int i=0;
                        for (Parameter parameter : ((WrapperParameter)param).getWrapperChildren()) {
                            if (i++>0)
                                parts += " ";
                            parts += parameter.getName().getLocalPart();
                        }
                        body.parts(parts);
                    } else if (param.isWrapperStyle()) {
                        body.parts(PARAMETERS);
                    } else {
                       body.parts(param.getName().getLocalPart());
                    }
                    generateSOAP12Headers(input, headerParams, requestMessage);
                }
                if (isRpc) {
                    body.namespace(method.getRequestParameters().iterator().next().getName().getNamespaceURI());
                }
            } else {
                // TODO localize this
                throw new WebServiceException("encoded use is not supported");
            }
            // output
            bodyParams.clear();
            headerParams.clear();
            splitParameters(bodyParams, headerParams, method.getResponseParameters());
            TypedXmlWriter output = operation.output();
            body = output._element(com.sun.xml.ws.wsdl.writer.document.soap12.Body.class);
            body.use(LITERAL);
            if (headerParams.size() > 0) {
                Parameter param = bodyParams.iterator().next();
                if (isRpc) {
                    String parts = "";
                    int i=0;
                    for (Parameter parameter : ((WrapperParameter)param).getWrapperChildren()) {
                        if (i++>0)
                            parts += " ";
                        parts += parameter.getName().getLocalPart();
                    }
                    body.parts(parts);
                } else if (param.isWrapperStyle()) {
                    body.parts(RESULT);
                } else {
                    body.parts(param.getName().getLocalPart());
                }
                generateSOAP12Headers(output, headerParams, responseMessage);
            }
            if (isRpc) {
                body.namespace(method.getRequestParameters().iterator().next().getName().getNamespaceURI());
            }

            for (CheckedException exception : method.getCheckedExceptions()) {
                QName tagName = exception.getDetailType().tagName;
                Fault fault = operation.fault().name(tagName.getLocalPart());
                com.sun.xml.ws.wsdl.writer.document.soap12.SOAPFault soapFault = fault._element(com.sun.xml.ws.wsdl.writer.document.soap12.SOAPFault.class).name(tagName.getLocalPart());
                soapFault.use(LITERAL);
            }
        }
    }

    protected void splitParameters(List<Parameter> bodyParams, List<Parameter>headerParams, List<Parameter>params) {
        for (Parameter parameter : params) {
            if (isBodyParameter(parameter)) {
                bodyParams.add(parameter);
            } else {
                headerParams.add(parameter);
            }
        }        
    }

    protected void generateSOAPHeaders(TypedXmlWriter writer, List<Parameter> parameters, QName message) {
        
        for (Parameter headerParam : parameters) {
            Header header = writer._element(Header.class);
            header.message(message);
            header.part(headerParam.getName().getLocalPart());
            header.use(LITERAL);
        }        
    }

    protected void generateSOAP12Headers(TypedXmlWriter writer, List<Parameter> parameters, QName message) {

        for (Parameter headerParam : parameters) {
            com.sun.xml.ws.wsdl.writer.document.soap12.Header header = writer._element(com.sun.xml.ws.wsdl.writer.document.soap12.Header.class);
            header.message(message);
            header.part(headerParam.getName().getLocalPart());
            header.use(LITERAL);
        }
    }

    protected void generateService() {
        Service service = definitions.service().name(model.getServiceQName().getLocalPart());
        QName portQName = model.getPortQName();
//        Port port = service.port().name(portQName.getLocalPart()+PORT);
        Port port = service.port().name(portQName.getLocalPart());
        port.binding(new QName(portQName.getNamespaceURI(), portQName.getLocalPart()+BINDING));
        if (model.getJavaMethods().iterator().next().getBinding() instanceof SOAPBinding) {
            if(bindingId.equals(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)){
                com.sun.xml.ws.wsdl.writer.document.soap12.SOAPAddress address = port._element(com.sun.xml.ws.wsdl.writer.document.soap12.SOAPAddress.class);
                address.location(REPLACE_WITH_ACTUAL_URL);
            }else{
                SOAPAddress address = port._element(SOAPAddress.class);
                address.location(REPLACE_WITH_ACTUAL_URL);
            }
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
        Result result;
        if (namespaceUri.equals("")) {
            return null;
        }
        com.sun.xml.ws.wsdl.writer.document.xsd.Import _import = types.schema()._import().namespace(namespaceUri);

        result = wsdlResolver.getSchemaOutput(namespaceUri, suggestedFileName);
        _import.schemaLocation(result.getSystemId());
        return result;
    }
    
    
    protected class JAXWSOutputSchemaResolver extends SchemaOutputResolver {
        public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
            return createOutputFile(namespaceUri, suggestedFileName);
        }
    }    
}
