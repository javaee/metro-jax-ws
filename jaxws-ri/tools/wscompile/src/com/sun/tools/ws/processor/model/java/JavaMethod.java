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

package com.sun.tools.ws.processor.model.java;

import com.sun.tools.ws.resources.ModelMessages;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.processor.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * @author WS Development Team
 */
public class JavaMethod {

    private final ErrorReceiver errorReceiver;
    private final String name;
    private final List<JavaParameter> parameters = new ArrayList<JavaParameter>();
    private final List<String> exceptions = new ArrayList<String>();
    private final WsimportOptions options;
    private JavaType returnType;

    public JavaMethod(String name, WsimportOptions options, ErrorReceiver receiver) {
        this.name = name;
        this.returnType = null;
        this.errorReceiver = receiver;
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public void setReturnType(JavaType returnType) {
        this.returnType = returnType;
    }

    private boolean hasParameter(String paramName) {
        for (JavaParameter parameter : parameters) {
            if (paramName.equals(parameter.getName())) {
                return true;
            }
        }
        return false;
    }

    private Parameter getParameter(String paramName){
        for (JavaParameter parameter : parameters) {
            if (paramName.equals(parameter.getName())) {
                return parameter.getParameter();
            }
        }
        return null;
    }

    public void addParameter(JavaParameter param) {
        // verify that this member does not already exist
        if (hasParameter(param.getName())) {
            if(options.isExtensionMode()){
                param.setName(getUniqueName(param.getName()));
            }else{
                Parameter duplicParam = getParameter(param.getName());
                if(param.getParameter().isEmbedded()){
                    errorReceiver.error(param.getParameter().getLocator(), ModelMessages.MODEL_PARAMETER_NOTUNIQUE_WRAPPER(param.getName(), param.getParameter().getEntityName()));
                    errorReceiver.error(duplicParam.getLocator(), ModelMessages.MODEL_PARAMETER_NOTUNIQUE_WRAPPER(param.getName(), duplicParam.getEntityName()));                    
                }else{
                    errorReceiver.error(param.getParameter().getLocator(), ModelMessages.MODEL_PARAMETER_NOTUNIQUE(param.getName(), param.getParameter().getEntityName()));
                    errorReceiver.error(duplicParam.getLocator(), ModelMessages.MODEL_PARAMETER_NOTUNIQUE(param.getName(), duplicParam.getEntityName()));
                }
                return;
            }
        }
        parameters.add(param);
    }

    public List<JavaParameter> getParametersList() {
        return parameters;
    }

    public void addException(String exception) {
        // verify that this exception does not already exist
        if (!exceptions.contains(exception)) {
            exceptions.add(exception);
        }
    }

    /** TODO: NB uses it, remove it once we expose it thru some API **/
    public Iterator<String> getExceptions() {
        return exceptions.iterator();
    }

    private String getUniqueName(String param){
        int parmNum = 0;
        while(hasParameter(param)){
            param = param + Integer.toString(parmNum++);
        }
        return param;
    }
}
