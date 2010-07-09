/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.ExceptionType;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.addressing.WsaActionUtil;
import javax.xml.ws.WebServiceException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * CheckedException class. Holds the exception class - class that has public
 * constructor
 * 
 * <code>public WrapperException()String message, FaultBean){}</code>
 * 
 * and method
 * 
 * <code>public FaultBean getFaultInfo();</code>
 *
 * @author Vivek Pandey
 */
public final class CheckedExceptionImpl implements CheckedException {
    private final Class exceptionClass;
    private final TypeReference detail;
    private final ExceptionType exceptionType;
    private final JavaMethodImpl javaMethod;
    private String messageName;
    private String faultAction = "";

    /**
     * @param jm {@link JavaMethodImpl} that throws this exception
     * @param exceptionClass
     *            Userdefined or WSDL exception class that extends
     *            java.lang.Exception.
     * @param detail
     *            detail or exception bean's TypeReference
     * @param exceptionType
     *            either ExceptionType.UserDefined or
     */
    public CheckedExceptionImpl(JavaMethodImpl jm, Class exceptionClass, TypeReference detail, ExceptionType exceptionType) {
        this.detail = detail;
        this.exceptionType = exceptionType;
        this.exceptionClass = exceptionClass;
        this.javaMethod = jm;
    }

    public AbstractSEIModelImpl getOwner() {
        return javaMethod.owner;
    }

    public JavaMethod getParent() {
        return javaMethod;
    }

    /**
     * @return the <code>Class</clode> for this object
     * 
     */
    public Class getExceptionClass() {
        return exceptionClass;
    }

    public Class getDetailBean() {
        return (Class) detail.type;
    }

    public Bridge getBridge() {
        return getOwner().getBridge(detail);
    }

    public TypeReference getDetailType() {
        return detail;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }
    
    public String getMessageName() {
        return messageName;
    }
    
    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getFaultAction() {
        return faultAction;
    }

    public void setFaultAction(String faultAction) {
        this.faultAction = faultAction;
    }

    public String getDefaultFaultAction() {
        return WsaActionUtil.getDefaultFaultAction(javaMethod,this);  
    }


}
