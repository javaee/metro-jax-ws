/**
 * $Id: WebServiceWrapperGenerator.java,v 1.16 2005-08-18 00:27:00 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import com.sun.mirror.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.sun.tools.ws.processor.generator.GeneratorBase;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.StringUtils;
import com.sun.xml.ws.util.Version;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;

import javax.xml.namespace.QName;
import javax.xml.bind.annotation.*;
import javax.xml.ws.WebFault;
import javax.xml.ws.ParameterIndex;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import static com.sun.codemodel.ClassType.CLASS;
import com.sun.codemodel.CodeWriter; 
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.writer.ProgressCodeWriter;
import com.sun.tools.ws.wscompile.FilerCodeWriter;


import javax.jws.*;
import javax.jws.soap.*;
import com.sun.tools.ws.processor.modeler.annotation.*;


/**
 * This class generates the request/response and Exception Beans
 * used by the JAX-WS runtime.  
 *
 * @author  WS Development Team
 */
public class WebServiceWrapperGenerator extends WebServiceVisitor {
    protected Set<String> wrapperNames;
    protected Set<String> processedExceptions;
    protected JCodeModel cm;
    

    public WebServiceWrapperGenerator(ModelBuilder builder, AnnotationProcessorContext context) {
        super(builder, context);
        cm =  new JCodeModel();
    }
 
    protected boolean shouldProcessWebService(WebService webService, InterfaceDeclaration intf) { 
        
        if (webService == null)
            builder.onError("webserviceap.endpointinterface.has.no.webservice.annotation", 
                    new Object[] {intf.getQualifiedName()});
        if (isLegalSEI(intf))
            return true;
        return false;
    }        

    protected boolean shouldProcessWebService(WebService webService, ClassDeclaration classDecl) {   
        if (webService == null)
            return false;
        return isLegalImplementation(classDecl); 
    }   
    
    protected void processWebService(WebService webService, TypeDeclaration d) {
        wrapperNames = new HashSet<String>();
        processedExceptions = new HashSet<String>();
    }
    
