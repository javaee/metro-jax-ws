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

import com.sun.xml.bind.api.Bridge;

import javax.xml.ws.WebFault;
import javax.xml.namespace.QName;
import java.rmi.RemoteException;

/**
 * This class provides abstractio to the  the exception class
 * corresponding to the wsdl:fault, such as class MUST have
 * {@link WebFault} annotation defined on it.
 *
 * Also the exception class must have
 *
 * <code>public WrapperException()String message, FaultBean){}</code>
 *
 * and method
 *
 * <code>public FaultBean getFaultInfo();</code>
 *
 * @author Vivek Pandey
 */
public interface CheckedException {
    /**
     * Gets the root {@link SEIModel} that owns this model.
     */
    SEIModel getOwner();

    /**
     * Gets the parent {@link JavaMethod} to which this checked exception belongs.
     */
    JavaMethod getParent();

    /**
     * The returned exception class would be userdefined or WSDL exception class.
     *
     * @return
     *      always non-null same object.
     */
    Class getExceptionClass();

    /**
     * The detail bean is serialized inside the detail entry in the SOAP message.
     * This must be known to the {@link javax.xml.bind.JAXBContext} inorder to get
     * marshalled/unmarshalled.
     *
     * @return the detail bean
     */
    Class getDetailBean();

    /**
     * Gives the {@link com.sun.xml.bind.api.Bridge} associated with the detail
     */
    Bridge getBridge();

    /**
     * Tells whether the exception class is a userdefined or a WSDL exception.
     * A WSDL exception class follows the pattern defined in JSR 224. According to that
     * a WSDL exception class must have:
     *
     * <code>public WrapperException()String message, FaultBean){}</code>
     *
     * and accessor method
     *
     * <code>public FaultBean getFaultInfo();</code>     
     */
    ExceptionType getExceptionType();

    /**
     * Gives the wsdl:portType/wsdl:operation/wsdl:fault@message value - that is the wsdl:message
     * referenced by wsdl:fault
     */
    String getMessageName();
}
