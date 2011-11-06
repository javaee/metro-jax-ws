/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.wsdl.document.soap.SOAPUse;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author  dkohlert
 */
public class AnnotationProcessorContext {

    private Map<Name, SeiContext> seiContextMap = new HashMap<Name, SeiContext>();
    private int round = 1;
    private boolean modelCompleted = false;

    public void addSeiContext(Name seiName, SeiContext seiContext) {
        seiContextMap.put(seiName, seiContext);
    }

    public SeiContext getSeiContext(Name seiName) {
        SeiContext context = seiContextMap.get(seiName);
        if (context == null) {
            context = new SeiContext(seiName);
            addSeiContext(seiName, context);
        }
        return context;
    }

    public SeiContext getSeiContext(TypeElement d) {
        return getSeiContext(d.getQualifiedName());
    }

    public Collection<SeiContext> getSeiContexts() {
        return seiContextMap.values();
    }

    public int getRound() {
        return round;
    }

    public void incrementRound() {
        round++;
    }

    public static boolean isEncoded(Model model) {
        if (model == null)
            return false;
        for (Service service : model.getServices()) {
            for (Port port : service.getPorts()) {
                for (Operation operation : port.getOperations()) {
                    if (operation.getUse() != null && operation.getUse().equals(SOAPUse.LITERAL))
                        return false;
                }
            }
        }
        return true;
    }

    public void setModelCompleted(boolean modelCompleted) {
        this.modelCompleted = modelCompleted;
    }

    public boolean isModelCompleted() {
        return modelCompleted;
    }

    public static class SeiContext {

        private Map<String, WrapperInfo> reqOperationWrapperMap = new HashMap<String, WrapperInfo>();
        private Map<String, WrapperInfo> resOperationWrapperMap = new HashMap<String, WrapperInfo>();
        private Map<Name, FaultInfo> exceptionBeanMap = new HashMap<Name, FaultInfo>();

        private Name seiName;
        private Name seiImplName;
        private boolean implementsSei;
        private String namespaceUri;

        public SeiContext(Name seiName) {
            this.seiName = seiName;
        }

        public void setImplementsSei(boolean implementsSei) {
            this.implementsSei = implementsSei;
        }

        public boolean getImplementsSei() {
            return implementsSei;
        }

        public void setNamespaceUri(String namespaceUri) {
            this.namespaceUri = namespaceUri;
        }

        public String getNamespaceUri() {
            return namespaceUri;
        }

        public Name getSeiImplName() {
            return seiImplName;
        }

        public void setSeiImplName(Name implName) {
            seiImplName = implName;
        }

        public void setReqWrapperOperation(ExecutableElement method, WrapperInfo wrapperInfo) {
            reqOperationWrapperMap.put(methodToString(method), wrapperInfo);
        }

        public WrapperInfo getReqOperationWrapper(ExecutableElement method) {
            return reqOperationWrapperMap.get(methodToString(method));
        }

        public void setResWrapperOperation(ExecutableElement method, WrapperInfo wrapperInfo) {
            resOperationWrapperMap.put(methodToString(method), wrapperInfo);
        }

        public WrapperInfo getResOperationWrapper(ExecutableElement method) {
            return resOperationWrapperMap.get(methodToString(method));
        }

        public String methodToString(ExecutableElement method) {
            StringBuilder buf = new StringBuilder(method.getSimpleName());
            for (VariableElement param : method.getParameters())
                buf.append(';').append(param.asType());
            return buf.toString();
        }

        public void clearExceptionMap() {
            exceptionBeanMap.clear();
        }

        public void addExceptionBeanEntry(Name exception, FaultInfo faultInfo, ModelBuilder builder) {
            exceptionBeanMap.put(exception, faultInfo);
        }

        public FaultInfo getExceptionBeanName(Name exception) {
            return exceptionBeanMap.get(exception);
        }
    }
}
