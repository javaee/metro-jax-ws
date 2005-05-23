/**
 * $Id: WebServiceModeler.java,v 1.1 2005-05-23 23:23:51 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;


import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.VoidType;
import com.sun.tools.ws.processor.config.HandlerChainInfo;
import com.sun.tools.ws.processor.config.HandlerInfo;
import com.sun.tools.ws.processor.generator.GeneratorConstants;
import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Parameter;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Request;
import com.sun.tools.ws.processor.model.Response;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaException;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.model.jaxb.JAXBElementMember;
import com.sun.tools.ws.processor.model.jaxb.JAXBModel;
import com.sun.tools.ws.processor.model.jaxb.JAXBProperty;
import com.sun.tools.ws.processor.model.jaxb.JAXBType;
import com.sun.tools.ws.processor.model.jaxb.RpcLitMember;
import com.sun.tools.ws.processor.model.jaxb.RpcLitStructure;
import com.sun.tools.ws.processor.modeler.ModelerUtils;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.tools.ws.util.xml.NullEntityResolver;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.wsdl.framework.ParseException;
import com.sun.tools.xjc.api.Reference;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.jws.HandlerChain;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.InitParam;
import javax.jws.soap.SOAPMessageHandler;
import javax.jws.soap.SOAPMessageHandlers;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.ParameterIndex;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
    	


/**
 *
 * @author  dkohlert
 */
public class WebServiceModeler extends  WebServiceVisitor { 

    protected Set<QName> reqElems;
    protected Map<QName, String> resElems;
    protected Set<QName> operationNames;
    
    
    public WebServiceModeler(ModelBuilder builder, AnnotationProcessorContext context) {
        super(builder, context);
    }  
    
    protected boolean shouldProcessWebService(WebService webService, InterfaceDeclaration intf) { 
        if (webService == null)
            return false;
        if (endpointReferencesInterface)
            builder.onError("webserviceap.endpointinterface.has.no.webservice.annotation", 
                    new Object[] {intf.getQualifiedName()});
        if (isLegalSEI(intf))
            return true;
        return false;
    }        

    protected boolean shouldProcessWebService(WebService webService, ClassDeclaration classDecl) {   
//        System.out.println("shouldprocessWS modeler gen class");
        if (webService == null)
            return false;
        return isLegalImplementation(classDecl); // && hasLegalSEI(classDecl);
    }        
    
    protected void processWebService(WebService webService, TypeDeclaration d) {
        reqElems = new HashSet<QName>();
        resElems = new HashMap<QName, String>();
        operationNames = new HashSet<QName>();
        port = new Port(new QName(wsdlNamespace, getXMLName(portName)));
/*        hChain = d.getAnnotation(HandlerChain.class);
        if (hChain != null) {
            soapHandlers = d.getAnnotation(SOAPMessageHandlers.class);
            if (soapHandlers != null) {
                builder.onError("webserviceap.cannot.combine.handlerchain.soapmessagehandlers");
            }
            URL url = builder.findResource(hChain.file());
//            System.out.println("looking for handlerChain: "+hChain.file()+ " result: "+url);
            if (url == null) {
                String tmp = d.getPackage().toString();
                tmp = tmp.replace('.', '/');
                tmp += "/"+hChain.file();
                url = builder.findResource(tmp);
//                System.out.println("looking for handlerChain: "+tmp+ " result: "+url);
            }
            if (url == null)
                builder.onError("webserviceap.failed.to.find.handlerchain.file",
                               new Object[] {d.getQualifiedName(), hChain.file()});
            try {
                soapHandlers = parseHandlerChain(new InputSource(url.openStream()), 
                        hChain.name(), hChain.file());           
//                System.out.println("soapHandlers: "+soapHandlers);
            } catch (IOException e) {
                builder.onError("webserviceap.failed.to.parse.handlerchain.file",
                               new Object[] {d.getQualifiedName(), hChain.file()});
            }
        } else {
            soapHandlers = d.getAnnotation(SOAPMessageHandlers.class);            
        }
        if (soapHandlers != null) {
            HandlerChainInfo handlerChainInfo = new HandlerChainInfo();
            HandlerInfo handlerInfo;
            for (SOAPMessageHandler handler : soapHandlers.value()) {
//                System.out.println("handler: "+handler);
                handlerInfo = new HandlerInfo();
                handlerInfo.setHandlerClassName(handler.className());
                if (handler.roles() != null) {
                    for (int i=0; i < handler.roles().length; i++)
                        handlerChainInfo.addRole(handler.roles()[i]);
                }
                if (handler.headers() != null) {
                    //System.out.println("headers.length: "+handler.headers().length);
                    for (int i=0; i < handler.headers().length; i++) {
                        String localname = handler.headers()[i];
//                        System.out.println("localNmae: "+localname);
                        handlerInfo.addHeaderName(QName.valueOf(handler.headers()[i]));
                    }
                }
                InitParam[] initParams = handler.initParams();
                if (initParams != null) {
                    Map properties = new HashMap();
                    for (int i=0; i < initParams.length; i++)
                        properties.put(initParams[i].name(), initParams[i].value());
                    handlerInfo.setProperties(properties);                      
                }
                handlerChainInfo.add(handlerInfo);
            }
            // now deal with the configured handlers
            port.setClientHandlerChainInfo(handlerChainInfo);
            port.setServerHandlerChainInfo(handlerChainInfo);
        }
        */
        JavaInterface javaInterface;
        if (endpointInterfaceName != null && seiContext.getImplementsSEI()) //endpointImplementsInterface)
            javaInterface = new JavaInterface(
                endpointInterfaceName, serviceImplName);
        else 
            javaInterface = new JavaInterface(serviceImplName, serviceImplName);
        if (serviceImplName != null) {
            port.setServiceImplName(serviceImplName);
            javaInterface.setImpl(packageName+DOT+portName);
        } 

        port.setJavaInterface(javaInterface);

        builder.setPortProperties(port);        
        port.setStyle(soapStyle);
        port.setWrapped(wrapped);
        String interfaceName;
        for (InterfaceType interfaceType : d.getSuperinterfaces()) {
            interfaceName = interfaceType.toString();
            if (!interfaceName.equals(javaInterface.getName()))
                javaInterface.addInterface(interfaceName);
        }

        builder.createModel(d, new QName(null, getXMLName(serviceName)), wsdlNamespace, 
            this.getClass().getName());

        Service service;
        // take care of inner classes
        service =
            new Service(
                new QName(wsdlNamespace, serviceName),
                new JavaInterface(packageName+DOT+serviceName, d.getQualifiedName()));
        builder.setService(service);             

        builder.setPort(port);
    }

