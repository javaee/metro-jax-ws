/**
 * $Id: WebServiceWrapperGenerator.java,v 1.3 2005-05-25 21:20:45 kohlert Exp $
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

import com.sun.tools.ws.processor.generator.GeneratorBase20;
import com.sun.tools.ws.processor.generator.GeneratorConstants;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.StringUtils;
import com.sun.xml.ws.util.Version;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.xml.ws.RequestWrapper;
import com.sun.xml.ws.ResponseWrapper;

import javax.xml.namespace.QName;
import javax.xml.bind.annotation.*;
import javax.xml.ws.WebFault;
import javax.xml.ws.ParameterIndex;

import javax.jws.*;
import javax.jws.soap.*;
import com.sun.tools.ws.processor.modeler.annotation.*;


/**
 *
 * @author  dkohlert
 */
public class WebServiceWrapperGenerator extends WebServiceVisitor {
    protected Set<String> wrapperNames;
    protected Set<String> processedExceptions;

    public WebServiceWrapperGenerator(ModelBuilder builder, AnnotationProcessorContext context) {
        super(builder, context);
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

    protected boolean shouldProcessMethod(MethodDeclaration method, WebMethod webMethod) {
        return !hasWebMethods || webMethod != null;
    }
    
    protected void processMethod(MethodDeclaration method, WebMethod webMethod) {
        boolean generatedWrapper = false;
        com.sun.xml.ws.SOAPBinding soapBinding = method.getAnnotation(com.sun.xml.ws.SOAPBinding.class);
        builder.log("method: "+method);
        boolean newBinding = false;
        if (soapBinding != null) {
            newBinding = pushSOAPBinding(new com.sun.xml.ws.SOAPBinding.MySOAPBinding(soapBinding), typeDecl);
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
        String requestClassName = beanPackage + StringUtils.capitalize(method.getSimpleName());
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        if(reqWrapper != null && (reqWrapper.type().length() > 0)){
            requestClassName = reqWrapper.type();
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
            if(resWrapper != null && (resWrapper.type().length() > 0)){
                responseClassName = resWrapper.type();
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
        String methodName = method.getSimpleName();                
        String operationName = builder.getOperationName(methodName);
        operationName = webMethod != null && webMethod.operationName().length() > 0 ?
                        webMethod.operationName() : methodName;
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
 
            IndentingWriter reqOut = null;
            if (canOverwriteRequest) {
                reqOut = createWriter(requestClassName, GeneratorConstants.FILE_TYPE_WRAPPER_BEAN);
            }

            IndentingWriter resOut = null;
            if (!isOneway && canOverwriteResponse) {
                resOut = createWriter(responseClassName, GeneratorConstants.FILE_TYPE_WRAPPER_BEAN);
            }

            WebResult webResult = method.getAnnotation(WebResult.class);
            String responseName = builder.getResponseName(operationName);
            String responseElementName = RETURN;
            if (webResult != null && webResult.name().length() > 0) {
                responseElementName = webResult.name();
            }  

            // package declaration
            String version = builder.getVersionString();
            GeneratorBase20.writePackage(reqOut, requestClassName, version, Version.VERSION_NUMBER);                                
            reqOut.pln();
            if (resOut != null) {
                GeneratorBase20.writePackage(resOut, responseClassName, version, Version.VERSION_NUMBER);                                
                resOut.pln();
            }

            // imports
            writeImport(reqOut);
            writeImport(resOut);

            // XMLElement Declarations
            writeXmlElementDeclaration(reqOut, operationName, typeNamespace);
            writeXmlElementDeclaration(resOut, responseName, typeNamespace);
                        
            collectMembers(method, operationName, typeNamespace, reqMembers, resMembers);            

            // XmlType
            writeXmlTypeDeclaration(reqOut, operationName, typeNamespace, reqMembers);
            writeXmlTypeDeclaration(resOut, responseName, typeNamespace, resMembers);
            
            // Class Declarations
            writeClassDeclaration(reqOut, requestClassName);
            writeClassDeclaration(resOut, responseClassName);
       
            // class members
            writeMembers(reqOut, reqMembers);
            writeMembers(resOut, resMembers);
            
            // default constructors
            writeDefaultConstructor(reqOut, requestClassName);
            writeDefaultConstructor(resOut, responseClassName);

            // endclass close writer
            writeClassClose(reqOut);
            writeClassClose(resOut);
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
            while (j<members.size() && members.get(j).getParamIndex() < index) {
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
        String responseName = builder.getResponseName(operationName);
        String responseElementName = RETURN;
        String responseNamespace = typeNamespace;
        if (webResult != null && webResult.name().length() > 0) {
            responseElementName = webResult.name();
            responseNamespace = webResult.targetNamespace().length() > 1 ? 
                webResult.targetNamespace() :
                responseNamespace;
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
        if (!(method.getReturnType() instanceof VoidType)) {                    
            responseMembers.add(new MemberInfo(-1, retType, RETURN_VALUE, 
                new QName(responseNamespace, responseElementName)));
        }

        for (ParameterDeclaration param : method.getParameters()) {
            WebParam.Mode mode = null;
            paramIndex++;
            holderType = builder.getHolderValueType(param.getType());
            webParam = param.getAnnotation(WebParam.class);
            paramType = param.getType().toString();
            paramNamespace = typeNamespace;
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
                    webParam.targetNamespace() : typeNamespace;
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
    
    private void writeXmlTypeDeclaration(IndentingWriter out, String typeName, String namespaceUri,
                                         ArrayList<MemberInfo> members) throws IOException {
        if (out == null)
            return;
        out.p("@XmlType(name=\""+typeName+"\", namespace=\""+namespaceUri+"\"");
        int i = 0;
        for (MemberInfo memInfo : members) {
            if (i++ == 0) {
                out.p(", propOrder={");                   
            } else {
                out.p(", ");                
            }
           out.p("\""+memInfo.getParamName()+"\"");
        }
        if (i > 0 )
            out.p("}");
        out.pln(")");
    }

    
    private void writeMembers(IndentingWriter out, ArrayList<MemberInfo> members) throws IOException {
        if (out == null)
            return;
        for (MemberInfo memInfo : members) {
            writeMember(out, memInfo.getParamIndex(), memInfo.getParamType(), 
                        memInfo.getParamName(), memInfo.getElementName());
        }
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
 
        IndentingWriter out =  createWriter(className, GeneratorConstants.FILE_TYPE_EXCEPTION_BEAN);
        
        // package declaration
        String version = builder.getVersionString();
        GeneratorBase20.writePackage(out, className, version, Version.VERSION_NUMBER);                                
        out.pln();

        // imports
        writeImport(out);

        // XmlElement Declarations
        writeXmlElementDeclaration(out, exceptionName, typeNamespace);
        
        // XmlType Declaration
        writeXmlTypeDeclaration(out, exceptionName, typeNamespace, members);
        
        // Class Declarations
        writeClassDeclaration(out, className);
        writeMembers(out, members);
        writeClassClose(out);       
        seiContext.addExceptionBeanEntry(thrownDecl.getQualifiedName(), faultInfo, builder);
        return true;
    }
    
    protected WebFault isWSDLException(Map<String, TypeMirror>map, ClassDeclaration thrownDecl) {
        WebFault webFault = thrownDecl.getAnnotation(WebFault.class);
        if (map.size() != 2 || map.get(FAULT_INFO) == null)
            return null;
        return webFault;
    }

    private IndentingWriter createWriter(String className, String type) throws IOException {
        ProcessorEnvironment env = builder.getProcessorEnvironment();
        IndentingWriter out = new IndentingWriter(env.getFiler().createSourceFile(className));

        File classFile =
            env.getNames().sourceFileForClass(
                className,
                className,
                builder.getSourceDir(),
                env);                
        GeneratedFileInfo fi = new GeneratedFileInfo();
        fi.setFile(classFile);
        fi.setType(type);
        env.addGeneratedFile(fi);            
        return out;
    }

    private void writeImport(IndentingWriter out) throws IOException {
        if (out == null)
            return;
        out.pln("import javax.xml.bind.annotation.*;");
        out.pln("import javax.xml.ws.ParameterIndex;");
        out.pln();
    }

    private void writeXmlElementDeclaration(IndentingWriter out, String elementName, String namespaceUri)
        throws IOException {

        if (out == null)
            return;
        out.p("@XmlRootElement(name=\""+elementName+"\"");       
        if (namespaceUri.length() > 0) {
            out.plnI(",");
            out.pln("namespace=\""+namespaceUri+"\")");
            out.pO();            
        } else {
            out.pln(")");
        }
        out.pln("@XmlAccessorType(AccessType.FIELD)");
    }

    private void writeClassDeclaration(IndentingWriter out, String className) throws IOException {
        if (out == null)
            return;
        out.plnI("public class "+ClassNameInfo.getName(className)+" {");            
    }

    private void writeMember(IndentingWriter out, int paramIndex, String paramType, 
        String paramName, QName elementName) throws IOException {

        if (out == null)
            return;
        if (elementName != null) {
            if (soapStyle.equals(SOAPStyle.DOCUMENT)) {
                if (wrapped)
                    out.pln("@XmlElement(namespace=\""+
                        elementName.getNamespaceURI()+"\", name=\""+
                        elementName.getLocalPart()+"\")");
                else
                    out.pln("@XmlValue");
            } else {
                out.pln("@XmlElement(namespace=\""+
                    elementName.getNamespaceURI()+"\", name=\""+
                    elementName.getLocalPart()+"\")");
            }        
        }
        if (paramIndex >= -1)
            out.pln("@ParameterIndex(value="+paramIndex+")");
        out.pln("public "+paramType+" "+paramName+";");            
    }

    private void writeDefaultConstructor(IndentingWriter out, String className) throws IOException {
        if (out == null)
            return;
        out.pln();
        out.pln("public "+ClassNameInfo.getName(className)+"(){}");
    }        

    private void writeClassClose(IndentingWriter out) throws IOException {
        if (out == null)
            return;
        out.pOln("}"); // class                                
        out.close();            
    }
}      
    
