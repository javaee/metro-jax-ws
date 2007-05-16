/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
                errorReceiver.error(param.getParameter().getLocator(), ModelMessages.MODEL_PARAMETER_NOTUNIQUE(param.getName(), param.getParameter().getEntityName()));
                Parameter duplicParam = getParameter(param.getName());
                errorReceiver.error(duplicParam.getLocator(), ModelMessages.MODEL_PARAMETER_NOTUNIQUE(param.getName(), duplicParam.getEntityName()));
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
