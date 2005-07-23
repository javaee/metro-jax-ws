/**
 * $Id: CheckedException.java,v 1.3 2005-07-23 04:10:08 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
