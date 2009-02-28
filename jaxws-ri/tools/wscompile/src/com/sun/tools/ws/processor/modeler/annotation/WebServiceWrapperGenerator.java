/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import static com.sun.codemodel.ClassType.CLASS;
import com.sun.codemodel.*;
import com.sun.codemodel.writer.ProgressCodeWriter;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.*;
import com.sun.tools.ws.processor.generator.GeneratorBase;
import com.sun.tools.ws.processor.generator.GeneratorConstants;
import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.util.DirectoryUtil;
import com.sun.tools.ws.resources.WebserviceapMessages;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.tools.ws.wscompile.FilerCodeWriter;
import com.sun.tools.ws.wscompile.WsgenOptions;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.xml.ws.util.StringUtils;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.impl.NameConverter;

import javax.jws.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.lang.annotation.Annotation;


/**
 * This class generates the request/response and Exception Beans
 * used by the JAX-WS runtime.
 *
 * @author  WS Development Team
 */
public class WebServiceWrapperGenerator extends WebServiceVisitor {
    private Set<String> wrapperNames;
    private Set<String> processedExceptions;
    private JCodeModel cm;
    private final MakeSafeTypeVisitor makeSafeVisitor;
    private static final Class[] jaxbAnns = new Class[] {
        XmlAttachmentRef.class, XmlMimeType.class, XmlJavaTypeAdapter.class,
        XmlList.class, XmlElement.class
    };

    public WebServiceWrapperGenerator(ModelBuilder builder, AnnotationProcessorContext context) {
        super(builder, context);
        makeSafeVisitor = new MakeSafeTypeVisitor(builder.getAPEnv());
    }

    protected void processWebService(WebService webService, TypeDeclaration d) {
        cm =  new JCodeModel();
        wrapperNames = new HashSet<String>();
        processedExceptions = new HashSet<String>();
    }

    protected void postProcessWebService(WebService webService, InterfaceDeclaration d) {
        super.postProcessWebService(webService, d);
        doPostProcessWebService(webService, d);
    }
    protected void postProcessWebService(WebService webService, ClassDeclaration d) {
        super.postProcessWebService(webService, d);
        doPostProcessWebService(webService, d);
    }

