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

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.util.Pool;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Provider;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Represents abstraction of SEI.
 *
 * <p>
 * This interface would be used to access which Java concepts correspond to
 * which WSDL concepts, such as which <code>wsdl:port</code> corresponds to
 * a SEI, or which <code>wsdl:operation</code> corresponds to {@link JavaMethod}.
 *
 * <P>
 * It also retains information about the databinding done for a SEI;
 * such as {@link JAXBRIContext} and {@link Bridge}.
 *
 * <p>
 * This model is constructed only when there is a Java SEI. Therefore it's
 * not available with {@link Dispatch} or {@link Provider}. Technologies that
 * need to work regardless of such surface API difference shall not be using
 * this model.
 *
 * @author Vivek Pandey
 */
public interface SEIModel {
    Pool.Marshaller getMarshallerPool();

    /**
     * JAXBContext that will be used to marshall/unmarshall the java classes found in the SEI.
     *
     * @return the <code>{@link JAXBRIContext}</code>
     * @deprecated Why do you need this?
     */
    JAXBContext getJAXBContext();

    /**
     * Get the Bridge associated with the {@link TypeReference}
     *
     * @param type
     * @return the <code>{@link Bridge}</code> for the <code>type</code>
     */
//    Bridge getBridge(TypeReference type);

    /**
     * Its a known fault if the exception thrown by {@link Method} is annotated with the
     * {@link javax.xml.ws.WebFault#name()} thas equal to the name passed as parameter.
     *
     * @param name   is the qualified name of fault detail element as specified by wsdl:fault@element value.
     * @param method is the Java {@link Method}
     * @return true if <code>name</code> is the name
     *         of a known fault name for the <code>method</code>
     */
//    boolean isKnownFault(QName name, Method method);

    /**
     * Checks if the {@link JavaMethod} for the {@link Method} knowns the exception class.
     *
     * @param m  {@link Method} to pickup the right {@link JavaMethod} model
     * @param ex exception class
     * @return true if <code>ex</code> is a Checked Exception
     *         for <code>m</code>
     */
//    boolean isCheckedException(Method m, Class ex);

    /**
     * This method will be useful to get the {@link JavaMethod} corrrespondiong to
     * a {@link Method} - such as on the client side.
     *
     * @param method for which {@link JavaMethod} is asked for
     * @return the {@link JavaMethod} representing the <code>method</code>
     */
    JavaMethod getJavaMethod(Method method);

    /**
     * Gives a {@link JavaMethod} for a given {@link QName}. The {@link QName} will
     * be equivalent to the SOAP Body or Header block or can simply be the name of an
     * infoset that corresponds to the payload.
     * @param name
     * @return the <code>JavaMethod</code> associated with the
     *         operation named name
     */
    public JavaMethod getJavaMethod(QName name);

    /**
     * Gives the JavaMethod associated with the wsdl operation
     * @param operationName QName of the wsdl operation
     * @return
     */
    public JavaMethod getJavaMethodForWsdlOperation(QName operationName);


    /**
     * Gives all the {@link JavaMethod} for a wsdl:port for which this {@link SEIModel} is
     * created.
     *
     * @return a {@link Collection} of {@link JavaMethod}
     *         associated with the {@link SEIModel}
     */
    Collection<? extends JavaMethod> getJavaMethods();

    /**
     * Location of the WSDL that defines the port associated with the {@link SEIModel}
     *
     * @return wsdl location uri - always non-null
     */
    @NotNull String getWSDLLocation();

    /**
     * wsdl:service qualified name for the port associated with the {@link SEIModel)
     *
     * @return wsdl:service@name value - always non-null
     */
    @NotNull QName getServiceQName();

    /**
     * Gets the {@link WSDLPort} that represents the port that this SEI binds to.
     */
    @NotNull WSDLPort getPort();

    /**
     * Value of the wsdl:port name associated with the {@link SEIModel)
     *
     * @return wsdl:service/wsdl:port@name value, always non-null
     */
    @NotNull QName getPortName();

    /**
     * Value of wsdl:portType bound to the port associated with the {@link SEIModel)
     *
     * @return
     */
    @NotNull QName getPortTypeName();

    /**
     *  Gives the wsdl:binding@name value
     */
    @NotNull QName getBoundPortTypeName();

    /**
     * Namespace of the wsd;:port associated with the {@link SEIModel)
     */
    @NotNull String getTargetNamespace();
}
