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

package org.jvnet.ws.databinding.impl.converter;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceFeature;

import org.jvnet.ws.databinding.Databinding;
import org.jvnet.ws.databinding.Databinding.Builder;
import org.jvnet.ws.databinding.Databinding.WSDLGenerator;
import org.jvnet.ws.databinding.JavaCallInfo;
import org.jvnet.ws.message.MessageContext;
import org.xml.sax.EntityResolver;

@SuppressWarnings("deprecation")
public class OldDatabindingFactory extends org.jvnet.ws.databinding.DatabindingFactory {
    private final com.oracle.webservices.api.databinding.DatabindingFactory factory;
    
    public OldDatabindingFactory(com.oracle.webservices.api.databinding.DatabindingFactory factory) {
        this.factory = factory;
    }
    
    @Override
    public org.jvnet.ws.databinding.Databinding.Builder createBuilder(Class<?> contractClass, Class<?> endpointClass) {
        final com.oracle.webservices.api.databinding.Databinding.Builder builder = factory.createBuilder(contractClass, endpointClass);
        org.jvnet.ws.databinding.Databinding.Builder oldBuilder = new org.jvnet.ws.databinding.Databinding.Builder() {

            @Override
            public Builder targetNamespace(String targetNamespace) {
                builder.targetNamespace(targetNamespace);
                return this;
            }

            @Override
            public Builder serviceName(QName serviceName) {
                builder.serviceName(serviceName);
                return this;
            }

            @Override
            public Builder portName(QName portName) {
                builder.portName(portName);
                return this;
            }

            @Override
            public Builder wsdlURL(URL wsdlURL) {
                builder.wsdlURL(wsdlURL);
                return this;
            }

            @Override
            public Builder wsdlSource(Source wsdlSource) {
                builder.wsdlSource(wsdlSource);
                return this;
            }

            @Override
            public Builder entityResolver(EntityResolver entityResolver) {
                builder.entityResolver(entityResolver);
                return this;
            }

            @Override
            public Builder classLoader(ClassLoader classLoader) {
                builder.classLoader(classLoader);
                return this;
            }

            @Override
            public Builder feature(WebServiceFeature... features) {
                builder.feature(features);
                return this;
            }

            @Override
            public Builder property(String name, Object value) {
                builder.property(name, value);
                return this;
            }
            
            @Override
            public Databinding build() {
                final com.oracle.webservices.api.databinding.Databinding databinding = builder.build();
                return new org.jvnet.ws.databinding.Databinding() {

                    @Override
                    public JavaCallInfo createJavaCallInfo(Method method,
                            Object[] args) {
                        return new OldJavaCallInfo(databinding.createJavaCallInfo(method, args));
                    }

                    @Override
                    public MessageContext serializeRequest(JavaCallInfo call) {
                        return (MessageContext) databinding.serializeRequest(new NewJavaCallInfo(call));
                    }

                    @Override
                    public JavaCallInfo deserializeResponse(
                            MessageContext message, JavaCallInfo call) {
                        return new OldJavaCallInfo(databinding.deserializeResponse(message, new NewJavaCallInfo(call)));
                    }

                    @Override
                    public JavaCallInfo deserializeRequest(
                            MessageContext message) {
                        return new OldJavaCallInfo(databinding.deserializeRequest(message));
                    }

                    @Override
                    public MessageContext serializeResponse(JavaCallInfo call) {
                        return (MessageContext) databinding.serializeResponse(new NewJavaCallInfo(call));
                    }
                    
                };
            }

            @Override
            public WSDLGenerator createWSDLGenerator() {
                return (WSDLGenerator) new OldWSDLGenerator(builder.createWSDLGenerator());
            }
            
        };
        
        return oldBuilder;
    }

    @Override
    public Map<String, Object> properties() {
        return factory.properties();
    }

}
