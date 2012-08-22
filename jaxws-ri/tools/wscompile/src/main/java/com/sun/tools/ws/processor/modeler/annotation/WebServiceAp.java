/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.tools.ws.processor.generator.GeneratorUtil;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.resources.WebserviceapMessages;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.wscompile.WsgenOptions;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.jws.WebService;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * WebServiceAp is a AnnotationProcessor for processing javax.jws.* and
 * javax.xml.ws.* annotations. This class is used either by the WsGen (CompileTool) tool or
 * indirectly when invoked by javac.
 *
 * @author WS Development Team
 */
@SupportedAnnotationTypes({
        "javax.jws.HandlerChain",
        "javax.jws.Oneway",
        "javax.jws.WebMethod",
        "javax.jws.WebParam",
        "javax.jws.WebResult",
        "javax.jws.WebService",
        "javax.jws.soap.InitParam",
        "javax.jws.soap.SOAPBinding",
        "javax.jws.soap.SOAPMessageHandler",
        "javax.jws.soap.SOAPMessageHandlers",
        "javax.xml.ws.BindingType",
        "javax.xml.ws.RequestWrapper",
        "javax.xml.ws.ResponseWrapper",
        "javax.xml.ws.ServiceMode",
        "javax.xml.ws.WebEndpoint",
        "javax.xml.ws.WebFault",
        "javax.xml.ws.WebServiceClient",
        "javax.xml.ws.WebServiceProvider",
        "javax.xml.ws.WebServiceRef"
})
@SupportedOptions({WebServiceAp.DO_NOT_OVERWRITE, WebServiceAp.IGNORE_NO_WEB_SERVICE_FOUND_WARNING})
public class WebServiceAp extends AbstractProcessor implements ModelBuilder {

    public static final String DO_NOT_OVERWRITE = "doNotOverWrite";
    public static final String IGNORE_NO_WEB_SERVICE_FOUND_WARNING = "ignoreNoWebServiceFoundWarning";

    private WsgenOptions options;
    protected AnnotationProcessorContext context;
    private File sourceDir;
    private boolean doNotOverWrite;
    private boolean ignoreNoWebServiceFoundWarning = false;
    private TypeElement remoteElement;
    private TypeMirror remoteExceptionElement;
    private TypeMirror exceptionElement;
    private TypeMirror runtimeExceptionElement;
    private TypeElement defHolderElement;
    private boolean isCommandLineInvocation;
    private PrintStream out;
    private Collection<TypeElement> processedTypeElements = new HashSet<TypeElement>();

    public WebServiceAp() {
        this.context = new AnnotationProcessorContext();
    }

    public WebServiceAp(WsgenOptions options, PrintStream out) {
        this.options = options;
        this.sourceDir = (options != null) ? options.sourceDir : null;
        this.doNotOverWrite = (options != null) && options.doNotOverWrite;
        this.context = new AnnotationProcessorContext();
        this.out = out;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        remoteElement = processingEnv.getElementUtils().getTypeElement(Remote.class.getName());
        remoteExceptionElement = processingEnv.getElementUtils().getTypeElement(RemoteException.class.getName()).asType();
        exceptionElement = processingEnv.getElementUtils().getTypeElement(Exception.class.getName()).asType();
        runtimeExceptionElement = processingEnv.getElementUtils().getTypeElement(RuntimeException.class.getName()).asType();
        defHolderElement = processingEnv.getElementUtils().getTypeElement(Holder.class.getName());
        if (options == null) {
            options = new WsgenOptions();

            out = new PrintStream(new ByteArrayOutputStream());

            doNotOverWrite = getOption(DO_NOT_OVERWRITE);
            ignoreNoWebServiceFoundWarning = getOption(IGNORE_NO_WEB_SERVICE_FOUND_WARNING);

            String classDir = ".";
            String property = System.getProperty("sun.java.command");
            if (property != null) {
                Scanner scanner = new Scanner(property);
                boolean sourceDirNext = false;
                while (scanner.hasNext()) {
                    String token = scanner.next();
                    if (sourceDirNext) {
                        classDir = token;
                        sourceDirNext = false;
                    } else if ("-verbose".equals(token)) {
                        options.verbose = true;
                    } else if ("-s".equals(token)) {
                        sourceDirNext = true;
                    }
                }
            }
            sourceDir = new File(classDir);
            property = System.getProperty("java.class.path");
            options.classpath = classDir + File.pathSeparator + (property != null ? property : "");
            isCommandLineInvocation = true;
        }
        options.filer = processingEnv.getFiler();
    }

