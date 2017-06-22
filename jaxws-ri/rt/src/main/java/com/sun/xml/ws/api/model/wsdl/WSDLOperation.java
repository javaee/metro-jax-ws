/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.api.model.wsdl;

import com.sun.xml.ws.api.model.wsdl.WSDLExtensible;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.namespace.QName;

/**
 * Provides abstraction of wsdl:portType/wsdl:operation.
 *
 * @author Vivek Pandey
 */
public interface WSDLOperation extends WSDLObject, WSDLExtensible {
    /**
     * Gets the name of the wsdl:portType/wsdl:operation@name attribute value as local name and wsdl:definitions@targetNamespace
     * as the namespace uri.
     */
    @NotNull QName getName();

    /**
     * Gets the wsdl:input of this operation
     */
    @NotNull WSDLInput getInput();

    /**
     * Gets the wsdl:output of this operation.
     *
     * @return
     *      null if this is an one-way operation.
     */
    @Nullable WSDLOutput getOutput();

    /**
     * Returns true if this operation is an one-way operation.
     */
    boolean isOneWay();

    /**
     * Gets the {@link WSDLFault} corresponding to wsdl:fault of this operation.
     */
    Iterable<? extends WSDLFault> getFaults();

    /**
     * Gives {@link WSDLFault} for the given soap fault detail value.
     *
     * <pre>
     *
     * Given a wsdl fault:
     *
     * &lt;wsdl:message nae="faultMessage">
     *  &lt;wsdl:part name="fault" element="<b>ns:myException</b>/>
     * &lt;/wsdl:message>
     *
     * &lt;wsdl:portType>
     *  &lt;wsdl:operation ...>
     *      &lt;wsdl:fault name="aFault" message="faultMessage"/>
     *  &lt;/wsdl:operation>
     * &lt;wsdl:portType>
     *
     *
     * For example given a soap 11 soap message:
     *
     * &lt;soapenv:Fault>
     *      ...
     *      &lt;soapenv:detail>
     *          &lt;<b>ns:myException</b>>
     *              ...
     *          &lt;/ns:myException>
     *      &lt;/soapenv:detail>
     *
     * QName faultQName = new QName(ns, "myException");
     * WSDLFault wsdlFault  = getFault(faultQName);
     *
     * The above call will return a WSDLFault that abstracts wsdl:portType/wsdl:operation/wsdl:fault.
     *
     * </pre>
     *
     * @param faultDetailName tag name of the element inside soaenv:Fault/detail/, must be non-null.
     * @return returns null if a wsdl fault corresponding to the detail entry name not found.
     */
    @Nullable WSDLFault getFault(QName faultDetailName);

    /**
     * Gives the enclosing wsdl:portType@name attribute value.
     */
    @NotNull QName getPortTypeName();
    
    /**
     * Returns parameter order
     * @return Parameter order
     */
    public String getParameterOrder();
}
