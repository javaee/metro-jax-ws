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
package com.sun.xml.ws.model;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.logging.Logger;

/**
 * Finds request/response wrapper and exception bean memebers.
 *
 * <p>
 * It uses JAXB's {@link AnnotationReader}, {@link Navigator} so that
 * tools can use this with APT, and the runtime can use this with
 * reflection.
 *
 * @author Jitendra Kotamraju
 */
public abstract class AbstractWrapperBeanGenerator<T,M,A> {

    private static final Logger LOGGER = Logger.getLogger(AbstractWrapperBeanGenerator.class.getName());

    private static final String RETURN = "return";
    private static final String RETURN_VALUE = "_return";
    private static final String EMTPY_NAMESPACE_ID = "";

    private static final Class[] jaxbAnns = new Class[] {
        XmlAttachmentRef.class, XmlMimeType.class, XmlJavaTypeAdapter.class,
        XmlList.class, XmlElement.class
    };

    private final AnnotationReader<T,?,?,M> annReader;
    private final Navigator<T,?,?,M> nav;
    private final BeanMemberFactory<T,A> factory;

    protected AbstractWrapperBeanGenerator(AnnotationReader<T,?,?,M> annReader,
            Navigator<T,?,?,M> nav, BeanMemberFactory<T,A> factory) {
        this.annReader = annReader;
        this.nav = nav;
        this.factory = factory;
    }

    public static interface BeanMemberFactory<T,A> {
        A createWrapperBeanMember(T paramType, String paramName,
            QName elementName, List<Annotation> jaxbAnnotations);
    }

    // Collects the JAXB annotations on a method
    private List<Annotation> collectJAXBAnnotations(M method) {
        List<Annotation> jaxbAnnotation = new ArrayList<Annotation>();
        for(Class jaxbClass : jaxbAnns) {
            Annotation ann = annReader.getMethodAnnotation(jaxbClass, method, null);
            if (ann != null) {
                jaxbAnnotation.add(ann);
            }
        }
        return jaxbAnnotation;
    }

    // Collects the JAXB annotations on a parameter
    private List<Annotation> collectJAXBAnnotations(M method, int paramIndex) {
        List<Annotation> jaxbAnnotation = new ArrayList<Annotation>();
        for(Class jaxbClass : jaxbAnns) {
            Annotation ann = annReader.getMethodParameterAnnotation(jaxbClass, method, paramIndex, null);
            if (ann != null) {
                jaxbAnnotation.add(ann);
            }
        }
        return jaxbAnnotation;
    }

    protected abstract T getSafeType(T type);
    protected abstract T getHolderValueType(T type);
    protected abstract boolean isVoidType(T type);

    protected void collectWrapperBeanMembers(M method, boolean wrapped,
        String typeNamespace, List<A> requestMembers, List<A> responseMembers) {

        List<Annotation> jaxbRespAnnotations = collectJAXBAnnotations(method);
        String responseElementName = RETURN;
        String responseName = RETURN_VALUE;
        String responseNamespace = wrapped ? EMTPY_NAMESPACE_ID : typeNamespace;
        boolean isResultHeader = false;
        WebResult webResult = annReader.getMethodAnnotation(WebResult.class, method ,null);
        if (webResult != null) {
            if (webResult.name().length() > 0) {
                responseElementName = webResult.name();
                responseName = JAXBRIContext.mangleNameToVariableName(webResult.name());

                //We wont have to do this if JAXBRIContext.mangleNameToVariableName() takes
                //care of mangling java identifiers
                responseName = getJavaReservedVarialbeName(responseName);
            }
            responseNamespace = webResult.targetNamespace().length() > 1 ?
                webResult.targetNamespace() :
                responseNamespace;
            isResultHeader = webResult.header();
        }

        T returnType = getSafeType(nav.getReturnType(method));
        // TODO shouldn't we use isVoidType(returnType) in the following ??
        if (!isVoidType(nav.getReturnType(method)) && !isResultHeader) {
            responseMembers.add(factory.createWrapperBeanMember(returnType, responseName,
                new QName(responseNamespace, responseElementName), jaxbRespAnnotations));
        }

        int paramIndex = -1;

        for (T param : nav.getMethodParameters(method)) {
            paramIndex++;
            List<Annotation> jaxbAnnotation = collectJAXBAnnotations(method, paramIndex);
            WebParam.Mode mode = null;
            T holderType = getHolderValueType(param);
            WebParam webParam = annReader.getMethodParameterAnnotation(WebParam.class, method, paramIndex, null);
            T paramType = getSafeType(param);
            String paramNamespace = wrapped ? EMTPY_NAMESPACE_ID : typeNamespace;
            if (holderType != null) {
                paramType = holderType;
            }
            String paramName =  "arg"+paramIndex;
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
            propertyName = getJavaReservedVarialbeName(propertyName);

            A member = factory.createWrapperBeanMember(paramType, propertyName,
                new QName(paramNamespace, paramName), jaxbAnnotation);
            if (holderType != null) {
                if (mode == null || mode.equals(WebParam.Mode.INOUT)) {
                    requestMembers.add(member);
                }
                responseMembers.add(member);
            } else {
                requestMembers.add(member);
            }
        }
    }

