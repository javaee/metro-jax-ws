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

package com.sun.xml.ws.api.model;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.model.soap.SOAPBinding;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.jws.WebService;

/**
 * Abstracts the annotated {@link Method} of a SEI.
 *
 * @author Vivek Pandey
 */
public interface JavaMethod {

    /**
     * Gets the root {@link SEIModel} that owns this model.
     */
    SEIModel getOwner();

    /**
     * On the server side, it uses this for invocation of the web service
     *
     * <p>
     * {@literal @}{@link WebService}(endpointInterface="I")
     * class A { }
     *
     * In this case, it retuns A's method 
     *
     * <p>
     * {@literal @}{@link WebService}(endpointInterface="I")
     * class A implements I { }
     * In this case, it returns A's method
     *
     * <p>
     * {@literal @}{@link WebService}
     * class A { }
     * In this case, it returns A's method
     *  
     * @return Returns the java {@link Method}
     */
    @NotNull Method getMethod();


    /**
     * This should be used if you want to access annotations on WebMethod
     * Returns the SEI method if there is one.
     *
     * <p>
     * {@literal @}{@link WebService}(endpointInterface="I")
     * class A { }
     * In this case, it retuns I's method
     *
     * <p>
     * {@literal @}{@link WebService}(endpointInterface="I")
     * class A implements I { }
     * In this case, it returns I's method
     *
     * <p>
     * {@literal @}{@link WebService}
     * class A { }
     * In this case, it returns A's method
     *
     * @return Returns the java {@link Method}
     */
    @NotNull Method getSEIMethod();

    /**
     * @return Returns the {@link MEP}.
     */
    MEP getMEP();

    /**
     * Binding object - a {@link SOAPBinding} isntance.
     *
     * @return the Binding object
     */
    SOAPBinding getBinding();

    /**
     * Gives the wsdl:operation@name value
     */
    @NotNull String getOperationName();


    /**
     * Gives the request wsdl:message@name value
     */
    @NotNull String getRequestMessageName();

    /**
     * Gives the response wsdl:messageName value
     * @return null if its a oneway operation that is getMEP().isOneWay()==true.
     * @see com.sun.xml.ws.api.model.MEP#isOneWay()
     */
    @Nullable String getResponseMessageName();

    /**
     * Gives soap:Body's first child's name for request message.
     *
     * @return
     *      null if this operation doesn't have any request parameter bound to the body.
     */
    @Nullable QName getRequestPayloadName();

    /**
     * Gives soap:Body's first child's name for response message.
     *
     * @return
     *      null if this operation doesn't have any response parameter bound to the body.
     */
    @Nullable QName getResponsePayloadName();

    /**
     * Gives the checked Exception thrown from this method.
     * 
     * @return Returns the {@link CheckedException}.
     */
    Collection<? extends CheckedException> getCheckedExceptions();
}
