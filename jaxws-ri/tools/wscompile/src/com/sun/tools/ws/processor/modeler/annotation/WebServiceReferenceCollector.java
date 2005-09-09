/**
 * $Id: WebServiceReferenceCollector.java,v 1.9 2005-09-09 05:50:17 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import com.sun.mirror.util.*;

import com.sun.tools.xjc.api.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.sun.tools.ws.processor.generator.GeneratorConstants;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.StringUtils;
import com.sun.xml.ws.util.Version;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.tools.ws.util.ClassNameInfo;
import javax.jws.soap.SOAPBinding.Style;

import javax.xml.namespace.QName;

import javax.xml.ws.WebFault;
import javax.jws.*;
import javax.jws.soap.*;
import com.sun.tools.ws.processor.modeler.annotation.*;

/**
 *
 * @author  WS Development Team
 */
public class WebServiceReferenceCollector extends WebServiceVisitor {
    protected Set<String> wrapperNames;
    protected Set<String> processedExceptions;

    public WebServiceReferenceCollector(ModelBuilder builder, AnnotationProcessorContext context) {
        super(builder, context);
    }
 
    protected boolean shouldProcessWebService(WebService webService, InterfaceDeclaration intf) { 
        if (webService == null)
            builder.onError(intf.getPosition(), "webserviceap.endpointinterface.has.no.webservice.annotation", 
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
        if (webMethod != null && webMethod.exclude())
            return false;
        
//        return !hasWebMethods || webMethod != null;
        return webMethod != null || endpointReferencesInterface ||
                method.getDeclaringType().equals(typeDecl) || 
                (method.getDeclaringType().getAnnotation(WebService.class) != null);
    }
    
    protected void processMethod(MethodDeclaration method, WebMethod webMethod) {
        boolean isOneway = method.getAnnotation(Oneway.class) != null;
        boolean generatedWrapper = false;
//        builder.log("WebServiceReferenceCollector - method: "+method);
//        builder.log("method.getDeclaringType(): "+method.getDeclaringType());            
        SOAPBinding soapBinding = method.getAnnotation(SOAPBinding.class);
        if (soapBinding == null && !method.getDeclaringType().equals(typeDecl)) {
            if (method.getDeclaringType() instanceof ClassDeclaration) {
                soapBinding = method.getDeclaringType().getAnnotation(SOAPBinding.class);            
                if (soapBinding != null)
                    builder.log("using "+method.getDeclaringType()+"'s SOAPBinding.");            
                else {
                    soapBinding = new MySOAPBinding();
                }
            }
        }    
        
        boolean newBinding = false;
        if (soapBinding != null) {
            if (soapBinding.style().equals(Style.RPC)) {
                builder.onError(method.getPosition(), "webserviceap.rpc.soapbinding.not.allowed.on.method",
                        new Object[] {typeDecl.getQualifiedName(), method.toString()});
            }
            
            newBinding = pushSOAPBinding(soapBinding, method, typeDecl);
        }
        try {
            collectTypes(method, webMethod, seiContext.getReqOperationWrapper(method) != null);
            if (seiContext.getReqOperationWrapper(method) != null) {
                AnnotationProcessorEnvironment apEnv = builder.getAPEnv();
                TypeDeclaration typeDecl;
                typeDecl = builder.getTypeDeclaration(seiContext.getReqOperationWrapper(method).getWrapperName());
                seiContext.addReference(typeDecl, apEnv);
                if (!isOneway) {
                    typeDecl = builder.getTypeDeclaration(seiContext.getResOperationWrapper(method).getWrapperName());
                    seiContext.addReference(typeDecl, apEnv);                
                }
            }
            collectExceptionBeans(method);
        } finally {
            if (newBinding) {
                popSOAPBinding();
            }
        }
    }

    private void collectTypes(MethodDeclaration method, WebMethod webMethod, boolean isDocLitWrapped) {
        addSchemaElements(method, isDocLitWrapped);
    }        
    
    private void collectExceptionBeans(MethodDeclaration method) {
        AnnotationProcessorEnvironment apEnv = builder.getAPEnv();
        for (ReferenceType thrownType : method.getThrownTypes()) {
            FaultInfo faultInfo = seiContext.getExceptionBeanName(thrownType.toString());
            if (faultInfo != null) {
                if (!faultInfo.isWSDLException()) {
                    seiContext.addReference(builder.getTypeDeclaration(faultInfo.getBeanName()), apEnv);            
                } else {
                    TypeMirror bean = faultInfo.beanTypeMoniker.create(apEnv);
                    Reference ref = seiContext.addReference(((DeclaredType)bean).getDeclaration(), apEnv);
                    seiContext.addSchemaElement(faultInfo.getElementName(), ref);
                }
            }
        }
    }
}      
    
