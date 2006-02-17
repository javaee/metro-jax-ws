/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.tools.ws.processor.modeler.annotation;

import static com.sun.codemodel.ClassType.CLASS;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.writer.ProgressCodeWriter;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.VoidType;

import com.sun.tools.ws.processor.generator.GeneratorBase;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.tools.ws.wscompile.FilerCodeWriter;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.ToolVersion;
import com.sun.xml.ws.util.StringUtils;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


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
    protected MakeSafeTypeVisitor makeSafeVisitor;


    public WebServiceWrapperGenerator(ModelBuilder builder, AnnotationProcessorContext context) {
        super(builder, context);
        makeSafeVisitor = new MakeSafeTypeVisitor();
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
            throw new ModelerException("modeler.nestedGeneratorError",e);
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
            throw new ModelerException("modeler.nestedGeneratorError",e);
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
            String fieldName = field.getSimpleName();
//            String typeName = field.getType().toString();
            String elementName = xmlElement != null ? xmlElement.name() : fieldName;
            String namespace =  xmlElement != null ? xmlElement.namespace() : "";

            String idxStr = fieldName.substring(3);
            int index = Integer.parseInt(idxStr);
            member = new MemberInfo(index, field.getType(),
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
                           
        AnnotationProcessorEnvironment apEnv = builder.getAPEnv();
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
        TypeMirror paramType;
        String paramName;
        String paramNamespace;
        TypeMirror holderType;
        int paramIndex = -1;
//        System.out.println("method: "+method.toString());
//        System.out.println("returnType: "+ method.getReturnType());

//        TypeMirror typeMirror = apEnv.getTypeUtils().getErasure(method.getReturnType());
        TypeMirror typeMirror = getSafeType(method.getReturnType());
        String retType = typeMirror.toString();                
        if (!(method.getReturnType() instanceof VoidType) && !isResultHeader) {                    
            responseMembers.add(new MemberInfo(-1, typeMirror, RETURN_VALUE, 
                new QName(responseNamespace, responseElementName)));
        }

        for (ParameterDeclaration param : method.getParameters()) {
            WebParam.Mode mode = null;
            paramIndex++;
//            System.out.println("param.getType(): "+param.getType());
            holderType = builder.getHolderValueType(param.getType());
            webParam = param.getAnnotation(WebParam.class);
//            typeMirror = apEnv.getTypeUtils().getErasure(param.getType());            
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
            MemberInfo memInfo = new MemberInfo(paramIndex, paramType, paramName, 
                new QName(paramNamespace, paramName));
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
    
    private TypeMirror getSafeType(TypeMirror type) {
//        System.out.println("type: "+type+" type.getClass(): "+type.getClass());
        TypeMirror retType = makeSafeVisitor.apply(type, builder.getAPEnv().getTypeUtils());
//        System.out.println("retType: "+retType+" retType.getClass(): "+retType.getClass());
        return retType;
    }
       
    private JType getType(TypeMirror typeMirror) throws IOException {
        String type = typeMirror.toString();
        JType jType = null;
        try {
//            System.out.println("typeName: "+typeName);
            jType = cm.parseType(type);
//            System.out.println("type: "+type);
            return jType;
        } catch (ClassNotFoundException e) {
            jType = cm.ref(type);
        }
        return jType;
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
                    xmlElementAnn.param("namespace", elementName.getNamespaceURI());
                } else {
                    JAnnotationUse xmlValueAnnn = field.annotate(XmlValue.class);                    
                }
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
        AnnotationProcessorEnvironment apEnv = builder.getAPEnv();
        String exceptionName = ClassNameInfo.getName(thrownDecl.getQualifiedName());
        if (processedExceptions.contains(exceptionName))
            return false;
        processedExceptions.add(exceptionName);
        WebFault webFault = thrownDecl.getAnnotation(WebFault.class);
        String className = beanPackage+ exceptionName + BEAN;
        
        Map<String, TypeMirror> propertyToTypeMap;
        propertyToTypeMap = TypeModeler.getExceptionProperties(thrownDecl);
        boolean isWSDLException = isWSDLException(propertyToTypeMap, thrownDecl);
        String namespace = typeNamespace;
        String name = exceptionName;
        FaultInfo faultInfo;
        if (isWSDLException) {
            TypeMirror beanType =  getSafeType(propertyToTypeMap.get(FAULT_INFO));
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
            builder.onError("webserviceap.method.exception.bean.name.not.unique",
                             new Object[] {typeDecl.getQualifiedName(), thrownDecl.getQualifiedName()});                                                        
        }

        ArrayList<MemberInfo> members = new ArrayList<MemberInfo>();
        MemberInfo member;
        String typeString;
        TypeMirror erasureType;
        TreeSet<String> keys = new TreeSet<String>(propertyToTypeMap.keySet());
        for (String key : keys) {
            TypeMirror type = propertyToTypeMap.get(key);
            erasureType =  getSafeType(type);
            member = new MemberInfo(-10, erasureType, key, null);
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
    
    protected boolean isWSDLException(Map<String, TypeMirror>map, ClassDeclaration thrownDecl) {
        WebFault webFault = thrownDecl.getAnnotation(WebFault.class);
        if (webFault == null)
            return false;
        if (map.size() != 2 || map.get(FAULT_INFO) == null)
            return false;
        return true;
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

    private void writeMember(JDefinedClass cls, int paramIndex, TypeMirror paramType, 
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
    
