/*
 * $Id: WebServiceReferenceCollector.java,v 1.16 2005-09-22 18:48:47 kohlert Exp $
 */
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
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
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

    public WebServiceReferenceCollector(ModelBuilder builder, AnnotationProcessorContext context) {
        super(builder, context);
    }
 
    
    protected void processWebService(WebService webService, TypeDeclaration d) {
    }
    
    protected void processMethod(MethodDeclaration method, WebMethod webMethod) {
        boolean isOneway = method.getAnnotation(Oneway.class) != null;
        boolean generatedWrapper = false;
        builder.log("WebServiceReferenceCollector - method: "+method);
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
    