    protected  void doPostProcessWebService(WebService webService, TypeDeclaration d) {
        if (cm != null) {
            File sourceDir = builder.getSourceDir();
            assert(sourceDir != null);
            WsgenOptions options = builder.getOptions();
            try {
                CodeWriter cw = new FilerCodeWriter(sourceDir, options);
                if(options.verbose)
                    cw = new ProgressCodeWriter(cw, System.out);
                cm.build(cw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void processMethod(MethodDeclaration method, WebMethod webMethod) {
        builder.log("WrapperGen - method: "+method);
        builder.log("method.getDeclaringType(): "+method.getDeclaringType());
        boolean generatedWrapper = false;
        if (wrapped && soapStyle.equals(SOAPStyle.DOCUMENT)) {
            generatedWrapper = generateWrappers(method, webMethod);
        }
        generatedWrapper = generateExceptionBeans(method) || generatedWrapper;
        if (generatedWrapper) {
            // Theres not going to be a second round
            builder.setWrapperGenerated(generatedWrapper);
        }
    }

    private boolean generateExceptionBeans(MethodDeclaration method) {
        String beanPackage = packageName + PD_JAXWS_PACKAGE_PD;
        if (packageName.length() == 0)
            beanPackage = JAXWS_PACKAGE_PD;
        boolean beanGenerated = false;
        for (ReferenceType thrownType : method.getThrownTypes()) {
            ClassDeclaration typeDecl = ((ClassType)thrownType).getDeclaration();
            if (typeDecl == null){
                builder.onError(WebserviceapMessages.WEBSERVICEAP_COULD_NOT_FIND_TYPEDECL(thrownType.toString(), context.getRound()));
                return false;
            }
            boolean tmp = generateExceptionBean(typeDecl, beanPackage);
            beanGenerated = beanGenerated || tmp;
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
///// fix for wsgen CR 6442344
        File file = new File(DirectoryUtil.getOutputDirectoryFor(requestClassName, builder.getSourceDir()),
                             Names.stripQualifier(requestClassName) + GeneratorConstants.JAVA_SRC_SUFFIX);
        builder.getOptions().addGeneratedFile(file);
//////////
        boolean canOverwriteRequest = builder.canOverWriteClass(requestClassName);
        if (!canOverwriteRequest) {
            builder.log("Class " + requestClassName + " exists. Not overwriting.");
        }
        if (duplicateName(requestClassName) && canOverwriteRequest) {
            builder.onError(WebserviceapMessages.WEBSERVICEAP_METHOD_REQUEST_WRAPPER_BEAN_NAME_NOT_UNIQUE(typeDecl.getQualifiedName(), method.toString()));
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
            canOverwriteResponse = builder.canOverWriteClass(responseClassName);
            if (!canOverwriteResponse) {
                builder.log("Class " + responseClassName + " exists. Not overwriting.");
            }
            if (duplicateName(responseClassName) && canOverwriteResponse) {
                builder.onError(WebserviceapMessages.WEBSERVICEAP_METHOD_RESPONSE_WRAPPER_BEAN_NAME_NOT_UNIQUE(typeDecl.getQualifiedName(), method.toString()));
            }
            file = new File(DirectoryUtil.getOutputDirectoryFor(responseClassName, builder.getSourceDir()),
                                 Names.stripQualifier(responseClassName) + GeneratorConstants.JAVA_SRC_SUFFIX);
            builder.getOptions().addGeneratedFile(file);
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

            // XMLElement Declarations
            writeXmlElementDeclaration(reqCls, reqName,reqNamespace);
            writeXmlElementDeclaration(resCls, resName, resNamespace);

            collectMembers(method, reqMembers, resMembers);

            // XmlType
            writeXmlTypeDeclaration(reqCls, reqName, reqNamespace, reqMembers);
            writeXmlTypeDeclaration(resCls, resName, resNamespace, resMembers);

            // class members
            writeMembers(reqCls, reqMembers);
            writeMembers(resCls, resMembers);

        } catch (Exception e) {
            throw new ModelerException("modeler.nestedGeneratorError",e);
        }
        return true;
    }

    private void collectMembers(MethodDeclaration method,
                                ArrayList<MemberInfo> requestMembers,
                                ArrayList<MemberInfo> responseMembers) {

        WebResult webResult = method.getAnnotation(WebResult.class);
        List<Annotation> jaxbRespAnnotations = collectJAXBAnnotations(method);
        String responseElementName = RETURN;
        String responseName = RETURN_VALUE;
        String responseNamespace = wrapped ? EMTPY_NAMESPACE_ID : typeNamespace;
        boolean isResultHeader = false;
        if (webResult != null) {
            if (webResult.name().length() > 0) {
                responseElementName = webResult.name();
                responseName = JAXBRIContext.mangleNameToVariableName(webResult.name());
                
                //We wont have to do this if JAXBRIContext.mangleNameToVariableName() takes
                //care of mangling java identifiers
                responseName = Names.getJavaReserverVarialbeName(responseName);
            }
            responseNamespace = webResult.targetNamespace().length() > 1 ?
                webResult.targetNamespace() :
                responseNamespace;
            isResultHeader = webResult.header();
        }

        // class members 
        WebParam webParam;
        TypeMirror paramType;
        String paramName;

        String paramNamespace;
        TypeMirror holderType;
        int paramIndex = -1;
        TypeMirror typeMirror = getSafeType(method.getReturnType());

        if (!(method.getReturnType() instanceof VoidType) && !isResultHeader) {
            responseMembers.add(new MemberInfo(typeMirror, responseName,
                new QName(responseNamespace, responseElementName), method, jaxbRespAnnotations.toArray(new Annotation[jaxbRespAnnotations.size()])));
        }

        for (ParameterDeclaration param : method.getParameters()) {
            List<Annotation> jaxbAnnotation = collectJAXBAnnotations(param);
            WebParam.Mode mode = null;
            paramIndex++;
            holderType = builder.getHolderValueType(param.getType());
            webParam = param.getAnnotation(WebParam.class);
            typeMirror =  getSafeType(param.getType());
            paramType = typeMirror;
            paramNamespace = wrapped ? EMTPY_NAMESPACE_ID : typeNamespace;
            if (holderType != null) {
                paramType = holderType;
            }
            paramName =  "arg"+paramIndex;
            if (webParam != null && webParam.header()) {
                continue;
            }
            if (webParam != null) {
                mode = webParam.mode();
                if (webParam.name().length() > 0)
                    paramName = webParam.name();
                if (webParam.targetNamespace().length() > 0)
                    paramNamespace = webParam.targetNamespace();
            }

            String propertyName = JAXBRIContext.mangleNameToVariableName(paramName);
            //We wont have to do this if JAXBRIContext.mangleNameToVariableName() takes
            //care of mangling java identifiers
            propertyName = Names.getJavaReserverVarialbeName(propertyName);

            MemberInfo memInfo = new MemberInfo(paramType, propertyName,
                new QName(paramNamespace, paramName), param, jaxbAnnotation.toArray(new Annotation[jaxbAnnotation.size()]));
            if (holderType != null) {
                if (mode == null || mode.equals(WebParam.Mode.INOUT)) {
                    requestMembers.add(memInfo);
                }
                responseMembers.add(memInfo);
            } else {
                requestMembers.add(memInfo);
            }
        }
    }

    private List<Annotation> collectJAXBAnnotations(Declaration decl) {
        List<Annotation> jaxbAnnotation = new ArrayList<Annotation>();
        for(Class jaxbClass : jaxbAnns) {
            Annotation ann = decl.getAnnotation(jaxbClass);
            if (ann != null) {
                jaxbAnnotation.add(ann);
            }
        }
        return jaxbAnnotation;
    }

    private TypeMirror getSafeType(TypeMirror type) {
        return makeSafeVisitor.apply(type, builder.getAPEnv().getTypeUtils());
    }

    private JType getType(TypeMirror typeMirror) {
        String type = typeMirror.toString();
        try {
//            System.out.println("typeName: "+typeName);
            return cm.parseType(type);
//            System.out.println("type: "+type);
        } catch (ClassNotFoundException e) {
            return cm.ref(type);
        }
    }

    private ArrayList<MemberInfo> sortMembers(ArrayList<MemberInfo> members) {
        Map<String, MemberInfo> sortedMap = new java.util.TreeMap<String, MemberInfo>();
        for (MemberInfo member : members) {
            sortedMap.put(member.getParamName(), member);
        }
        ArrayList<MemberInfo> sortedMembers = new ArrayList<MemberInfo>();
        sortedMembers.addAll(sortedMap.values());
        return sortedMembers;
    }

    private void writeMembers(JDefinedClass cls, ArrayList<MemberInfo> members) {
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
                    xmlElementAnn.param("namespace", elementName.getNamespaceURI());
                    if(memInfo.getParamType() instanceof ArrayType){
                        xmlElementAnn.param("nillable", true);
                    }
                } else {
                    field.annotate(XmlValue.class);
                }
                annotateParameterWithJAXBAnnotations(field, memInfo.getJaxbAnnotations());                
            }

            // copy adapter if needed
            XmlJavaTypeAdapter xjta = memInfo.getDecl().getAnnotation(XmlJavaTypeAdapter.class);
            if(xjta!=null) {
                JAnnotationUse xjtaA = field.annotate(XmlJavaTypeAdapter.class);
                try {
                    xjta.value();
                    throw new AssertionError();
                } catch (MirroredTypeException e) {
                    xjtaA.param("value",getType(e.getTypeMirror()));
                }
                // XmlJavaTypeAdapter.type() is for package only. No need to copy.
            }
        }
        for (MemberInfo memInfo : members) {
            writeMember(cls, memInfo.getParamType(),
                        memInfo.getParamName());
        }
    }

    private void annotateParameterWithJAXBAnnotations(JFieldVar field, Annotation[] jaxbAnnotations) {
        for(Annotation ann : jaxbAnnotations){
            if(ann instanceof XmlMimeType){
                JAnnotationUse jaxbAnn = field.annotate(XmlMimeType.class);
                jaxbAnn.param("value", ((XmlMimeType)ann).value());
            }else if(ann instanceof XmlJavaTypeAdapter){
                JAnnotationUse jaxbAnn = field.annotate(XmlJavaTypeAdapter.class);
                XmlJavaTypeAdapter ja = (XmlJavaTypeAdapter) ann;
                jaxbAnn.param("value", ja.value());
                jaxbAnn.param("type", ja.type());
            }else if(ann instanceof XmlAttachmentRef){
                field.annotate(XmlAttachmentRef.class);
            }else if(ann instanceof XmlList){
                field.annotate(XmlList.class);
            }else {
                throw new WebServiceException("Unknown JAXB annotation " + ann);
            }
        }
    }

    protected JDefinedClass getCMClass(String className, com.sun.codemodel.ClassType type) {
        JDefinedClass cls;
        try {
            cls = cm._class(className, type);
        } catch (com.sun.codemodel.JClassAlreadyExistsException e){
            cls = cm._getClass(className);
        }
        return cls;
    }

    private boolean generateExceptionBean(ClassDeclaration thrownDecl, String beanPackage) {
        if (builder.isRemoteException(thrownDecl))
            return false;

        String exceptionName = ClassNameInfo.getName(thrownDecl.getQualifiedName());
        if (processedExceptions.contains(exceptionName))
            return false;
        processedExceptions.add(exceptionName);
        WebFault webFault = thrownDecl.getAnnotation(WebFault.class);
        String className = beanPackage+ exceptionName + BEAN;

        TreeMap<String,MethodDeclaration> propertyToTypeMap = new TreeMap<String,MethodDeclaration>();

        TypeModeler.collectExceptionProperties(thrownDecl,propertyToTypeMap);

        boolean isWSDLException = isWSDLException(propertyToTypeMap, thrownDecl);
        String namespace = typeNamespace;
        String name = exceptionName;
        FaultInfo faultInfo;
        if (isWSDLException) {
            TypeMirror beanType =  getSafeType(propertyToTypeMap.get(FAULT_INFO).getReturnType());
            faultInfo = new FaultInfo(TypeMonikerFactory.getTypeMoniker(beanType), true);
            namespace = webFault.targetNamespace().length()>0 ?
                               webFault.targetNamespace() : namespace;
            name = webFault.name().length()>0 ?
                          webFault.name() : name;
            faultInfo.setElementName(new QName(namespace, name));
            seiContext.addExceptionBeanEntry(thrownDecl.getQualifiedName(), faultInfo, builder);
            return false;
        }
        if (webFault != null) {
            namespace = webFault.targetNamespace().length()>0 ?
                        webFault.targetNamespace() : namespace;
            name = webFault.name().length()>0 ?
                   webFault.name() : name;
            className = webFault.faultBean().length()>0 ?
                        webFault.faultBean() : className;

        }
        JDefinedClass cls = getCMClass(className, CLASS);
        faultInfo = new FaultInfo(className, false);

        if (duplicateName(className)) {
            builder.onError(WebserviceapMessages.WEBSERVICEAP_METHOD_EXCEPTION_BEAN_NAME_NOT_UNIQUE(typeDecl.getQualifiedName(), thrownDecl.getQualifiedName()));
        }

        ArrayList<MemberInfo> members = new ArrayList<MemberInfo>();
        for (String key : propertyToTypeMap.keySet()) {
            MethodDeclaration method = propertyToTypeMap.get(key);
            TypeMirror erasureType =  getSafeType(method.getReturnType());
            MemberInfo member = new MemberInfo(erasureType, key, null, method);
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
        for (String doc : GeneratorBase.getJAXWSClassComment(builder.getSourceVersion())) {
            comment.add(doc);
        }

        // XmlElement Declarations
        writeXmlElementDeclaration(cls, name, namespace);

        // XmlType Declaration
        members = sortMembers(members);
        writeXmlTypeDeclaration(cls, exceptionName, typeNamespace, members);

        writeMembers(cls, members);

        seiContext.addExceptionBeanEntry(thrownDecl.getQualifiedName(), faultInfo, builder);
        return true;
    }

    protected boolean isWSDLException(Map<String,MethodDeclaration> map, ClassDeclaration thrownDecl) {
        WebFault webFault = thrownDecl.getAnnotation(WebFault.class);
        if (webFault == null)
            return false;
        return !(map.size() != 2 || map.get(FAULT_INFO) == null);
    }

    private void writeXmlElementDeclaration(JDefinedClass cls, String elementName, String namespaceUri) {

       if (cls == null)
            return;
        JAnnotationUse xmlRootElementAnn = cls.annotate(XmlRootElement.class);
        xmlRootElementAnn.param("name", elementName);
        if (namespaceUri.length() > 0) {
            xmlRootElementAnn.param("namespace", namespaceUri);
        }
        JAnnotationUse xmlAccessorTypeAnn = cls.annotate(cm.ref(XmlAccessorType.class));
        xmlAccessorTypeAnn.param("value", XmlAccessType.FIELD);
    }

    private void writeXmlTypeDeclaration(JDefinedClass cls, String typeName, String namespaceUri,
                                         ArrayList<MemberInfo> members) {
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

    private void writeMember(JDefinedClass cls, TypeMirror paramType,
                             String paramName) {

        if (cls == null)
            return;

        String accessorName =JAXBRIContext.mangleNameToPropertyName(paramName);
        String getterPrefix = paramType.toString().equals("boolean")? "is" : "get";
        JType propType = getType(paramType);
        JMethod m = cls.method(JMod.PUBLIC, propType, getterPrefix+ accessorName);
        JDocComment methodDoc = m.javadoc();
        JCommentPart ret = methodDoc.addReturn();
        ret.add("returns "+propType.name());
        JBlock body = m.body();
        body._return( JExpr._this().ref(paramName) );

        m = cls.method(JMod.PUBLIC, cm.VOID, "set"+accessorName);
        JVar param = m.param(propType, paramName);
        methodDoc = m.javadoc();
        JCommentPart part = methodDoc.addParam(paramName);
        part.add("the value for the "+ paramName+" property");
        body = m.body();
        body.assign( JExpr._this().ref(paramName), param );
    }
}      
    
