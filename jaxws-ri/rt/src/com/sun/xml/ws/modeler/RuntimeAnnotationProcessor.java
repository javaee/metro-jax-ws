/**
 * $Id: RuntimeAnnotationProcessor.java,v 1.3 2005-05-25 21:20:45 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.modeler;

import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.RequestWrapper;
import com.sun.xml.ws.ResponseWrapper;
import com.sun.xml.ws.SOAPBinding;
import com.sun.xml.ws.model.*;
import com.sun.xml.ws.model.soap.SOAPBlock;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.model.soap.Style;

import javax.jws.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.ParameterIndex;
import javax.xml.ws.WebFault;
import javax.xml.ws.Holder;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.rmi.RemoteException;
import java.util.StringTokenizer;


/**
 * $author: JAXWS Development Team
 */
public class RuntimeAnnotationProcessor {
    private QName portQName;
    private Class portClass;
    private RuntimeModel runtimeModel;
    private com.sun.xml.ws.model.soap.SOAPBinding defaultBinding;
    private String packageName;
    private String targetNamespace;
    private boolean isWrapped = true;
    private boolean usesWebMethod = false;
    public static final String PD_JAXWS_PACKAGE_PD  = ".jaxws.";
    public static final String JAXWS_PACKAGE_PD        = "jaxws.";
    public static final String RESPONSE             = "Response";
    public static final String RETURN               = "return";
    public static final String BEAN                 = "Bean";
    public static final Class HOLDER_CLASS = Holder.class;
    public static final Class REMOTE_EXCEPTION_CLASS = RemoteException.class;
    public static final Class RPC_LIT_PAYLOAD_CLASS = com.sun.xml.ws.encoding.jaxb.RpcLitPayload.class;

    public RuntimeAnnotationProcessor(QName portName, Class portClass) {
        this.portQName = portName;
        this.portClass = portClass;
    }

    //currently has many local vars which will be eliminated after debugging issues
    //first draft
    public RuntimeModel buildRuntimeModel() {
        runtimeModel = new SOAPRuntimeModel();

        if (!portClass.isAnnotationPresent(javax.jws.WebService.class))
            throw new WebServiceException("Annotations must be supported for JAXRPC 2.0");

        Class clazz = portClass;
        WebService webService =
            (WebService) portClass.getAnnotation(WebService.class);
        if (webService.endpointInterface().length() > 0) {
            clazz = getClass(webService.endpointInterface());
            if (!clazz.isAnnotationPresent(javax.jws.WebService.class)) {
                // TODO localize
                throw new WebServiceException("EndpointInterface: "+
                    webService.endpointInterface()+" does not have WebServcie Annotation");
            }
        }

        processClass(clazz);
        runtimeModel.postProcess();
        return runtimeModel;
    }

    protected static Class getClass(String className) {
//        System.out.println("getClass: "+className);
        try {
            //return Class.forName(className, Thread.currentThread().getContextClassLoader());
            return Thread.currentThread().getContextClassLoader().loadClass(className);
//            return portInterface.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            // TODO locallize
            throw new WebServiceException("class: "+
                className+" could not be found");
        }
    }


