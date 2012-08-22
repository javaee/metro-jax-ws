/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.server.provider;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.spi.db.BindingHelper;

import javax.activation.DataSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * Keeps the runtime information like Service.Mode and erasure of Provider class
 * about Provider endpoint. It proccess annotations to find about Service.Mode
 * It also finds about parameterized type(e.g. Source, SOAPMessage, DataSource)
 * of endpoint class.
 *
 * @author Jitendra Kotamraju
 * @author Kohsuke Kawaguchi
 */
final class ProviderEndpointModel<T> {
    /**
     * True if this is {@link AsyncProvider}.
     */
    final boolean isAsync;

    /**
     * In which mode does this provider operate?
     */
    @NotNull final Service.Mode mode;
    /**
     * T of {@link Provider}&lt;T>.
     */
    @NotNull final Class datatype;
    /**
     * User class that extends {@link Provider}.
     */
    @NotNull final Class implClass;

    ProviderEndpointModel(Class<T> implementorClass, WSBinding binding) {
        assert implementorClass != null;
        assert binding != null;

        implClass = implementorClass;
        mode = getServiceMode(implementorClass);
        Class otherClass = (binding instanceof SOAPBinding)
            ? SOAPMessage.class : DataSource.class;
        isAsync = AsyncProvider.class.isAssignableFrom(implementorClass);


        Class<? extends Object> baseType = isAsync ? AsyncProvider.class : Provider.class;
        Type baseParam = BindingHelper.getBaseType(implementorClass, baseType);
        if (baseParam==null)
            throw new WebServiceException(ServerMessages.NOT_IMPLEMENT_PROVIDER(implementorClass.getName()));
        if (!(baseParam instanceof ParameterizedType))
            throw new WebServiceException(ServerMessages.PROVIDER_NOT_PARAMETERIZED(implementorClass.getName()));

        ParameterizedType pt = (ParameterizedType)baseParam;
        Type[] types = pt.getActualTypeArguments();
        if(!(types[0] instanceof Class))
            throw new WebServiceException(ServerMessages.PROVIDER_INVALID_PARAMETER_TYPE(implementorClass.getName(),types[0]));
        datatype = (Class)types[0];

        if (mode == Service.Mode.PAYLOAD && datatype!=Source.class) {
            // Illegal to have PAYLOAD && SOAPMessage
            // Illegal to have PAYLOAD && DataSource
            throw new IllegalArgumentException(
                "Illeagal combination - Mode.PAYLOAD and Provider<"+otherClass.getName()+">");
        }
    }

    /**
     * Is it PAYLOAD or MESSAGE ??
     *
     * @param c endpoint class
     * @return Service.Mode.PAYLOAD or Service.Mode.MESSAGE
     */
    private static Service.Mode getServiceMode(Class<?> c) {
        ServiceMode mode = c.getAnnotation(ServiceMode.class);
        return (mode == null) ? Service.Mode.PAYLOAD : mode.value();
    }
}