    protected void postProcessWebService(WebService webService, TypeDeclaration d) {
        super.postProcessWebService(webService, d);
        if (cm != null) {
            File sourceDir = builder.getSourceDir();
            ProcessorEnvironment env = builder.getProcessorEnvironment();          
            try {
                CodeWriter cw = new FilerCodeWriter(sourceDir, env);

                if(env.verbose())
                    cw = new ProgressCodeWriter(cw, System.out);
                cm.build(cw);            
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }    

    protected boolean shouldProcessMethod(MethodDeclaration method, WebMethod webMethod) {
        return !hasWebMethods || webMethod != null;
    }
    
    protected void processMethod(MethodDeclaration method, WebMethod webMethod) {
        boolean generatedWrapper = false;
        SOAPBinding soapBinding = method.getAnnotation(SOAPBinding.class);
        builder.log("method: "+method);
        boolean newBinding = false;
        if (soapBinding != null) {
            if (soapBinding.style().equals(SOAPBinding.Style.RPC)) {
                builder.onError("webserviceap.rpc.soapbinding.not.allowed.on.method",
                        new Object[] {typeDecl.getQualifiedName(), method.toString()});
            }
            newBinding = pushSOAPBinding(soapBinding, typeDecl);
        }
        try {
            if (wrapped && soapStyle.equals(SOAPStyle.DOCUMENT)) {
                generatedWrapper = generateWrappers(method, webMethod);
            } 
            generatedWrapper = generateExceptionBeans(method) || generatedWrapper;
            if (generatedWrapper) {
                // Theres not going to be a second round
                builder.setWrapperGenerated(generatedWrapper);
            }
        } finally {
            if (newBinding) {
                popSOAPBinding();
            }
        }
    }
    
    private boolean generateExceptionBeans(MethodDeclaration method) {
        String beanPackage = packageName + PD_JAXWS_PACKAGE_PD;
        if (packageName.length() == 0)
            beanPackage = JAXWS_PACKAGE_PD;        
        boolean beanGenerated = false;
        try {
            for (ReferenceType thrownType : method.getThrownTypes()) {
                ClassDeclaration typeDecl = ((ClassType)thrownType).getDeclaration();
                if (typeDecl == null)
                    builder.onError("webserviceap.could.not.find.typedecl",
                         new Object[] {thrownType.toString(), context.getRound()});
                boolean tmp = generateExceptionBean(typeDecl, beanPackage);
                beanGenerated = beanGenerated || tmp;                    
            }       
        } catch (Exception e) {
            throw new ModelerException(
                "modeler.nestedGeneratorError",
                new LocalizableExceptionAdapter(e));
        }                 
        return beanGenerated;
    }
    
    private boolean duplicateName(String name) {
        for (String str : wrapperNames) {
            if (str.equalsIgnoreCase(name))
		return true;
        }
        wrapperNames.add(name);        
	return false;
    }
    
    private boolean generateWrappers(MethodDeclaration method, WebMethod webMethod) {
        boolean isOneway = method.getAnnotation(Oneway.class) != null;
        String beanPackage = packageName + PD_JAXWS_PACKAGE_PD;
        if (packageName.length() == 0)
            beanPackage = JAXWS_PACKAGE_PD;
        String methodName = method.getSimpleName();                
        String operationName = builder.getOperationName(methodName);
        operationName = webMethod != null && webMethod.operationName().length() > 0 ?
                        webMethod.operationName() : operationName;
        String reqName = operationName;
        String resName = operationName+RESPONSE;
        String reqNamespace = typeNamespace;
        String resNamespace = typeNamespace;

        String requestClassName = beanPackage + StringUtils.capitalize(method.getSimpleName());
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        if (reqWrapper != null) {
            if (reqWrapper.className().length() > 0) 
                requestClassName = reqWrapper.className();
            if (reqWrapper.localName().length() > 0)
                reqName = reqWrapper.localName();
            if (reqWrapper.targetNamespace().length() > 0)
                reqNamespace = reqWrapper.targetNamespace();
        }
        builder.log("requestWrapper: "+requestClassName);
        if (duplicateName(requestClassName)) {
            builder.onError("webserviceap.method.request.wrapper.bean.name.not.unique",
                             new Object[] {typeDecl.getQualifiedName(), method.toString()});                                            
        }
        boolean canOverwriteRequest = builder.canOverWriteClass(requestClassName);
        if (!canOverwriteRequest) {
            builder.log("Class " + requestClassName + " exists. Not overwriting.");
        }   
        String responseClassName = null;       
        boolean canOverwriteResponse = canOverwriteRequest;
        if (!isOneway) {
            responseClassName = beanPackage+StringUtils.capitalize(method.getSimpleName())+RESPONSE;
            ResponseWrapper resWrapper = method.getAnnotation(ResponseWrapper.class);
            if(resWrapper != null) {
                if (resWrapper.className().length() > 0)
                    responseClassName = resWrapper.className();
                if (resWrapper.localName().length() > 0)
                    resName = resWrapper.localName();
                if (resWrapper.targetNamespace().length() > 0)
                    resNamespace = resWrapper.targetNamespace();                
            }           
            if (duplicateName(responseClassName)) {
                builder.onError("webserviceap.method.respone.wrapper.bean.name.not.unique",
                                 new Object[] {typeDecl.getQualifiedName(), method.toString()});                                            
            }
            canOverwriteResponse = builder.canOverWriteClass(requestClassName);            
            if (!canOverwriteResponse) {
                builder.log("Class " + responseClassName + " exists. Not overwriting.");
            }    
        }
        ArrayList<MemberInfo> reqMembers = new ArrayList<MemberInfo>();
        ArrayList<MemberInfo> resMembers = new ArrayList<MemberInfo>();
        WrapperInfo reqWrapperInfo = new WrapperInfo(requestClassName);
        reqWrapperInfo.setMembers(reqMembers);
        WrapperInfo resWrapperInfo = null;
        if (!isOneway) {
            resWrapperInfo = new WrapperInfo(responseClassName);
            resWrapperInfo.setMembers(resMembers); 
        }
        seiContext.setReqWrapperOperation(method, reqWrapperInfo);
        if (!isOneway)
            seiContext.setResWrapperOperation(method, resWrapperInfo);
        try {
            if (!canOverwriteRequest && !canOverwriteResponse) {                
                getWrapperMembers(reqWrapperInfo);
                getWrapperMembers(resWrapperInfo);
                return false;
            }
 
            JDefinedClass reqCls = null;
            if (canOverwriteRequest) {
                reqCls = getCMClass(requestClassName, CLASS);
            }

            JDefinedClass resCls = null;
            if (!isOneway && canOverwriteResponse) {                
                resCls = getCMClass(responseClassName, CLASS);
            }

            // package declaration
            String version = builder.getVersionString();

            // XMLElement Declarations
            writeXmlElementDeclaration(reqCls, reqName,reqNamespace);
            writeXmlElementDeclaration(resCls, resName, resNamespace);
                        
            collectMembers(method, operationName, typeNamespace, reqMembers, resMembers);            

            // XmlType
            writeXmlTypeDeclaration(reqCls, reqName, reqNamespace, reqMembers);
            writeXmlTypeDeclaration(resCls, resName, resNamespace, resMembers);
                 
            // class members
            writeMembers(reqCls, reqMembers);
            writeMembers(resCls, resMembers);            
        } catch (Exception e) {
            throw new ModelerException(
                "modeler.nestedGeneratorError",
                new LocalizableExceptionAdapter(e));
        }      
        return true;
    }

    private void getWrapperMembers(WrapperInfo wrapperInfo) throws Exception {
        if (wrapperInfo == null)
            return;
        TypeDeclaration type = builder.getTypeDeclaration(wrapperInfo.getWrapperName());
        Collection<FieldDeclaration> fields = type.getFields();
        ArrayList<MemberInfo> members = new ArrayList<MemberInfo>();
        MemberInfo member;
        int i=0;
        for (FieldDeclaration field : fields) {
            XmlElement xmlElement = field.getAnnotation(XmlElement.class);
            ParameterIndex paramIndex = field.getAnnotation(ParameterIndex.class);                            
            String typeName = field.getType().toString();
            String elementName = xmlElement != null ? xmlElement.name() : field.getSimpleName();
            String namespace =  xmlElement != null ? xmlElement.namespace() : "";
            int index = paramIndex != null ? paramIndex.value() : i;
            member = new MemberInfo(index, typeName, 
                                    field.getSimpleName(), 
                                    new QName(namespace, elementName));
            int j=0;
            while (j<members.size() && members.get(j++).getParamIndex() < index) {
                break;
            }
            members.add(j, member);
            i++;
        }
        for (MemberInfo member2 : members) {
            wrapperInfo.addMember(member2);
        }
    }
    
    private void collectMembers(MethodDeclaration method, String operationName, String namespace,
                               ArrayList<MemberInfo> requestMembers,
                               ArrayList<MemberInfo> responseMembers) {
                                   
        WebResult webResult = method.getAnnotation(WebResult.class);
        String responseElementName = RETURN;
        String responseNamespace = wrapped ? EMTPY_NAMESPACE_ID : typeNamespace;
        boolean isResultHeader = false;
        if (webResult != null) { 
            if (webResult.name().length() > 0) {
                responseElementName = webResult.name();
            }
            responseNamespace = webResult.targetNamespace().length() > 1 ? 
                webResult.targetNamespace() :
                responseNamespace;
            isResultHeader = webResult.header();
        }  

        // class members 
        WebParam webParam;
        String paramType;
        String paramName;
        String paramNamespace;
        TypeMirror holderType;
        int paramIndex = -1;

        String retType = method.getReturnType() instanceof TypeDeclaration ? 
                    ((ClassDeclaration)method.getReturnType()).getQualifiedName() :
                    method.getReturnType().toString();                
        if (!(method.getReturnType() instanceof VoidType) && !isResultHeader) {                    
            responseMembers.add(new MemberInfo(-1, retType, RETURN_VALUE, 
                new QName(responseNamespace, responseElementName)));
        }

        for (ParameterDeclaration param : method.getParameters()) {
            WebParam.Mode mode = null;
            paramIndex++;
            holderType = builder.getHolderValueType(param.getType());
            webParam = param.getAnnotation(WebParam.class);
            paramType = param.getType().toString();
            paramNamespace = wrapped ? EMTPY_NAMESPACE_ID : typeNamespace;
            if (holderType != null) {
                paramType = holderType.toString();
            }
            paramName =  param.getSimpleName();
            if (webParam != null && webParam.header()) {
                continue;
            }                   
            if (webParam != null) {
                mode = webParam.mode(); 
                paramName =  webParam.name().length() > 0 ? webParam.name() : paramName;
                paramNamespace = webParam.targetNamespace().length() > 0 ?
                    webParam.targetNamespace() : paramNamespace;
            }
            MemberInfo memInfo = new MemberInfo(paramIndex, paramType, paramName, 
                new QName(paramNamespace, paramName));
            if (holderType != null) {          
                if (mode != null &&  mode.equals(WebParam.Mode.IN))
                    builder.onError("webserviceap.holder.parameters.must.not.be.in.only", 
                                new Object[] {typeDecl.getQualifiedName(), method.toString(), paramIndex});
                else if (mode == null || mode.equals(WebParam.Mode.INOUT)) {   
                    requestMembers.add(memInfo);
                }
                responseMembers.add(memInfo);
            } else {
                requestMembers.add(memInfo);
            }
        }
    }

    private JType getType(String typeName) throws IOException {
        JType type = null;
        try {
            type = cm.parseType(typeName);
            return type;
        } catch (ClassNotFoundException e) {
            type = cm.ref(typeName);
        }
        return type;
    }
    
    private void writeMembers(JDefinedClass cls, ArrayList<MemberInfo> members) throws IOException {
        if (cls == null)
            return;
        for (MemberInfo memInfo : members) {
            JType type = getType(memInfo.getParamType());
            JFieldVar field = cls.field(JMod.PRIVATE, type, memInfo.getParamName());
            QName elementName = memInfo.getElementName();
            if (elementName != null) {
                if (soapStyle.equals(SOAPStyle.RPC) || wrapped) {                   
                    JAnnotationUse xmlElementAnn = field.annotate(XmlElement.class);
                    xmlElementAnn.param("name", elementName.getLocalPart());
//                    if (elementName.getNamespaceURI().length() > 0)
                    xmlElementAnn.param("namespace", elementName.getNamespaceURI());
                } else {
                    JAnnotationUse xmlValueAnnn = field.annotate(XmlValue.class);                    
                }
            }
            if (memInfo.getParamIndex() >= -1) {
                JAnnotationUse parameterIndex = field.annotate(cm.ref(ParameterIndex.class));
                parameterIndex.param("value", memInfo.getParamIndex());
            }
        }
        for (MemberInfo memInfo : members) {
            writeMember(cls, memInfo.getParamIndex(), memInfo.getParamType(), 
                        memInfo.getParamName(), memInfo.getElementName());
        }
    }
    
    protected JDefinedClass getCMClass(String className, com.sun.codemodel.ClassType type) {
        JDefinedClass cls = null;
        try {
            cls = cm._class(className, type);
        } catch (com.sun.codemodel.JClassAlreadyExistsException e){
            cls = cm._getClass(className);
        }        
        return cls;
    }      

    private boolean generateExceptionBean(ClassDeclaration thrownDecl, String beanPackage) throws IOException {
        if (builder.isRemoteException(thrownDecl))
            return false;
        String exceptionName = ClassNameInfo.getName(thrownDecl.getQualifiedName());
        if (processedExceptions.contains(exceptionName))
            return false;
        processedExceptions.add(exceptionName);
        String className = beanPackage+ exceptionName + BEAN;
        
        Map<String, TypeMirror> propertyToTypeMap;
        propertyToTypeMap = TypeModeler.getExceptionProperties(thrownDecl);
        WebFault webFault = isWSDLException(propertyToTypeMap, thrownDecl);
        FaultInfo faultInfo;
        if (webFault != null) {
            TypeMirror beanType = propertyToTypeMap.get(FAULT_INFO);
            faultInfo = new FaultInfo(TypeMonikerFactory.getTypeMoniker(beanType), true);
            String namespace = webFault.targetNamespace().length()>0 ?
                               webFault.targetNamespace() : typeNamespace;
            String name = webFault.name().length()>0 ?
                          webFault.name() : exceptionName;
            faultInfo.setElementName(new QName(namespace, name));
            seiContext.addExceptionBeanEntry(thrownDecl.getQualifiedName(), faultInfo, builder);
            return false;
        } 
        JDefinedClass cls = getCMClass(className, CLASS);
        faultInfo = new FaultInfo(className, false);

        if (duplicateName(className)) {
            builder.onError("webserviceap.method.exception.bean.name.not.unique",
                             new Object[] {typeDecl.getQualifiedName(), thrownDecl.getQualifiedName()});                                                        
        }

        ArrayList<MemberInfo> members = new ArrayList<MemberInfo>();
        MemberInfo member;
        for (String key : propertyToTypeMap.keySet()) {
            TypeMirror type = propertyToTypeMap.get(key);
            member = new MemberInfo(-10, type.toString(), key, null);
            members.add(member);
        } 
        faultInfo.setMembers(members);
        
        boolean canOverWriteBean = builder.canOverWriteClass(className);
        if (!canOverWriteBean) {
            builder.log("Class " + className + " exists. Not overwriting.");
            seiContext.addExceptionBeanEntry(thrownDecl.getQualifiedName(), faultInfo, builder);   
            return false;
        }           
        if (seiContext.getExceptionBeanName(thrownDecl.getQualifiedName()) != null)
            return false;
 
        //write class comment - JAXWS warning
        JDocComment comment = cls.javadoc();
        for (String doc : GeneratorBase.getJAXWSClassComment(
                builder.getVersionString(), Version.VERSION_NUMBER)) {
            comment.add(doc);
        }
        
        // XmlElement Declarations
        writeXmlElementDeclaration(cls, exceptionName, typeNamespace);
        
        // XmlType Declaration
        writeXmlTypeDeclaration(cls, exceptionName, typeNamespace, members);
        
        writeMembers(cls, members);

        seiContext.addExceptionBeanEntry(thrownDecl.getQualifiedName(), faultInfo, builder);
        return true;
    }
    
    protected WebFault isWSDLException(Map<String, TypeMirror>map, ClassDeclaration thrownDecl) {
        WebFault webFault = thrownDecl.getAnnotation(WebFault.class);
        if (map.size() != 2 || map.get(FAULT_INFO) == null)
            return null;
        return webFault;
    }

    private void writeXmlElementDeclaration(JDefinedClass cls, String elementName, String namespaceUri)
        throws IOException {

       if (cls == null)
            return;
        JAnnotationUse xmlRootElementAnn = cls.annotate(XmlRootElement.class);
        xmlRootElementAnn.param("name", elementName);
        if (namespaceUri.length() > 0) {
            xmlRootElementAnn.param("namespace", namespaceUri);
        }
        JAnnotationUse xmlAccessorTypeAnn = cls.annotate(cm.ref(XmlAccessorType.class));
        xmlAccessorTypeAnn.param("value", AccessType.FIELD);
    }
   
    private void writeXmlTypeDeclaration(JDefinedClass cls, String typeName, String namespaceUri,
                                         ArrayList<MemberInfo> members) throws IOException {
        if (cls == null)
            return;
        JAnnotationUse xmlTypeAnn = cls.annotate(cm.ref(XmlType.class));
        xmlTypeAnn.param("name", typeName);
        xmlTypeAnn.param("namespace", namespaceUri);
        if (members.size() > 1) {
            JAnnotationArrayMember paramArray = xmlTypeAnn.paramArray("propOrder");
            for (MemberInfo memInfo : members) {
                paramArray.param(memInfo.getParamName());
            }
        }
    }

    private void writeMember(JDefinedClass cls, int paramIndex, String paramType, 
        String paramName, QName elementName) throws IOException {

        if (cls == null)
            return;
        String capPropName = StringUtils.capitalize(paramName);
        String getterPrefix = paramType.equals("boolean") || paramType.equals("java.lang.Boolean") ? "is" : "get";
        JMethod m = null;
        JDocComment methodDoc = null;
        JType propType = getType(paramType);
        m = cls.method(JMod.PUBLIC, propType, getterPrefix+capPropName);
        methodDoc = m.javadoc();
        JCommentPart ret = methodDoc.addReturn();
        ret.add("returns "+propType.name());
        JBlock body = m.body();
        body._return( JExpr._this().ref(paramName) );        
        
        m = cls.method(JMod.PUBLIC, cm.VOID, "set"+capPropName); 
        JVar param = m.param(propType, paramName);
        methodDoc = m.javadoc();
        JCommentPart part = methodDoc.addParam(paramName);
        part.add("the value for the "+ paramName+" property");
        body = m.body();
        body.assign( JExpr._this().ref(paramName), param );        
    }
}      
    