    void processClass(Class clazz) {
        WebService webService =
            (WebService) clazz.getAnnotation(WebService.class);
        String portName  = clazz.getSimpleName();
        portName = webService.name().length() >0 ? webService.name() : portName;

        targetNamespace = webService.targetNamespace();
        packageName = clazz.getPackage().getName();
        if (targetNamespace.length() == 0)
            targetNamespace = getNamespace(packageName);
        QName name = new QName(portName, targetNamespace);
        runtimeModel.setWSDLLocation(webService.wsdlLocation());


        javax.jws.soap.SOAPBinding soapBinding =
            (javax.jws.soap.SOAPBinding) clazz.getAnnotation(javax.jws.soap.SOAPBinding.class);
        if (soapBinding != null) {
            isWrapped = soapBinding.parameterStyle().equals(
                javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED);
        }
        defaultBinding = createBinding(soapBinding);
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(WebMethod.class)) {
                usesWebMethod = true;
                break;
            }
        }

        for (Method method : clazz.getMethods()) {
            if (method.getDeclaringClass().equals(Object.class))
                continue;
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            processMethod(method, webService);
        }
    }

    protected com.sun.xml.ws.model.soap.SOAPBinding createBinding(javax.jws.soap.SOAPBinding soapBinding) {
        com.sun.xml.ws.model.soap.SOAPBinding rtSOAPBinding =
            new com.sun.xml.ws.model.soap.SOAPBinding();
        Style style = (soapBinding == null ||
            soapBinding.style().equals(javax.jws.soap.SOAPBinding.Style.DOCUMENT)) ?
            Style.DOCUMENT : Style.RPC;
        rtSOAPBinding.setStyle(style);
        return rtSOAPBinding;
//        Binding binding =
//            new Binding<com.sun.xml.rpc.rt.model.soap.SOAPBinding>(rtSOAPBinding);
//        return binding;
    }

    protected String getNamespace(String packageName) {
        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[] {"example", "com"};
        } else {
            tokens = new String[tokenizer.countTokens()];
            for (int i=tokenizer.countTokens()-1; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        String namespace = "http://";
        String dot = "";
        for (int i=0; i<tokens.length; i++) {
            if (i==1)
                dot = ".";
            namespace += dot+tokens[i];
        }
        return namespace + "/jaxrpc";
    }

    protected void processMethod(Method method, WebService webService) {
//System.out.println("processing Method: "+method.getName());

        JavaMethod javaMethod;
        if (method.getDeclaringClass().equals(portClass))
            javaMethod = new JavaMethod(method);
        else {
            try {
                Method tmpMethod = portClass.getMethod(method.getName(), 
                    method.getParameterTypes());
                javaMethod = new JavaMethod(tmpMethod);
            } catch (NoSuchMethodException e) {
                throw new WebServiceException("method: "+
                    method.getName()+" could not be found on class: "+
                    portClass.getName());                
            }
        }
        String methodName = method.getName();
        int modifier = method.getModifiers();
        //use for checking

        if (method.isAnnotationPresent(Oneway.class)) {
//            System.out.println("method is oneway");
            javaMethod.setMEP(MessageStruct.ONE_WAY_MEP);
        } else
            javaMethod.setMEP(MessageStruct.REQUEST_RESPONSE_MEP);


        WebMethod webMethod = method.getAnnotation(WebMethod.class);
        // If one WebMethod is used, then only methods with WebMethod will be
        // processed.
        if (usesWebMethod && webMethod == null) {
            return;
        }

        String action = null;
        String operationName = method.getName();
        if (webMethod != null ) {
            action = webMethod.action();
            operationName = webMethod.operationName().length() > 0 ?
                webMethod.operationName() :
                operationName;
        }

        com.sun.xml.ws.SOAPBinding methodBinding =
            method.getAnnotation(com.sun.xml.ws.SOAPBinding.class);
        SOAPBinding.MySOAPBinding myMethodBinding = null;
        boolean methodIsWrapped = isWrapped;
        Style style = defaultBinding.getStyle();
        if (methodBinding != null) {
            myMethodBinding = new SOAPBinding.MySOAPBinding(methodBinding);
            //Binding binding = createBinding(myMethodBinding);
            com.sun.xml.ws.model.soap.SOAPBinding mySOAPBinding = createBinding(myMethodBinding);
//            com.sun.xml.rpc.rt.model.soap.SOAPBinding mySOAPBinding =
//                (com.sun.xml.rpc.rt.model.soap.SOAPBinding)binding.getBindingType();
            style = mySOAPBinding.getStyle();
            if (action != null)
                mySOAPBinding.setSOAPAction(action);
            methodIsWrapped = myMethodBinding.parameterStyle().equals(
                javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED);
            //javaMethod.setBinding(binding);
            javaMethod.setBinding(mySOAPBinding);
        } else {
            javaMethod.setBinding(defaultBinding);
        }
        if (!methodIsWrapped) {
            processDocBareMethod(javaMethod, methodName, webMethod, operationName,
                method, webService);
        } else if (style.equals(Style.DOCUMENT)) {
            processDocWrappedMethod(javaMethod, methodName, webMethod, operationName,
                method, webService);
        } else {
            processRpcMethod(javaMethod, methodName, webMethod, operationName,
                method, webService);
        }
        runtimeModel.addJavaMethod(javaMethod);
    }

    protected void processDocWrappedMethod(JavaMethod javaMethod, String methodName,
        WebMethod webMethod, String operationName, Method method, WebService webService) {
        boolean isOneway = method.isAnnotationPresent(Oneway.class);
//System.out.println("processing Method: "+method.getName());
        // processParameters(method);
        //WebWrapper webWrapper = method.getAnnotation(WebWrapper.class);
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        ResponseWrapper resWrapper = method.getAnnotation(ResponseWrapper.class);
        String beanPackage = packageName + PD_JAXWS_PACKAGE_PD;
        if (packageName.length() == 0)
            beanPackage = JAXWS_PACKAGE_PD;
        //String requestClassName = beanPackage + capitalize(method.getName());
        String requestClassName = null;
        if(reqWrapper != null && reqWrapper.type().length()>0){
            requestClassName = reqWrapper.type();
        }else{
            requestClassName = beanPackage + capitalize(method.getName());
        }
            

//        String responseClassName = beanPackage+
//            capitalize(method.getName()) + RESPONSE;
        String responseClassName = null;
        if(resWrapper != null && resWrapper.type().length()>0){
            responseClassName = resWrapper.type();
        }else{
            responseClassName = beanPackage + capitalize(method.getName()) + RESPONSE;
        }

        Class requestClass = getClass(requestClassName);
        QName reqElementName = (reqWrapper != null)?new QName(reqWrapper.namespace(),reqWrapper.name()):getWrapperElementName(requestClass);
//        QName reqElementName = getWrapperElementName(requestClass);
        Class responseClass = null;
        QName resElementName = null;
        if (!isOneway) {
            responseClass = getClass(responseClassName);
//            resElementName = getWrapperElementName(responseClass);
            resElementName = (resWrapper != null)?new QName(resWrapper.namespace(),resWrapper.name()):getWrapperElementName(responseClass);
        }

        TypeReference typeRef =
                new TypeReference(reqElementName, requestClass, new Annotation[0]);
        WrapperParameter requestWrapper = new WrapperParameter(typeRef,
            com.sun.xml.ws.model.Mode.IN, 0);
        requestWrapper.setBinding(SOAPBlock.BODY);
        javaMethod.addParameter(requestWrapper);
        WrapperParameter responseWrapper = null;
        if (!isOneway) {
            typeRef = new TypeReference(resElementName, responseClass,
                                        new Annotation[0]);
            responseWrapper = new WrapperParameter(typeRef,
                com.sun.xml.ws.model.Mode.OUT, -1);
            javaMethod.addParameter(responseWrapper);
            responseWrapper.setBinding(SOAPBlock.BODY);
        }

        // return value
        String resultName = null;
        String resultTNS = null;
        QName resultQName = null;
        WebResult webResult = method.getAnnotation(WebResult.class);
        Class returnType = method.getReturnType();
        if (webResult != null) {
            resultName = webResult.name();
            resultTNS = webResult.targetNamespace();
            //is this right?
            if (resultTNS.length() == 0)
                resultTNS = targetNamespace;
            resultQName = new QName(resultTNS, resultName);
        } else if (!isOneway && !returnType.getName().equals("void")) {
            resultQName = getParamElementName(-1, responseClass);
        }


        Type genericReturnType = method.getGenericReturnType();
        if (!isOneway && (returnType != null) && (!returnType.getName().equals("void"))) {
            Class returnClazz = returnType;
            Annotation[] rann = method.getAnnotations();
            if (resultQName.getLocalPart() != null) {
                TypeReference rTypeReference = new TypeReference(resultQName, returnType, rann);
                Parameter returnParameter = new Parameter(rTypeReference, com.sun.xml.ws.model.Mode.OUT, -1);
                returnParameter.setBinding(SOAPBlock.BODY);
                responseWrapper.addWrapperChild(returnParameter);
            }
        }

        //get WebParam
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        TypeVariable<Method>[] typeVariables = method.getTypeParameters();
        Annotation[][] pannotations = method.getParameterAnnotations();
        int pos = 0;
        QName paramQName = null;
        for (Class clazzType : parameterTypes) {
            Type parameterType = clazzType;
            Parameter param = null;
            String paramName = "";
            String paramNamespace = targetNamespace;
            boolean isHeader = false;
            boolean isHolder = HOLDER_CLASS.isAssignableFrom(clazzType);
            //set the actual type argument of Holder in the TypeReference
            if (isHolder) {
                //TODO: need to handle Holder(s) defined by jaxrpc 1.1 spec
                if(clazzType.getName().equals(Holder.class.getName())){
                    Type at = ((ParameterizedType)genericParameterTypes[pos]).getActualTypeArguments()[0];
                    if(at instanceof ParameterizedType)
                        parameterType = ((ParameterizedType)at).getRawType();
                    else
                        parameterType = at;
                    //clazzType = (Class)((ParameterizedType)genericParameterTypes[pos]).getActualTypeArguments()[0];
                }
            }
            com.sun.xml.ws.model.Mode paramMode = isHolder ?
                com.sun.xml.ws.model.Mode.INOUT :
                com.sun.xml.ws.model.Mode.IN;
            for (Annotation annotation : pannotations[pos]) {
                if (annotation.annotationType() == javax.jws.WebParam.class) {
                    javax.jws.WebParam webParam = (javax.jws.WebParam) annotation;
                    paramName = webParam.name();
                    if (!webParam.targetNamespace().equals("")) {
                        paramNamespace = webParam.targetNamespace();
                    }
                    isHeader = webParam.header();
//                    if (isHeader && paramNamespace.length() == 0)
//                        paramNamespace = targetNamespace;
                    WebParam.Mode mode = webParam.mode();
                    if (isHolder && mode == javax.jws.WebParam.Mode.IN)
                        mode = javax.jws.WebParam.Mode.INOUT;
                    paramMode = (mode == javax.jws.WebParam.Mode.IN) ? com.sun.xml.ws.model.Mode.IN :
                        (mode == javax.jws.WebParam.Mode.INOUT) ? com.sun.xml.ws.model.Mode.INOUT :
                        com.sun.xml.ws.model.Mode.OUT;
                    break;
                }
            }
//            if (isHeader && paramMode == com.sun.xml.rpc.rt.model.Mode.IN)
//                paramMode = com.sun.xml.rpc.rt.model.Mode.INOUT;
            if (paramName.length() != 0) {
                paramQName = new QName(paramNamespace, paramName);
            } else {  // go get it from the wrappers
                Class paramWrapperClass = requestClass;
                if (paramMode != com.sun.xml.ws.model.Mode.IN) {
                    if (isOneway) {
                        // TODO localize this
                        throw new WebServiceException("oneway operation should not have out parameters");
                    }
                    paramWrapperClass = responseClass;
                }
                paramQName = getParamElementName(pos, paramWrapperClass);
            }
//            System.out.println("paramName: "+ paramQName);
            typeRef =
                new TypeReference(paramQName, parameterType, pannotations[pos]);
            param = new Parameter(typeRef, paramMode, pos++);
            if (isHeader) {
                param.setBinding(SOAPBlock.HEADER);
                javaMethod.addParameter(param);
            } else {
                param.setBinding(SOAPBlock.BODY);
                if (!paramMode.equals(com.sun.xml.ws.model.Mode.OUT)) {
                    requestWrapper.addWrapperChild(param);
                }
                if (!paramMode.equals(com.sun.xml.ws.model.Mode.IN)) {
                    if (isOneway) {
                        // TODO localize this
                        throw new WebServiceException("oneway operation should not have out parameters");
                    }
                    responseWrapper.addWrapperChild(param);
                }
            }
        }
        processExceptions(javaMethod, method);
    }

    protected QName getParamElementName(int paramPos, Class wrapperClass) {
        QName elementName = null;
        for (Field field : wrapperClass.getDeclaredFields()) {
            ParameterIndex paramIndex = field.getAnnotation(ParameterIndex.class);
            if (paramIndex.value() == paramPos) {
                XmlElement xmlElement = field.getAnnotation(XmlElement.class);
                String namespace = xmlElement.namespace();
                String name = xmlElement.name();
                elementName = new QName(namespace, name);
                break;
            }
        }
        return elementName;
    }

    protected QName getWrapperElementName(Class wrapper) {
        QName elementName = null;
        XmlRootElement rootElement = (XmlRootElement)wrapper.getAnnotation(XmlRootElement.class);
        if (rootElement != null) {
            String localName = rootElement.name();
            String namespace = rootElement.namespace();
            elementName = new QName(namespace, localName);
        } else {
            // TODO this is wrong we need an annotation to solve this.
            XmlType type = (XmlType)wrapper.getAnnotation(XmlType.class);
            if (type != null) {
                String localName = type.name();
                String namespace = type.namespace();
                elementName = new QName(namespace, localName);
            }
        }
        return elementName;
    }


    protected void processRpcMethod(JavaMethod javaMethod, String methodName,
        WebMethod webMethod, String operationName, Method method, WebService webService) {
        boolean isOneway = method.isAnnotationPresent(Oneway.class);
//System.out.println("processing Method: "+method.getName());
        // processParameters(method);
        String beanPackage = packageName + PD_JAXWS_PACKAGE_PD;
        if (packageName.length() == 0)
            beanPackage = JAXWS_PACKAGE_PD;
        QName reqElementName = new QName(targetNamespace, operationName);
        QName resElementName = null;
        if (!isOneway) {
            resElementName = new QName(targetNamespace, operationName+RESPONSE);
        }

        TypeReference typeRef =
                new TypeReference(reqElementName, RPC_LIT_PAYLOAD_CLASS, new Annotation[0]);
        WrapperParameter requestWrapper = new WrapperParameter(typeRef,
            com.sun.xml.ws.model.Mode.IN, 0);
        javaMethod.addParameter(requestWrapper);
        WrapperParameter responseWrapper = null;
        if (!isOneway) {
            typeRef = new TypeReference(resElementName, RPC_LIT_PAYLOAD_CLASS,
                                        new Annotation[0]);
            responseWrapper = new WrapperParameter(typeRef,
                com.sun.xml.ws.model.Mode.OUT, -1);
            javaMethod.addParameter(responseWrapper);
        }

        String resultName = null;
        String resultTNS = null;
        QName resultQName = null;
        WebResult webResult = method.getAnnotation(WebResult.class);

        if (webResult != null) {
            resultName = webResult.name();
            resultQName = new QName(resultName);
        } else if (!isOneway) {
            resultQName = new QName(RETURN);
        }

        Class returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        if (!isOneway && (returnType != null) && (!returnType.getName().equals("void"))) {
            Class returnClazz = returnType;
            Annotation[] rann = method.getAnnotations();
            TypeReference rTypeReference = new TypeReference(resultQName, returnType, rann);
            Parameter returnParameter = new Parameter(rTypeReference, com.sun.xml.ws.model.Mode.OUT, -1);
            returnParameter.setBinding(SOAPBlock.BODY);
            responseWrapper.addWrapperChild(returnParameter);
        }

        //get WebParam
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        TypeVariable<Method>[] typeVariables = method.getTypeParameters();
        Annotation[][] pannotations = method.getParameterAnnotations();
        int pos = 0;
        QName paramQName = null;
        for (Class clazzType : parameterTypes) {
            Parameter param = null;
            String paramName = "";
            String paramNamespace = "";
            boolean isHeader = false;
            boolean isHolder = HOLDER_CLASS.isAssignableFrom(clazzType);
            //set the actual type argument of Holder in the TypeReference
            if (isHolder) {
                //TODO: need to handle Holder(s) defined by jaxrpc 1.1 spec
                if (clazzType.getName().equals(Holder.class.getName()))
                    clazzType = (Class)((ParameterizedType)genericParameterTypes[pos]).getActualTypeArguments()[0];
            }
            com.sun.xml.ws.model.Mode paramMode = isHolder ?
                com.sun.xml.ws.model.Mode.INOUT :
                com.sun.xml.ws.model.Mode.IN;
            for (Annotation annotation : pannotations[pos]) {
                if (annotation.annotationType() == javax.jws.WebParam.class) {
                    javax.jws.WebParam webParam = (javax.jws.WebParam) annotation;
                    paramName = webParam.name();
                    isHeader = webParam.header();
                    WebParam.Mode mode = webParam.mode();
                    paramNamespace = webParam.targetNamespace();
                    if (isHolder && mode == javax.jws.WebParam.Mode.IN)
                        mode = javax.jws.WebParam.Mode.INOUT;
                    paramMode = (mode == javax.jws.WebParam.Mode.IN) ? com.sun.xml.ws.model.Mode.IN :
                        (mode == javax.jws.WebParam.Mode.INOUT) ? com.sun.xml.ws.model.Mode.INOUT :
                        com.sun.xml.ws.model.Mode.OUT;
                    break;
                }
            }

            if (paramName.length() == 0) {
                paramName = "arg"+pos;
            }

            if (!isHeader) {
                //its rpclit body param, set namespace to ""
                paramQName = new QName("", paramName);
            } else {
                if (paramNamespace.length() == 0)
                    paramNamespace = targetNamespace;
                paramQName = new QName(paramNamespace, paramName);
            }
//            System.out.println("paramName: "+ paramQName);
            typeRef =
                new TypeReference(paramQName, clazzType, pannotations[pos]);
            param = new Parameter(typeRef, paramMode, pos++);
            if (isHeader) {
                param.setBinding(SOAPBlock.HEADER);
                javaMethod.addParameter(param);
            } else {
                param.setBinding(SOAPBlock.BODY);
                if (!paramMode.equals(com.sun.xml.ws.model.Mode.OUT)) {
                    requestWrapper.addWrapperChild(param);
                }
                if (!paramMode.equals(com.sun.xml.ws.model.Mode.IN)) {
                    if (isOneway) {
                        // TODO localize this
                        throw new WebServiceException("oneway operation should not have out parameters");
                    }
                    responseWrapper.addWrapperChild(param);
                }
            }
        }
        processExceptions(javaMethod, method);
    }

    protected void processExceptions(JavaMethod javaMethod, Method method) {
        for (Type exception : method.getGenericExceptionTypes()) {
            if (REMOTE_EXCEPTION_CLASS.isAssignableFrom((Class)exception))
                continue;
            Class exceptionBean;
            Annotation[] anns;
            Method faultInfoMethod = getWSDLExceptionFaultInfo((Class)exception);
            ExceptionType exceptionType = ExceptionType.WSDLException;
            String namespace = targetNamespace;
            String name = ((Class)exception).getSimpleName();
            if (faultInfoMethod == null)  {
                String packageName = ((Class)exception).getPackage().getName();
                String beanPackage = packageName + PD_JAXWS_PACKAGE_PD;
                if (packageName.length() == 0)
                    beanPackage = JAXWS_PACKAGE_PD;
                String className = beanPackage+ name + BEAN;
                exceptionBean = getClass(className);
                exceptionType = ExceptionType.UserDefined;
                anns = exceptionBean.getAnnotations();
            } else {
                WebFault webFault = (WebFault)((Class)exception).getAnnotation(WebFault.class);
                exceptionBean = faultInfoMethod.getReturnType();
                anns = faultInfoMethod.getAnnotations();
                namespace = webFault.targetNamespace();
                name = webFault.name();
            }
            QName faultName = new QName(namespace, name);
            TypeReference typeRef = new TypeReference(faultName, exceptionBean,
                anns);
            CheckedException checkedException =
                new CheckedException((Class)exception, typeRef, exceptionType);
            javaMethod.addException(checkedException);
        }
    }

    protected Method getWSDLExceptionFaultInfo(Class exception) {
        if (!exception.isAnnotationPresent(WebFault.class))
            return null;
        try {
            Method getFaultInfo = exception.getMethod("getFaultInfo", new Class[0]);
            return getFaultInfo;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    protected void processDocBareMethod(JavaMethod javaMethod, String methodName,
        WebMethod webMethod, String operationName, Method method, WebService webService) {

        String resultName = null;
        String resultTNS = null;
        WebResult webResult = method.getAnnotation(WebResult.class);

        if (webResult != null) {
            resultName = webResult.name();
            resultTNS = webResult.targetNamespace();
            if (resultTNS == null || resultTNS.equals("")) {
                resultTNS = targetNamespace;
            }
        } else {
            resultTNS = targetNamespace;
            resultName = operationName+"Response";
        }

        Class returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        QName responseQName = null;
        if ((returnType != null) && (!returnType.getName().equals("void"))) {
            Class returnClazz = returnType;
            Annotation[] rann = method.getAnnotations();
            QName rqname = null;
            if (resultName != null) {
                responseQName = new QName(resultTNS, resultName);
                TypeReference rTypeReference = new TypeReference(responseQName, returnType, rann);
                Parameter returnParameter = new Parameter(rTypeReference, com.sun.xml.ws.model.Mode.OUT, -1);
                returnParameter.setBinding(SOAPBlock.BODY);
                javaMethod.addParameter(returnParameter);
            }
        }

        //get WebParam
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        TypeVariable<Method>[] typeVariables = method.getTypeParameters();
        Annotation[][] pannotations = method.getParameterAnnotations();
        QName requestQName = null;
        int pos = 0;
        for (Class clazzType : parameterTypes) {
            Parameter param = null;
            String paramName = method.getName();
            String targetNamespace = webService.targetNamespace();
            boolean isHeader = false;
//            com.sun.xml.rpc.rt.model.Mode paramMode = com.sun.xml.rpc.rt.model.Mode.IN;
            boolean isHolder = HOLDER_CLASS.isAssignableFrom(clazzType);
            //set the actual type argument of Holder in the TypeReference
            if (isHolder) {
                //TODO: need to handle Holder(s) defined by jaxrpc 1.1 spec
                if (clazzType.getName().equals(Holder.class.getName()))
                    clazzType = (Class)((ParameterizedType)genericParameterTypes[pos]).getActualTypeArguments()[0];
            }
            com.sun.xml.ws.model.Mode paramMode = isHolder ?
                com.sun.xml.ws.model.Mode.INOUT :
                com.sun.xml.ws.model.Mode.IN;
            for (Annotation annotation : pannotations[pos]) {
                if (annotation.annotationType() == javax.jws.WebParam.class) {
                    javax.jws.WebParam webParam = (javax.jws.WebParam) annotation;
                    paramName = webParam.name();
                    if (!webParam.targetNamespace().equals("")) {
                        targetNamespace = webParam.targetNamespace();
                    }
                    isHeader = webParam.header();
                    WebParam.Mode mode = webParam.mode();
                    if (isHolder && mode == javax.jws.WebParam.Mode.IN)
                        mode = javax.jws.WebParam.Mode.INOUT;
                    paramMode = (mode == javax.jws.WebParam.Mode.IN) ? com.sun.xml.ws.model.Mode.IN :
                        (mode == javax.jws.WebParam.Mode.INOUT) ? com.sun.xml.ws.model.Mode.INOUT :
                        com.sun.xml.ws.model.Mode.OUT;
                    break;
                }
            }

            requestQName = new QName(targetNamespace, paramName);
            //doclit/wrapped
            TypeReference typeRef = //operationName with upper 1 char
                new TypeReference(requestQName, clazzType,
                    pannotations[pos]);
            param = new Parameter(typeRef, paramMode, pos++);
            if (isHeader)
                param.setBinding(SOAPBlock.HEADER);
            else
                param.setBinding(SOAPBlock.BODY);
            javaMethod.addParameter(param);
        }

        processExceptions(javaMethod, method);
    }

    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
    
    /*
     * Return service QName
     */
    public static QName getServiceName(Class implClass) {
        String name = implClass.getSimpleName();
        WebService webService =
            (WebService)implClass.getAnnotation(WebService.class);
        if (webService == null) {
            throw new WebServiceException(
                "Require @WebService annotation on implementation class");
        }
        if (webService.endpointInterface().length() > 0) {
            Class seiClass = getClass(webService.endpointInterface());
            webService = (WebService)seiClass.getAnnotation(WebService.class);
            if (webService == null) {
                throw new WebServiceException(
                    "Require @WebService annotation on SEI class");
            }
            name = seiClass.getSimpleName();
        }
        if (webService.name().length() > 0) {
            name = webService.name();
        }
        return new QName(webService.targetNamespace(), name);
    }
}