    protected void postProcessWebService(WebService webService, TypeDeclaration d) { 
        super.postProcessWebService(webService, d);
        
    }    
    
    private Element findChain(Element root, String chainName) {
        NodeList chains = root.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, HANDLER_CHAIN);
        Element targetChain = null;
        if (chains.getLength() > 0) {    
            for (int i=0; targetChain == null && i < chains.getLength(); i++) {
                Element chain = (Element)chains.item(i);
                NodeList names = chain.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, HANDLER_CHAIN_NAME);            
                if (names.item(0).getFirstChild().getNodeValue().equals(chainName)) {
                    targetChain = chain;
                }
            }
        }        
        return targetChain;
    }
    
    private SOAPMessageHandlers parseHandlerChain(InputSource source, String chainName, String file) {
        try {         
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            docBuilder.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException e)
                    throws SAXParseException {
                    throw e;
                }

                public void fatalError(SAXParseException e)
                    throws SAXParseException {
                    throw e;
                }

                public void warning(SAXParseException err)
                    throws SAXParseException {
                    // do nothing
                }
            });
            
            docBuilder.setEntityResolver(new NullEntityResolver());
            Document dom = docBuilder.parse(source);
            Element root = dom.getDocumentElement();
            if (!root.getLocalName().equals("handler-config")) {
                builder.onError("webserviceap.invalid.handlerchain.file.nohandler-config",
                        new Object[]{file});
            }
            Element targetChain = findChain(root, chainName);
            SOAPMessageHandlers messageHandlers = null;
            if (targetChain == null) {
                builder.onError("webserviceap.could.not.find.handlerchain",
                   new Object[]{chainName, file});
            }
            else {
                ArrayList<SOAPMessageHandler> handlers = new ArrayList<SOAPMessageHandler>();                
                messageHandlers = parseMessageHandlers(targetChain, file);
            }
            return messageHandlers;
        } catch (ParserConfigurationException e) {
            throw new ParseException(
                "parsing.parserConfigException",
                new LocalizableExceptionAdapter(e));
        } catch (FactoryConfigurationError e) {
            throw new ParseException(
                "parsing.factoryConfigException",
                new LocalizableExceptionAdapter(e));
        } catch(SAXException e){
            throw new ParseException(
                    "parsing.saxException",
                    new LocalizableExceptionAdapter(e));
        } catch(IOException e){
            throw new ParseException(
                    "parsing.saxException",
                    new LocalizableExceptionAdapter(e));
        }
    }
    
    private SOAPMessageHandlers parseMessageHandlers(Element chain, String chainFile) {
        MySOAPMessageHandlers soapMessageHandlers = new MySOAPMessageHandlers();
        NodeList handlerList = chain.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, HANDLER); 
        for (int i=0; i<handlerList.getLength(); i++) {
            soapMessageHandlers.addHandler(parseHandler((Element)handlerList.item(i), chainFile));
        }
        return soapMessageHandlers;
    }
    
    private SOAPMessageHandler parseHandler(Element element, String handlerFile) {
        MySOAPMessageHandler handler = new MySOAPMessageHandler();
        NodeList nodes = element.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, HANDLER_NAME);            
        if (nodes.getLength() == 1) {
            handler.setName(nodes.item(0).getFirstChild().getNodeValue());
        }
        nodes =  element.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, HANDLER_CLASS);            
        if (nodes.getLength() == 1) {
            handler.setClassName(nodes.item(0).getFirstChild().getNodeValue());
        } else {
            builder.onError("webserviceap.handlerclass.notspecified",
                             new Object[] {handlerFile});
        }
        nodes =  element.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, INIT_PARAM);     
        for (int i=0; i<nodes.getLength(); i++) {
            parseInitParam(handler, (Element)nodes.item(i), handlerFile);
        }
        nodes =  element.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, SOAP_ROLE);
        for (int i=0; i<nodes.getLength(); i++) {
            handler.addRole(nodes.item(i).getFirstChild().getNodeValue());
        }
        nodes =  element.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, SOAP_HEADER);
        for (int i=0; i<nodes.getLength(); i++) {
            String nodeValue = nodes.item(i).getFirstChild().getNodeValue();
            String nsPrefix = "";
            String nsURI = null;
            int idx = nodeValue.indexOf(":");
            if (idx > 0) {
                nsPrefix = nodeValue.substring(0, idx);
                nodeValue = nodeValue.substring(idx+1);
                nsURI = nodes.item(i).getAttributes().getNamedItem("xmlns:"+nsPrefix).getNodeValue();
            }
            if (nsURI != null)              
                handler.addHeader("{"+nsURI+"}/"+nodeValue);
            else
                handler.addHeader(nodeValue);
                
        }
        return handler;
    }
    
    private void parseInitParam(MySOAPMessageHandler handler, Element element, String handlerFile) {
        NodeList keys = element.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, PARAM_NAME);    
        NodeList values = element.getElementsByTagNameNS(WEBSERVICE_NAMESPACE, PARAM_VALUE);    
        if (keys.getLength() == 1 && values.getLength() == 1)
            handler.addInitParam(keys.item(0).getFirstChild().getNodeValue(),
                                values.item(0).getFirstChild().getNodeValue());
        else
            builder.onError("webserviceap.init_param.format.error");
    }
    
    private JAXBType getJAXBTypeForQName(TypeDeclaration decl, JAXBModel jaxbModel, List<MemberInfo> members){
        String javaName = decl.getQualifiedName();
        JavaType javaType = new JavaSimpleType(javaName, null);
        Reference ref = seiContext.getReference(decl);
//        QName qname = jaxbModel.getJ2SJAXBModel().getXmlTypeName(new Reference(decl, builder.getAPEnv()));
        QName qname = jaxbModel.getJ2SJAXBModel().getXmlTypeName(ref);
        if (qname == null)
            System.out.println("could not find jaxb qname for: "+javaName);
        JAXBType type =  new JAXBType(qname, javaType, null, jaxbModel);
        if (members != null) {
            List<JAXBProperty> props = new ArrayList<JAXBProperty>();
            for (MemberInfo member : members) {
                JAXBProperty prop = createJAXBProperty(member);
                props.add(prop);
            }
            type.setWrapperChildren(props);            
        }
        return type; 
    }        

    private Parameter getParameter(String name, List<Parameter> params) {
        for (Parameter param : params) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return null;
    }

    protected boolean shouldProcessMethod(MethodDeclaration method, WebMethod webMethod) {
        return !hasWebMethods || webMethod != null;
    }
    
    protected void processMethod(MethodDeclaration method, WebMethod webMethod) {
        com.sun.xml.ws.SOAPBinding soapBinding = method.getAnnotation(com.sun.xml.ws.SOAPBinding.class);
        boolean newBinding = false;
        if (soapBinding != null) {
            newBinding = pushSOAPBinding(new com.sun.xml.ws.SOAPBinding.MySOAPBinding(soapBinding), typeDecl);
        } 
        try {
            if (soapStyle.equals(SOAPStyle.DOCUMENT)) {
                if (wrapped)
                    processDocumentWrappedMethod(method, webMethod);
                else
                    processDocumentBareMethod(method, webMethod);
            } else {
                processRpcMethod(method, webMethod);
            }
        } finally {
            if (newBinding) {
                popSOAPBinding();
            }
        }
    }
    
    private Operation createOperation(QName operationName, MethodDeclaration method) {
        if (operationNames.contains(operationName)) {
            builder.onError("webserviceap.operation.name.not.unique",
                             new Object[] {typeDecl.getQualifiedName(), method.toString(), operationName.toString()});                                
        }
        operationNames.add(operationName);
        Operation operation = new Operation(operationName);
        operation.setUse(soapUse);
        operation.setStyle(soapStyle);
        operation.setWrapped(wrapped);
        return operation;
    }
    
    protected void processDocumentWrappedMethod(MethodDeclaration method, WebMethod webMethod) {
        String methodName = method.getSimpleName();
        String operationName = builder.getOperationName(methodName);
        String action = "";
        String operationNamespace = wsdlNamespace;
        operationName = webMethod != null && webMethod.operationName() != null &&
                        webMethod.operationName().length() > 0 ?
                        webMethod.operationName() : methodName;
        action = webMethod != null && webMethod.action() != null &&
                        webMethod.action().length() > 0 ?
                        webMethod.action() : action;

        String portName = ClassNameInfo.getName(port.getJavaInterface().getName());
        builder.log("creating operation: " + operationName);
        Operation operation = createOperation(new QName(operationName), method);

        operation.setSOAPAction(action);
        JavaMethod javaMethod = new JavaMethod(methodName);
        javaMethod.setDeclaringClass(getQualifiedName(method.getDeclaringType()));
        operation.setJavaMethod(javaMethod);
        String packageName = method.getDeclaringType().getPackage().getQualifiedName();
        if (packageName.length() > 0) {
            packageName = packageName + DOT;
        }
        operation.setStyle(soapStyle);
        operation.setUse(soapUse);

        boolean isOneway = method.getAnnotation(Oneway.class) != null;

        WrapperInfo reqWrapperInfo = seiContext.getReqOperationWrapper(method);
        String reqWrapperName = reqWrapperInfo.getWrapperName();
        TypeDeclaration reqWrapperDecl = builder.getTypeDeclaration(reqWrapperName);        
        JAXBType reqType = getJAXBTypeForQName(reqWrapperDecl, seiContext.getJAXBModel(), 
                                               reqWrapperInfo.getMembers());
        
        TypeDeclaration resWrapperDecl = null;
        JAXBType resType = null;
        if (!isOneway) {
            WrapperInfo resWrapperInfo = seiContext.getResOperationWrapper(method);
            String resWrapperName = resWrapperInfo.getWrapperName();
            resWrapperDecl = builder.getTypeDeclaration(resWrapperName);
            resType = getJAXBTypeForQName(resWrapperDecl, seiContext.getJAXBModel(),
                                           resWrapperInfo.getMembers());
        }

        // create response
        JAXBElementMember member = null;
        JavaParameter javaParameter = null;
        Block responseBlock = null;
        Response response = null;
        boolean hasHolders = false;
        for (ParameterDeclaration param : method.getParameters()) {
            hasHolders = param.getType() instanceof DeclaredType &&
                builder.getHolderValueType(param.getType())
                    != null;
            if (hasHolders)
                break;
        }
        TypeMirror returnType = method.getReturnType();
        WebResult webResult = method.getAnnotation(WebResult.class);
        String responseName = RETURN;
        if (webResult != null) {
            responseName = webResult.name().length() > 0 ? webResult.name() : responseName;
        }  
        
        if (isOneway) {
            if (!(returnType instanceof VoidType))
                builder.onError("webserviceap.oneway.operation.cannot.have.return.type",
                                new Object[] {typeDecl.getQualifiedName(), methodName});
            if (hasHolders)
                builder.onError("webserviceap.oneway.operation.cannot.have.holders",
                                new Object[] {typeDecl.getQualifiedName(), methodName});
            if (method.getThrownTypes().size() > 0) {
                TypeDeclaration runtimeException = builder.getTypeDeclaration(RUNTIME_EXCEPTION_CLASSNAME);
                for (ReferenceType thrown :   method.getThrownTypes()) {
                    if (!thrown.toString().equals(REMOTE_EXCEPTION_CLASSNAME) &&
                        !thrown.toString().equals(RUNTIME_EXCEPTION_CLASSNAME) &&
                        !TypeModeler.isSubclass(((ClassType)thrown).getDeclaration(), runtimeException )) {
                        builder.onError("webserviceap.oneway.operation.cannot.declare.exceptions",
                                      new Object[] {typeDecl.getQualifiedName(), methodName, thrown.toString()});                        
                    }
                }                
            }
        }
        List<Parameter> resParams = null;
        if (!isOneway || !(returnType instanceof VoidType) || hasHolders) {
            response = new Response();
            responseBlock =
                new Block(
                    new QName(
                        wsdlNamespace,
                        Names.getResponseName(operationName)));
            resParams = ModelerUtils.createUnwrappedParameters(resType, responseBlock);
            response.addBodyBlock(responseBlock);
            if (!(returnType instanceof VoidType)) {
                Parameter resultParam = getParameter(responseName, resParams);
                if (resultParam == null) {
                    ParameterIndex paramIndex;
                    for (FieldDeclaration field : resWrapperDecl.getFields()) {
                        paramIndex = field.getAnnotation(ParameterIndex.class);
                        if (paramIndex.value() == -1) {
                            String fieldName = field.getSimpleName();
                            resultParam = getParameter(fieldName, resParams);
                            break;
                        }
                    }
                }                
                resultParam.setEmbedded(true);
                resultParam.setBlock(responseBlock);
                javaParameter =
                    new JavaParameter(null, resultParam.getType().getJavaType(),
                        resultParam,
                        false);
                javaMethod.setReturnType(resultParam.getType().getJavaType());
                resultParam.setJavaParameter(javaParameter);
                response.addParameter(resultParam);           
                operation.setProperty(WSDLModelerBase.WSDL_RESULT_PARAMETER, resultParam.getName());                   
            }
            operation.setResponse(response);
        } 
        
        //  create request
        Request request = new Request();
        // build up body block
        Parameter parameter;
        Block block = new Block(new QName(wsdlNamespace, getXMLName(operationName)));
        Block headerBlock = null;
        List<Parameter> params = ModelerUtils.createUnwrappedParameters(reqType, block);
        TypeMirror holderType;
        String paramName;
        String paramNamespace;
        WebParam.Mode mode = WebParam.Mode.IN;
        boolean isHeader = false;
        boolean isInParam = false;
        int pos = 0;
        for (ParameterDeclaration param : method.getParameters()) {
            mode = null;
            isInParam = true;
            isHeader = false;
            TypeMirror paramType = param.getType();
            WebParam webParam = param.getAnnotation(WebParam.class);                
            paramNamespace = wsdlNamespace;
            if (webParam != null) {
                paramName = webParam.name().length() > 0 ? 
                            webParam.name() : param.getSimpleName();
                mode = webParam.mode(); 
                isHeader = webParam.header();
                paramNamespace = webParam.targetNamespace().length() > 0 ? webParam.targetNamespace() : paramNamespace;
            } else {
                paramName = param.getSimpleName();
            }
            holderType = builder.getHolderValueType(param.getType());
            if (holderType != null)
                paramType = holderType;
            if (isHeader) {
                if (webParam.name().length() == 0)
                    builder.onError("webserviceap.header.parameters.must.have.webparam.name",
                                new Object[] {typeDecl.getQualifiedName(), method.toString(), param.toString()});
                
                headerBlock = new Block(new QName(paramNamespace, getXMLName(paramName)));
                parameter = new Parameter(getXMLName(paramName));
                parameter.setBlock(headerBlock);
                parameter.setEmbedded(false);
                if (!(mode != null && mode.equals(WebParam.Mode.OUT)))
                    request.addHeaderBlock(headerBlock);                
                QName paramQName = new QName(paramNamespace, paramName);
                JAXBType jaxbType = new JAXBType(paramQName, 
                                              new JavaSimpleType(paramType.toString(), null),
                                              null,
                                              seiContext.getJAXBModel());
                headerBlock.setType(jaxbType);
                parameter.setType(jaxbType);  
                operation.setWrapped(false);
            } else {
                parameter = getParameter(paramName, params);
                if (parameter == null) {
                    ParameterIndex paramIndex;
                    for (FieldDeclaration field : reqWrapperDecl.getFields()) {
                        paramIndex = field.getAnnotation(ParameterIndex.class);
                        if (paramIndex.value() == pos) {
                            parameter = getParameter(field.getSimpleName(), params);
                            paramName = parameter.getName();
                            break;
                        }
                    }
                }
            }
            if (parameter != null)
                parameter.setParameterOrderPosition(pos);            
            if (holderType != null) {
                if (mode != null && mode.equals(WebParam.Mode.IN)) {
                    builder.onError("webserviceap.holder.parameters.must.not.be.in.only", 
                                new Object[] {typeDecl.getQualifiedName(), method.toString(), pos});
                }
                Parameter responseParam;
                if (!isHeader) {
                    responseParam = getParameter(paramName, resParams);                
                    if (responseParam == null) {
                        ParameterIndex paramIndex;
                        for (FieldDeclaration field : resWrapperDecl.getFields()) {
                            paramIndex = field.getAnnotation(ParameterIndex.class);
                            if (paramIndex.value() == pos) {
                                for (Parameter par : params)
                                responseParam = getParameter(field.getSimpleName(), resParams);
                                paramName = responseParam.getName();
                                break;
                            }
                        }
                    }
                } else {
                    Block resHeaderBlock = new Block(new QName(paramNamespace, getXMLName(paramName)));                    
                    QName typeName = new QName(paramNamespace, paramName);
                    responseParam = new Parameter(getXMLName(paramName));                
                    responseParam.setBlock(resHeaderBlock);
                    responseParam.setEmbedded(false);
                    JAXBType resJaxbType = new JAXBType(typeName, 
                                                      new JavaSimpleType(paramType.toString(), null),
                                                      null,
                                                      seiContext.getJAXBModel());

                    responseParam.setType(resJaxbType);                                                                      
                    resHeaderBlock.setType(resJaxbType);
                    response.addHeaderBlock(resHeaderBlock);
                    if (mode == null || mode.equals(WebParam.Mode.INOUT)) {
                        parameter.setLinkedParameter(responseParam);
                        responseParam.setLinkedParameter(parameter);
                    }                    
                }
                responseParam.setParameterOrderPosition(pos);
                javaParameter =
                    new JavaParameter(
                        paramName,
                        responseParam.getType().getJavaType(),
                        responseParam,
                        true);
                javaParameter.setHolderName(param.getType().toString());
                responseParam.setJavaParameter(javaParameter);
                if (mode == null || mode.equals(WebParam.Mode.INOUT)) {
                    parameter.setLinkedParameter(responseParam);
                    responseParam.setLinkedParameter(parameter);
                }
                response.addParameter(responseParam);   
                isInParam = !(mode != null && mode.equals(WebParam.Mode.OUT));
            } 
            if (isInParam) {
                javaParameter =
                    new JavaParameter(
                        paramName,
                        parameter.getType().getJavaType(),
                        parameter,
                        holderType != null);
                if (holderType != null)
                    javaParameter.setHolderName(param.getType().toString());
                parameter.setJavaParameter(javaParameter);
                request.addParameter(parameter);
            }
            javaMethod.addParameter(javaParameter);
            pos++;            
        }

        request.addBodyBlock(block);
        operation.setRequest(request);

        processExceptions(method, operation);

        port.addOperation(operation);
        port.getJavaInterface().addMethod(javaMethod);
    }
    
    protected void processDocumentBareMethod(MethodDeclaration method, WebMethod webMethod) {
        String methodName = method.getSimpleName();
        String operationName = builder.getOperationName(methodName);
        String action = "";
        operationName = webMethod != null && webMethod.operationName().length() > 0 ?
                        webMethod.operationName() : methodName;
        action = webMethod != null && webMethod.action().length() > 0 ?
                        webMethod.action() : action;
        String portName = ClassNameInfo.getName(port.getJavaInterface().getName());
        builder.log("creating operation: " + operationName);
        Operation operation = createOperation(new QName(operationName), method);
        operation.setWrapped(false);
        operation.setSOAPAction(action);
        JavaMethod javaMethod = new JavaMethod(methodName);
        javaMethod.setDeclaringClass(getQualifiedName(method.getDeclaringType()));
        operation.setJavaMethod(javaMethod);
        String packageName = method.getDeclaringType().getPackage().getQualifiedName();
       if (packageName.length() > 0) {
            packageName = packageName + DOT;
        }
        operation.setStyle(soapStyle);
        operation.setUse(soapUse);

        boolean isOneway = method.getAnnotation(Oneway.class) != null;
        // create response
        JAXBElementMember member = null;
        JavaParameter javaParameter = null;
        Block responseBlock = null;
        Response response = null;
        boolean hasHolders = false;
        for (ParameterDeclaration param : method.getParameters()) {
            hasHolders = param.getType() instanceof DeclaredType &&
                builder.getHolderValueType(param.getType())
                    != null;
            if (hasHolders)
                break;
        }
        TypeMirror returnType = method.getReturnType();
        WebResult webResult = method.getAnnotation(WebResult.class);
        String responseName = builder.getResponseName(method.getSimpleName());
        String responseNamespace = wsdlNamespace;
        if (webResult != null) {
            responseName = webResult.name().length() > 0 ? webResult.name() : responseName;
            responseNamespace = webResult.targetNamespace().length() > 0 ? webResult.targetNamespace() : responseNamespace;
        }  

        if (isOneway) {
            if (!(returnType instanceof VoidType))
                builder.onError("webserviceap.oneway.operation.cannot.have.return.type",
                                new Object[] {typeDecl.getQualifiedName(), methodName});
            if (hasHolders)
                builder.onError("webserviceap.oneway.operation.cannot.have.holders",
                                new Object[] {typeDecl.getQualifiedName(), methodName});
            if (method.getThrownTypes().size() > 0) {
                TypeDeclaration runtimeException = builder.getTypeDeclaration(RUNTIME_EXCEPTION_CLASSNAME);
                for (ReferenceType thrown :   method.getThrownTypes()) {
                    if (!thrown.toString().equals(REMOTE_EXCEPTION_CLASSNAME) &&
                        !thrown.toString().equals(RUNTIME_EXCEPTION_CLASSNAME) &&
                        !TypeModeler.isSubclass(((ClassType)thrown).getDeclaration(), runtimeException )) {
                        builder.onError("webserviceap.oneway.operation.cannot.declare.exceptions",
                                      new Object[] {typeDecl.getQualifiedName(), methodName, thrown.toString()});                        
                    }
                }                
            }
        }
        
        int inParams = 0;
        int outParams = 0;        
        if (!isOneway || !(returnType instanceof VoidType) || hasHolders) {
            response = new Response();
            responseBlock =
                new Block(
                    new QName(
                        responseNamespace,
                        responseName));
            response.addBodyBlock(responseBlock);
            if (!(returnType instanceof VoidType)) {
                outParams++;
                QName typeName = new QName(responseNamespace, responseName);
                responseBlock.setName(typeName);
                if (resElems.keySet().contains(typeName) &&
                    !resElems.get(typeName).equals(returnType.toString())) {
                    builder.onError("webserviceap.document.literal.bare.method.return.not.unique",
                                     new Object[] {typeDecl.getQualifiedName(), method.toString(), typeName.toString(),
                                                   returnType.toString()});
                }
                resElems.put(typeName, returnType.toString());
                JAXBType jaxbType = new JAXBType(typeName, 
                                                new JavaSimpleType(returnType.toString(), null),
                                                null,
                                                seiContext.getJAXBModel());
                // TODO is this right?
                responseBlock.setType(jaxbType);                                
                Parameter resultParam = new Parameter(responseName);
                resultParam.setType(jaxbType);
                resultParam.setEmbedded(false);
                resultParam.setBlock(responseBlock);
                resultParam.setParameterOrderPosition(-1);
                javaParameter =
                    new JavaParameter(null, resultParam.getType().getJavaType(),
                        resultParam,
                        false);
                javaMethod.setReturnType(resultParam.getType().getJavaType());
                resultParam.setJavaParameter(javaParameter);
                response.addParameter(resultParam);           
                operation.setProperty(WSDLModelerBase.WSDL_RESULT_PARAMETER, resultParam.getName());                   
            }  else {
                javaMethod.setReturnType(new JavaSimpleType(returnType.toString(), null));
            }
            operation.setResponse(response);
        } else {
            javaMethod.setReturnType(new JavaSimpleType(returnType.toString(), null));
        }
        
        //  create request
        Request request = new Request();
        // build up body block
        Parameter parameter;
        Block block = new Block(new QName(wsdlNamespace, getXMLName(operationName)));
        Block headerBlock = null;
        TypeMirror holderType;
        String paramName;
        String paramNamespace;
        WebParam.Mode mode = WebParam.Mode.IN;                     
        boolean isHeader = false;
        boolean isInParam = false;
        int pos = 0;
        for (ParameterDeclaration param : method.getParameters()) {
            mode = null;
            isInParam = true;
            isHeader = false;            
            WebParam webParam = param.getAnnotation(WebParam.class);                
            paramName = method.getSimpleName();
            paramNamespace = wsdlNamespace;            
            if (webParam != null) {
                paramName = webParam.name().length() > 0 ? 
                            webParam.name() : method.getSimpleName();
                mode = webParam.mode(); 
                isHeader = webParam.header();
                paramNamespace = webParam.targetNamespace().length() > 0 ? webParam.targetNamespace() : paramNamespace;
                
            }
            TypeMirror paramType = param.getType();
            holderType = builder.getHolderValueType(param.getType());
            if (holderType != null)
                paramType = holderType;
            QName paramQName = new QName(paramNamespace, paramName);
            JAXBType jaxbType = new JAXBType(paramQName, 
                                              new JavaSimpleType(paramType.toString(), null),
                                              null,
                                              seiContext.getJAXBModel());
            parameter = new Parameter(getXMLName(paramName));
            parameter.setType(jaxbType);                
            parameter.setParameterOrderPosition(pos);
            if (isHeader) {
                if (webParam.name().length() == 0)
                    builder.onError("webserviceap.header.parameters.must.have.webparam.name",
                                new Object[] {typeDecl.getQualifiedName(), method.toString(), param.toString()});
                headerBlock = new Block(new QName(paramNamespace, getXMLName(paramName)));
                parameter.setBlock(headerBlock);
                parameter.setEmbedded(false);
                if (!(mode != null && mode.equals(WebParam.Mode.OUT)))
                    request.addHeaderBlock(headerBlock);
                headerBlock.setType(jaxbType);
            } else if (holderType == null || !mode.equals(WebParam.Mode.OUT)) {                
                if (webParam != null && webParam.name().length() > 0)
                    block.setName(paramQName);
                
                if (reqElems.contains(paramQName)) {
                    builder.onError("webserviceap.document.literal.bare.method.not.unique",
                                     new Object[] {typeDecl.getQualifiedName(), method.toString(), paramQName.toString()});                    
                }
                reqElems.add(paramQName);
                parameter.setBlock(block);
                inParams++;
            }           
            if (holderType != null) {
                Parameter responseParam = new Parameter(getXMLName(paramName)); 
                responseParam.setEmbedded(false);
                QName typeName = parameter.getType().getName();   
                JAXBType resJaxbType = new JAXBType(typeName, 
                                              new JavaSimpleType(paramType.toString(), null),
                                              null,
                                              seiContext.getJAXBModel());

                responseParam.setType(resJaxbType);
                javaParameter =
                        new JavaParameter(
                            paramName,
                            responseParam.getType().getJavaType(),
                            responseParam,
                            true);
                javaParameter.setHolderName(param.getType().toString());
                responseParam.setJavaParameter(javaParameter);
                responseParam.setParameterOrderPosition(pos);
                if (!isHeader) {
                    responseParam.setBlock(responseBlock);
                    responseBlock.setName(typeName);
                    responseBlock.setType(responseParam.getType());
                    outParams++;
                } else {
                    Block resHeaderBlock = new Block(typeName);                    
                    resHeaderBlock.setType(resJaxbType);
                    response.addHeaderBlock(resHeaderBlock);
                    responseParam.setBlock(resHeaderBlock);
                }                 
                if (mode == null || mode.equals(WebParam.Mode.INOUT)) {
                    parameter.setLinkedParameter(responseParam);
                    responseParam.setLinkedParameter(parameter);
                }
                response.addParameter(responseParam);   
                isInParam = !(mode != null && mode.equals(WebParam.Mode.OUT));                
            }
            
            if (isInParam) {
                javaParameter =
                    new JavaParameter(
                        paramName,
                        parameter.getType().getJavaType(),
                        parameter,
                        holderType != null);
                if (holderType != null)
                    javaParameter.setHolderName(param.getType().toString());
                parameter.setJavaParameter(javaParameter);
                request.addParameter(parameter);
            }
            if (!isHeader) {
                // TODO is this correct?
                block.setType(parameter.getType());
            }
            javaMethod.addParameter(javaParameter);
            pos++;
        }
        if (inParams == 0) {
            QName typeName = new QName(wsdlNamespace, method.getSimpleName());
            JAXBType jaxbType = new JAXBType(typeName, 
                                              new JavaSimpleType("void", null),
                                              null,
                                              seiContext.getJAXBModel());            
            block.setType(jaxbType);
        }
        if (outParams > 1) {
            builder.onError("webserviceap.document.literal.bare.cannot.have.more.than.one.out", 
                    new Object[] {typeDecl.getQualifiedName(), method.toString()});
        }        
        if (inParams > 1) {
            builder.onError("webserviceap.document.literal.bare.must.have.only.one.in.parameter", 
                            new Object[] {typeDecl.getQualifiedName(), method.toString(), inParams});
        }
        if (inParams == 0 && outParams == 0) {
            builder.onError("webserviceap.document.literal.bare.must.have.one.in.or.out", 
                            new Object[] {typeDecl.getQualifiedName(), method.toString(), inParams});
        }
        request.addBodyBlock(block);
        operation.setRequest(request);

        processExceptions(method, operation);

        port.addOperation(operation);
        port.getJavaInterface().addMethod(javaMethod);
    }

    
    protected void processExceptions(MethodDeclaration method, Operation operation) {
        //  create Fault Messages
        JavaMethod javaMethod = operation.getJavaMethod();
        javaMethod.setThrowsRemoteException(false);            
        Collection<ReferenceType> exceptions = method.getThrownTypes();
        for (ReferenceType exception : exceptions) {
            if (exception.toString().equals(REMOTE_EXCEPTION_CLASSNAME))
                javaMethod.setThrowsRemoteException(true);
            createFault(((ClassType)exception).getDeclaration(), operation);
        }        
    }    
    
    protected void createFault(TypeDeclaration exception, Operation operation) {
        if (builder.isRemoteException(exception))
            return;        
        String exceptionName = exception.getQualifiedName();
        FaultInfo faultInfo = seiContext.getExceptionBeanName(exceptionName);
        JavaException javaException = new JavaException(exception.getQualifiedName(), true, null);        
        JAXBType jaxbType = null;
        QName elementName;        
        QName faultQName;
        String javaMemberName = null;
        if (faultInfo.isWSDLException()) {
            AnnotationProcessorEnvironment apEnv = builder.getAPEnv();
            TypeMirror bean = faultInfo.getBeanTypeMoniker().create(apEnv);
            String beanName = bean.toString();
            Reference ref = seiContext.getReference(((DeclaredType)bean).getDeclaration());
//            faultQName = getMemberSchemaName(new Reference(((DeclaredType)bean).getDeclaration(), apEnv));
            faultQName = getMemberSchemaName(ref);
            elementName = faultInfo.getElementName();
            jaxbType = new JAXBType(faultQName, 
                                    new JavaSimpleType(beanName, null),
                                    null,
                                    seiContext.getJAXBModel());       
            javaMemberName = GeneratorConstants.FAULT_CLASS_MEMBER_NAME;
            JavaStructureMember javaMember = new JavaStructureMember(
                                                javaMemberName,
                                                jaxbType.getJavaType(),
                                                null);
            javaMember.setReadMethod(GET_FAULT_INFO);
            javaException.add(javaMember);            
        } else {
            String beanName = faultInfo.getBeanName();
            TypeDeclaration beanDecl = builder.getTypeDeclaration(beanName);
            jaxbType = getJAXBTypeForQName(beanDecl, seiContext.getJAXBModel(), faultInfo.getMembers());
            faultQName = jaxbType.getName();
            elementName = faultQName;
        }
        Block faultBlock = new Block(elementName, jaxbType); 
        Fault fault = new Fault(elementName.getLocalPart());
        fault.setJavaException(javaException);
        fault.setWsdlException(faultInfo.isWSDLException());
        fault.setBlock(faultBlock);
        fault.setElementName(elementName);
        fault.setJavaMemberName(javaMemberName);
        operation.addFault(fault);
        operation.getJavaMethod().addException(exceptionName);
    } 
        
    protected JAXBProperty createJAXBProperty(MemberInfo member) {
        JAXBProperty property = new JAXBProperty();
        JavaType javaType = new JavaSimpleType(member.getParamType(), null);
        property.setType(member.getParamType());
        property.setName(member.getParamName());
        if (member.getElementName() != null)
            property.setElementName(member.getElementName());
        return property; 
        
    }
    
    protected void processRpcMethod(MethodDeclaration method, WebMethod webMethod) {
        String methodName = method.getSimpleName();
        String operationName = builder.getOperationName(methodName);
        String action = "";
        String operationNamespace = wsdlNamespace;
        operationName = webMethod != null && webMethod.operationName().length() > 0 ?
                        webMethod.operationName() : methodName;
        action = webMethod != null && webMethod.action().length() > 0 ?
                        webMethod.action() : action;

        String portName = ClassNameInfo.getName(port.getJavaInterface().getName());
        builder.log("creating operation: " + operationName);
        Operation operation = createOperation(new QName(operationName), method);
        operation.setWrapped(true);
        operation.setSOAPAction(action);
        JavaMethod javaMethod = new JavaMethod(methodName);
        javaMethod.setDeclaringClass(getQualifiedName(method.getDeclaringType()));
        operation.setJavaMethod(javaMethod);
        String packageName = method.getDeclaringType().getPackage().getQualifiedName();
        if (packageName.length() > 0) {
            packageName = packageName + DOT;
        }
        operation.setStyle(soapStyle);
        operation.setUse(soapUse);

        boolean isOneway = method.getAnnotation(Oneway.class) != null;        

        // create response
        JavaParameter javaParameter = null;
        Block responseBlock = null;
        Response response = null;
        boolean hasHolders = false;
        for (ParameterDeclaration param : method.getParameters()) {
            hasHolders = param.getType() instanceof DeclaredType &&
                builder.getHolderValueType(param.getType())
                    != null;
            if (hasHolders)
                break;
        }
        TypeMirror returnType = method.getReturnType();
        WebResult webResult = method.getAnnotation(WebResult.class);
        String responseName = RETURN;
        String responseNamespace = wsdlNamespace;
        if (webResult != null) {
            responseName = webResult.name().length() > 0 ? webResult.name() : responseName;
            responseNamespace = webResult.targetNamespace().length() > 0 ? webResult.targetNamespace() : responseNamespace;
        }  
        
        if (isOneway) {
            if (!(returnType instanceof VoidType))
                builder.onError("webserviceap.oneway.operation.cannot.have.return.type",
                                new Object[] {typeDecl.getQualifiedName(), methodName});
            if (hasHolders)
                builder.onError("webserviceap.oneway.operation.cannot.have.holders",
                                new Object[] {typeDecl.getQualifiedName(), methodName});
            if (method.getThrownTypes().size() > 0) {
                TypeDeclaration runtimeException = builder.getTypeDeclaration(RUNTIME_EXCEPTION_CLASSNAME);
                for (ReferenceType thrown :   method.getThrownTypes()) {
                    if (!thrown.toString().equals(REMOTE_EXCEPTION_CLASSNAME) &&
                        !thrown.toString().equals(RUNTIME_EXCEPTION_CLASSNAME) &&
                        !TypeModeler.isSubclass(((ClassType)thrown).getDeclaration(), runtimeException )) {
                        builder.onError("webserviceap.oneway.operation.cannot.declare.exceptions",
                                      new Object[] {methodName, thrown.toString()});                        
                    }
                }                
            }
        }
        RpcLitStructure resStruct = new RpcLitStructure();
        if (!isOneway || !(returnType instanceof VoidType) || hasHolders) {
            response = new Response();
            responseBlock = 
                new Block(
                    new QName(
                        operationNamespace,
                        getXMLName(Names.getResponseName(operationName))));
            response.addBodyBlock(responseBlock);
            resStruct = new RpcLitStructure(responseBlock.getName(), seiContext.getJAXBModel());
            resStruct.setJavaType(new JavaSimpleType("com.sun.xml.rpc.encoding.jaxb.RpcLitPayload", null));
            responseBlock.setType(resStruct);
            if (!(returnType instanceof VoidType)) {
                String typeStr = returnType.toString();
                QName typeName = new QName(responseNamespace, responseName);
                Parameter resultParam = new Parameter(responseName);
                RpcLitMember member = new RpcLitMember(new QName(responseNamespace, responseName), typeStr);
                Reference ref = seiContext.getReference(method);
//                member.setSchemaTypeName(getMemberSchemaName(new Reference(method))); 
                member.setSchemaTypeName(getMemberSchemaName(ref)); 
                JavaType javaType = simpleTypeCreator.getJavaSimpleType(typeStr);
                if (javaType == null)
                    javaType = new JavaSimpleType(typeStr, null);
                member.setJavaType(javaType);
                resStruct.addRpcLitMember(member);
                resultParam.setType(member);
                resultParam.setEmbedded(true);
                resultParam.setBlock(responseBlock);
                javaParameter =
                    new JavaParameter(null, resultParam.getType().getJavaType(),
                        resultParam,
                        false);
                javaMethod.setReturnType(resultParam.getType().getJavaType());
                resultParam.setJavaParameter(javaParameter);
                response.addParameter(resultParam);           
                operation.setProperty(WSDLModelerBase.WSDL_RESULT_PARAMETER, resultParam.getName());                   
            }
            operation.setResponse(response);
        } 
        
        //  create request
        Request request = new Request();
        // build up body block
        Parameter parameter;
        QName typeName = new QName(operationNamespace, getXMLName(operationName));
        Block block = new Block(typeName);
        Block headerBlock = null;
        RpcLitStructure rpcStruct = new RpcLitStructure(typeName, seiContext.getJAXBModel());
        rpcStruct.setJavaType(new JavaSimpleType("com.sun.xml.rpc.encoding.jaxb.RpcLitPayload", null));
        block.setType(rpcStruct);
        TypeMirror holderType;
        String paramName = null;
        String paramNamespace;
        WebParam.Mode mode = WebParam.Mode.IN;
        boolean isHeader = false;
        boolean isInParam;
        int pos = 0;
        for (ParameterDeclaration param : method.getParameters()) {
            parameter = null;
            mode = null;  
            isHeader = false;
            isInParam = true;
            String typeStr = param.getType().toString();
            WebParam webParam = param.getAnnotation(WebParam.class);                
            paramNamespace = wsdlNamespace;            
            paramName = "arg"+pos;
            if (webParam != null) {
                if (webParam.name().length() > 0)
                    paramName = webParam.name(); 
                mode = webParam.mode();
                isHeader = webParam.header();
                paramNamespace = webParam.targetNamespace().length() > 0 ? webParam.targetNamespace() : paramNamespace;
                
            }
            holderType = builder.getHolderValueType(param.getType());
            if (holderType != null)
                typeStr = holderType.toString();
            
            TypeMirror paramType = holderType != null ? holderType : param.getType();
            QName paramQName = new QName(paramNamespace, paramName);
            parameter = new Parameter(getXMLName(paramName));
            parameter.setParameterOrderPosition(pos);            
            JAXBType jaxbType = new JAXBType(paramQName, 
                                          new JavaSimpleType(typeStr, null),
                                          null,
                                          seiContext.getJAXBModel());
            if (isHeader) {
                if (webParam.name().length() == 0)
                    builder.onError("webserviceap.header.parameters.must.have.webparam.name",
                                new Object[] {typeDecl.getQualifiedName(), method.toString(), param.toString()});
                
                headerBlock = new Block(new QName(paramNamespace, getXMLName(paramName)));
                parameter.setType(jaxbType);                
                parameter.setBlock(headerBlock);
                headerBlock.setType(jaxbType);
                parameter.setEmbedded(false);
                if (!(mode != null && mode.equals(WebParam.Mode.OUT)))
                    request.addHeaderBlock(headerBlock);                                        
            } else { 
                RpcLitMember member = new RpcLitMember(paramQName, typeStr);
                Reference ref = seiContext.getReference(param);
//                member.setSchemaTypeName(getMemberSchemaName(new Reference(paramType, param))); 
                member.setSchemaTypeName(getMemberSchemaName(ref)); 
                JavaType javaType = simpleTypeCreator.getJavaSimpleType(typeStr);
                if (javaType == null)
                    javaType = new JavaSimpleType(typeStr, null);
                member.setJavaType(javaType);
                parameter.setType(member);    
                parameter.setEmbedded(true);
                parameter.setBlock(block);
                if (mode == null || !mode.equals(WebParam.Mode.OUT))
                    rpcStruct.addRpcLitMember(member);                 
            }
            if (holderType != null) {
                Parameter responseParam = new Parameter(getXMLName(paramName));                
                if (!isHeader) {
                    responseParam.setEmbedded(true);
                    RpcLitMember member = new RpcLitMember(paramQName, typeStr);
                    Reference ref = seiContext.getReference(param);
//                    member.setSchemaTypeName(getMemberSchemaName(new Reference(paramType,param)));
                    member.setSchemaTypeName(getMemberSchemaName(ref));
                    JavaType javaType = simpleTypeCreator.getJavaSimpleType(typeStr);
                    if (javaType == null)
                        javaType = new JavaSimpleType(typeStr, null);
                    member.setJavaType(javaType);
                    responseParam.setJavaParameter(javaParameter); 
                    responseParam.setType(parameter.getType());
                    responseParam.setBlock(responseBlock);
                    resStruct.addRpcLitMember(member);
                } else {
                    Block resHeaderBlock = new Block(new QName(paramNamespace, getXMLName(paramName)));                    
                    responseParam.setType(jaxbType);
                    responseParam.setBlock(resHeaderBlock);
                    responseParam.setEmbedded(false);
                    resHeaderBlock.setType(jaxbType); 
                    response.addHeaderBlock(resHeaderBlock);
                    if (mode == null || mode.equals(WebParam.Mode.INOUT)) {
                        parameter.setLinkedParameter(responseParam);
                        responseParam.setLinkedParameter(parameter);
                    }                    
                    
                }
                responseParam.setParameterOrderPosition(pos);
                javaParameter =
                    new JavaParameter(
                        paramName,
                        responseParam.getType().getJavaType(),
                        responseParam,
                        true);
                javaParameter.setHolderName(param.getType().toString());
                responseParam.setJavaParameter(javaParameter);
                if (mode == null || mode.equals(WebParam.Mode.INOUT)) {
                    parameter.setLinkedParameter(responseParam);
                    responseParam.setLinkedParameter(parameter);
                }
                response.addParameter(responseParam);   
                isInParam = !(mode != null && mode.equals(WebParam.Mode.OUT));
            } 
            if (isInParam) {
                javaParameter =
                    new JavaParameter(
                        paramName,
                        parameter.getType().getJavaType(),
                        parameter,
                        holderType != null);
                if (holderType != null)
                    javaParameter.setHolderName(param.getType().toString());
                parameter.setJavaParameter(javaParameter);
                request.addParameter(parameter);
            }
            javaMethod.addParameter(javaParameter);
            pos++;                  
        }
        request.addBodyBlock(block);
        operation.setRequest(request);

        processExceptions(method, operation);

        port.addOperation(operation);
        port.getJavaInterface().addMethod(javaMethod);
    }
    
    protected QName getMemberSchemaName(Reference typeReference) {
        QName name;
        JAXBModel jaxbModel = seiContext.getJAXBModel();
        name = jaxbModel.getJ2SJAXBModel().getXmlTypeName(typeReference);    
        return name;
    }
    
    private String getQualifiedName(TypeDeclaration type) {
        if (type instanceof ClassDeclaration)
            return ((ClassDeclaration)type).getQualifiedName();
        else if (type instanceof InterfaceDeclaration)
            return ((InterfaceDeclaration)type).getQualifiedName();
        else if (type instanceof EnumDeclaration)
            return ((EnumDeclaration)type).getQualifiedName();
        return null;
    }
    
    protected String getXMLName(String str) {
        return builder.getXMLName(str);
    }
    
    class MySOAPMessageHandlers implements SOAPMessageHandlers {
        ArrayList<SOAPMessageHandler> handlers = new ArrayList<SOAPMessageHandler>();
        public SOAPMessageHandler[] value() {
            SOAPMessageHandler[] array = new MySOAPMessageHandler[handlers.size()];
            return (SOAPMessageHandler[])handlers.toArray(array);
        }
        void addHandler(SOAPMessageHandler handler) {
            handlers.add(handler);
        }
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return SOAPMessageHandlers.class;
        }
    }

    class MySOAPMessageHandler implements SOAPMessageHandler {
        String name = "";
        String className = null;
        ArrayList<InitParam> initParams = new ArrayList<InitParam>();
        ArrayList<String> roles = new ArrayList<String>();
        ArrayList<String> headers = new ArrayList<String>();
        
        public String name() {
            return name;
        } 
        void setName(String name) {
            this.name = name;
        }
        public String className() {
            return className;
        }
        void setClassName(String className) {
            this.className = className;
        }
        public InitParam[] initParams() {
            InitParam[] array = new MyInitParam[initParams.size()];
            return (InitParam[])initParams.toArray(array);
        }
        void addInitParam(InitParam initParam) {
            initParams.add(initParam);
        }
        void addInitParam(String key, String Value) {
            initParams.add(new MyInitParam(key, Value));
        }
        public String[] roles() {
            String[] array = new String[roles.size()];
            return (String[])roles.toArray(array);
        }
        void addRole(String role) {
            roles.add(role);
        }
        public String[] headers() {
            String[] array = new String[headers.size()];
            return (String[])headers.toArray(array);
        }       
        void addHeader(String header) {
            headers.add(header);
        }
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return SOAPMessageHandler.class;
        }
        
        class MyInitParam implements InitParam {
            String name = null;
            String value = null;
            MyInitParam(String name, String value) {
                this.name = name;
                this.value = value;
            }
            public String name() {
                return name;
            }
            public String value() {
                return value;
            }
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return InitParam.class;
            }
        }
    }
} 
    
