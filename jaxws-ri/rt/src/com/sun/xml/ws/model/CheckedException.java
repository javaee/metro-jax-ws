/**
 * $Id: CheckedException.java,v 1.4 2005-09-10 19:47:49 kohsuke Exp $
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

package com.sun.xml.ws.model;

import com.sun.xml.bind.api.TypeReference;
import java.lang.reflect.Type; 

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

public class CheckedException {
    /**
     * @param exceptionClass
     *            Userdefined or WSDL exception class that extends
     *            java.lang.Exception.
     * @param detail
     *            detail or exception bean's TypeReference
     * @param exceptionType
     *            either ExceptionType.UserDefined or
     *            ExceptionType.WSDLException
     */
    public CheckedException(Class exceptionClass, TypeReference detail, ExceptionType exceptionType) {        
        this.detail = detail;
        this.exceptionType = exceptionType;
        this.exceptionClass = exceptionClass;
    }

    /**
     * @return the <code>Class</clode> for this object
     * 
     */
    public Class getExcpetionClass() {
        return exceptionClass;
    }

    public Class getDetailBean() {
        return (Class) detail.type;
    }

    public TypeReference getDetailType() {
        return detail;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public void setHeaderFault(boolean hf) {
        this.headerFault = hf;
    }

    public boolean isHeaderFault() {
        return headerFault;
    }

    private Class exceptionClass;

    private TypeReference detail;

    private ExceptionType exceptionType;

    private boolean headerFault = false;
}
