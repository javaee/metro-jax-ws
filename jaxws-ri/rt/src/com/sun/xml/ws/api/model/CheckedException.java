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