    //TODO MOVE Names.java to runtime (instead of doing the following)
    /*
     * See if its a java keyword name, if so then mangle the name
     */
    private static @NotNull String getJavaReservedVarialbeName(@NotNull String name) {
        String reservedName = reservedWords.get(name);
        return reservedName == null ? name : reservedName;
    }

    private static final Map<String, String> reservedWords;

    static {
        reservedWords = new HashMap<String, String>();
        reservedWords.put("abstract", "_abstract");
        reservedWords.put("assert", "_assert");
        reservedWords.put("boolean", "_boolean");
        reservedWords.put("break", "_break");
        reservedWords.put("byte", "_byte");
        reservedWords.put("case", "_case");
        reservedWords.put("catch", "_catch");
        reservedWords.put("char", "_char");
        reservedWords.put("class", "_class");
        reservedWords.put("const", "_const");
        reservedWords.put("continue", "_continue");
        reservedWords.put("default", "_default");
        reservedWords.put("do", "_do");
        reservedWords.put("double", "_double");
        reservedWords.put("else", "_else");
        reservedWords.put("extends", "_extends");
        reservedWords.put("false", "_false");
        reservedWords.put("final", "_final");
        reservedWords.put("finally", "_finally");
        reservedWords.put("float", "_float");
        reservedWords.put("for", "_for");
        reservedWords.put("goto", "_goto");
        reservedWords.put("if", "_if");
        reservedWords.put("implements", "_implements");
        reservedWords.put("import", "_import");
        reservedWords.put("instanceof", "_instanceof");
        reservedWords.put("int", "_int");
        reservedWords.put("interface", "_interface");
        reservedWords.put("long", "_long");
        reservedWords.put("native", "_native");
        reservedWords.put("new", "_new");
        reservedWords.put("null", "_null");
        reservedWords.put("package", "_package");
        reservedWords.put("private", "_private");
        reservedWords.put("protected", "_protected");
        reservedWords.put("public", "_public");
        reservedWords.put("return", "_return");
        reservedWords.put("short", "_short");
        reservedWords.put("static", "_static");
        reservedWords.put("strictfp", "_strictfp");
        reservedWords.put("super", "_super");
        reservedWords.put("switch", "_switch");
        reservedWords.put("synchronized", "_synchronized");
        reservedWords.put("this", "_this");
        reservedWords.put("throw", "_throw");
        reservedWords.put("throws", "_throws");
        reservedWords.put("transient", "_transient");
        reservedWords.put("true", "_true");
        reservedWords.put("try", "_try");
        reservedWords.put("void", "_void");
        reservedWords.put("volatile", "_volatile");
        reservedWords.put("while", "_while");
        reservedWords.put("enum", "_enum");
    }

}