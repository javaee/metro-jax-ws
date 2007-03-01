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
package com.sun.xml.ws.api.model;
/**
 * Type of java exception as defined by JAXWS 2.0 JSR 224.
 *
 * Tells whether the exception class is a userdefined or a WSDL exception.
 * A WSDL exception class follows the pattern defined in JSR 224. According to that
 * a WSDL exception class must have:
 *
 * <code>public WrapperException()String message, FaultBean){}</code>
 *
 * and accessor method
 *
 * <code>public FaultBean getFaultInfo();</code>
 * 
 * @author Vivek Pandey
 */
public enum ExceptionType {
    WSDLException(0), UserDefined(1);

    ExceptionType(int exceptionType){
        this.exceptionType = exceptionType;
    }

    public int value() {
        return exceptionType;
    }
    private final int exceptionType;
}
