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
package com.sun.xml.ws.server.provider;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.AsyncProvider;

import javax.activation.DataSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.soap.SOAPBinding;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * Keeps the runtime information like Service.Mode and erasure of Provider class
 * about Provider endpoint. It proccess annotations to find about Service.Mode
 * It also finds about parameterized type(e.g. Source, SOAPMessage, DataSource)
 * of endpoint class.
 * 
 */
final class ProviderEndpointModel<T> {
    
    private final boolean isSource;
    private final Service.Mode mode;
    private final boolean isAsync;

    ProviderEndpointModel(Class<T> implementorClass, WSBinding binding) {
        assert implementorClass != null;
        assert binding != null;

        mode = getServiceMode(implementorClass);
        Class otherClass = (binding instanceof SOAPBinding)
            ? SOAPMessage.class : DataSource.class;
        isAsync = AsyncProvider.class.isAssignableFrom(implementorClass);
        isSource = isSource(isAsync ? AsyncProvider.class : Provider.class,
                implementorClass, otherClass);

        if (mode == Service.Mode.PAYLOAD && !isSource) {
            // Illegal to have PAYLOAD && SOAPMessage
            // Illegal to have PAYLOAD && DataSource
            throw new IllegalArgumentException(
                "Illeagal combination - Mode.PAYLOAD and Provider<"+otherClass.getName()+">");
        }
    }
    
    public boolean isSource() {
        return isSource;
    }

    public boolean isAsync() {
        return isAsync;
    }
    
    public Service.Mode getServiceMode() {
        return mode;
    }
    
    /**
     * Is it PAYLOAD or MESSAGE ??
     */
    private static Service.Mode getServiceMode(Class<?> c) {
        ServiceMode mode = c.getAnnotation(ServiceMode.class);
        if (mode == null) {
            return Service.Mode.PAYLOAD;
        }
        return mode.value();
    }

    /**
     * Is it Provider&lt;Source> ? Finds whether the parameterized type is
     * Source.class or not.
     *
     * @param provider Provider.class or AsyncProvider.class
     * @param c provider endpoint class
     * @param otherClass Typically SOAPMessage.class or DataSource.class
     * @return true if c's parameterized type is Source
     *         false otherwise
     * @throws IllegalArgumentException if it is not
     *         Provider&lt;Source> or Provider&lt;otherClass>
     *
     */
    private static boolean isSource(Class provider, Class c, Class otherClass) {
        Type base = JAXBRIContext.getBaseType(c, provider);
        assert base != null;
        if (base instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)base;
            Type[] types = pt.getActualTypeArguments();
            if (types[0] instanceof Class && Source.class.isAssignableFrom((Class)types[0])) {
                return true;
            }
            if (types[0] instanceof Class && otherClass.isAssignableFrom((Class)types[0])) {
                return false;
            }
        }
        throw new IllegalArgumentException(
            "Endpoint should implement Provider<"+Source.class.getName()+
                "> or Provider<"+otherClass.getName()+">");
    }

}
