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
import com.sun.mirror.util.*;

import javax.xml.namespace.QName;
import com.sun.tools.ws.processor.modeler.annotation.*;

/**
 *
 * @author WS Development Team
 */
public interface ModelBuilder {
    public AnnotationProcessorEnvironment getAPEnv();
    public void createModel(TypeDeclaration d, QName modelName, String targetNamespace, String modelerClassName);
    public void setService(Service service);
    public void setPort(Port port);
    public String getOperationName(String methodName);
    public String getResponseName(String operationName);
    public TypeMirror getHolderValueType(TypeMirror type);
    public boolean checkAndSetProcessed(TypeDeclaration typeDecl);
    public boolean isRemoteException(TypeDeclaration typeDecl);
    public boolean isRemote(TypeDeclaration typeDecl);
    public boolean canOverWriteClass(String className);
    public void setWrapperGenerated(boolean wrapperGenerated);
    public TypeDeclaration getTypeDeclaration(String typeName);
    public String getSourceVersion();
    public ProcessorEnvironment getProcessorEnvironment();
    public File getSourceDir();
    public String getXMLName(String javaName);
    public void onError(String key);
    public void onError(String key, Object[] args) throws ModelerException;
    public void onError(SourcePosition srcPos, String key, Object[] args) throws ModelerException;
    public void onError(Localizable msg) throws ModelerException;
    public void onWarning(Localizable msg);
    public void onInfo(Localizable msg);
    public void log(String msg);
}
