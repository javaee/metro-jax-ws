/**
 * $Id: CheckedException.java,v 1.1 2005-05-23 22:42:09 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.model;

import com.sun.xml.bind.api.TypeReference;
import java.lang.reflect.Type; 

/**
 * @author Vivek Pandey
 * 
 * CheckedException class. Holds the exception class - class that has public
 * constructor
 * 
 * <code>public WrapperException()String message, FaultBean){}</code>
 * 
 * and method
 * 
 * <code>public FaultBean getFaultInfo();</code>
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
     * @return
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