    private boolean getOption(String key) {
        String value = processingEnv.getOptions().get(key);
        if (value != null) {
            return Boolean.valueOf(value);
        }
        return false;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (context.getRound() != 1) {
            return true;
        }
        context.incrementRound();
        WebService webService;
        WebServiceProvider webServiceProvider;
        WebServiceVisitor webServiceVisitor = new WebServiceWrapperGenerator(this, context);
        boolean processedEndpoint = false;
        Collection<TypeElement> classes = new ArrayList<TypeElement>();
        filterClasses(classes, roundEnv.getRootElements());
        for (TypeElement element : classes) {
            webServiceProvider = element.getAnnotation(WebServiceProvider.class);
            webService = element.getAnnotation(WebService.class);
            if (webServiceProvider != null) {
                if (webService != null) {
                    processError(WebserviceapMessages.WEBSERVICEAP_WEBSERVICE_AND_WEBSERVICEPROVIDER(element.getQualifiedName()));
                }
                processedEndpoint = true;
            }

            if (webService == null) {
                continue;
            }

            element.accept(webServiceVisitor, null);
            processedEndpoint = true;
        }
        if (!processedEndpoint) {
            if (isCommandLineInvocation) {
                if (!ignoreNoWebServiceFoundWarning)
                    processWarning(WebserviceapMessages.WEBSERVICEAP_NO_WEBSERVICE_ENDPOINT_FOUND());
            } else {
                processError(WebserviceapMessages.WEBSERVICEAP_NO_WEBSERVICE_ENDPOINT_FOUND());
            }
        }
        return true;
    }

    private void filterClasses(Collection<TypeElement> classes, Collection<? extends Element> elements) {
        for (Element element : elements) {
            if (element.getKind().equals(ElementKind.CLASS)) {
                classes.add((TypeElement) element);
                filterClasses(classes, ElementFilter.typesIn(element.getEnclosedElements()));
            }
        }
    }

    @Override
    public void processWarning(String message) {
        if (isCommandLineInvocation) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
        } else {
            report(message);
        }
    }

    protected void report(String msg) {
        PrintStream outStream = out != null ? out : new PrintStream(out, true);
        outStream.println(msg);
        outStream.flush();
    }

    @Override
    public void processError(String message) {
        if (isCommandLineInvocation) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
            throw new AbortException();
        } else {
            throw new ModelerException(message);
        }
    }

    @Override
    public void processError(String message, Element element) {
        if (isCommandLineInvocation) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
        } else {
            throw new ModelerException(message);
        }
    }

    @Override
    public boolean canOverWriteClass(String className) {
        return !((doNotOverWrite && GeneratorUtil.classExists(options, className)));
    }

    @Override
    public File getSourceDir() {
        return sourceDir;
    }

    @Override
    public boolean isRemote(TypeElement typeElement) {
        return processingEnv.getTypeUtils().isSubtype(typeElement.asType(), remoteElement.asType());
    }

    @Override
    public boolean isServiceException(TypeMirror typeMirror) {
        return processingEnv.getTypeUtils().isSubtype(typeMirror, exceptionElement)
                && !processingEnv.getTypeUtils().isSubtype(typeMirror, runtimeExceptionElement)
                && !processingEnv.getTypeUtils().isSubtype(typeMirror, remoteExceptionElement);
    }

    @Override
    public TypeMirror getHolderValueType(TypeMirror type) {
        return TypeModeler.getHolderValueType(type, defHolderElement, processingEnv);
    }

    @Override
    public boolean checkAndSetProcessed(TypeElement typeElement) {
        if (!processedTypeElements.contains(typeElement)) {
            processedTypeElements.add(typeElement);
            return false;
        }
        return true;
    }

    @Override
    public void log(String message) {
        if (options != null && options.verbose) {
            message = new StringBuilder().append('[').append(message).append(']').toString(); // "[%s]"
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
        }
    }

    @Override
    public WsgenOptions getOptions() {
        return options;
    }

    @Override
    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnv;
    }

    @Override
    public String getOperationName(Name messageName) {
        return messageName != null ? messageName.toString() : null;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        if (SourceVersion.latest().compareTo(SourceVersion.RELEASE_6) > 0)
            return SourceVersion.valueOf("RELEASE_7");
        else
            return SourceVersion.RELEASE_6;
    }
}



