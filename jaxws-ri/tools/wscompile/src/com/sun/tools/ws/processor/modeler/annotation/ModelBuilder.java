/**
 * $Id: ModelBuilder.java,v 1.1 2005-05-23 23:23:50 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.modeler.annotation;

import java.io.File;

import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.localization.Localizable;

import java.net.URL;
import java.util.Properties;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;

import javax.xml.namespace.QName;
import com.sun.tools.ws.processor.modeler.annotation.*;

/**
 *
 * @author JAX-RPC Development Team
 */
public interface ModelBuilder {
//    public ClassLoader getClassLoader();
    public AnnotationProcessorEnvironment getAPEnv();
    public void createModel(TypeDeclaration d, QName modelName, String targetNamespace, String modelerClassName);
    public void setService(Service service);
    public void setPort(Port port);
//    public String getWsdlNamespaceURI();
//    public void setWsdlNamespaceURI(String namespace);
    public String getOperationName(String methodName);
    public String getResponseName(String operationName);
    public TypeMirror getHolderValueType(TypeMirror type);
//    public String getParamName(TypeMirror type, int pos);
//    public Properties getOptions();
    public boolean checkAndSetProcessed(TypeDeclaration typeDecl);
//    public URL findResource(String resourceName);
    public boolean isRemoteException(TypeDeclaration typeDecl);
    public boolean isRemote(TypeDeclaration typeDecl);
    public boolean canOverWriteClass(String className);
    public void setWrapperGenerated(boolean wrapperGenerated);
    public void setPortProperties(Port port);
    public TypeDeclaration getTypeDeclaration(String typeName);
//    public DeclaredType getDeclaredType(TypeDeclaration typeDecl);
//    public TypeMirror getTypeMirror(String typeName);
    public String getVersionString();
    public ProcessorEnvironment getProcessorEnvironment();
    public File getSourceDir();
    public String getXMLName(String javaName);
//    public QName getWSDLPortName(String portName);
//    public QName getWSDLBindingName(String portName);
//    public QName getWSDLPortTypeName(String portName);
//    public QName getWSDLInputMessageName(String operationName);
//    public QName getWSDLOutputMessageName(String operationName);
 //   public QName getWSDLFaultMessageName(String faultName);
    public void onError(String key);
    public void onError(String key, Object[] args) throws ModelerException;
    public void onError(Localizable msg) throws ModelerException;
    public void onWarning(Localizable msg);
    public void onInfo(Localizable msg);
    public void log(String msg);
}
