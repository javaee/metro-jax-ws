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

package com.sun.xml.ws.transport.http;

import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.PortAddressResolver;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.istack.NotNull;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractList;
import java.util.Map.Entry;

/**
 * List of {@link HttpAdapter}s created together.
 *
 * <p>
 * Some cases WAR file may contain multiple endpoints for ports in a WSDL.
 * If the runtime knows these ports, their port addresses can be patched.
 * This class keeps a list of {@link HttpAdapter}s and use that information to patch
 * multiple port addresses.
 *
 * <p>
 * Concrete implementations of this class need to override {@link #createHttpAdapter}
 * method to create implementations of {@link HttpAdapter}.
 *
 * @author Jitendra Kotamraju
 */
public abstract class HttpAdapterList<T extends HttpAdapter> extends AbstractList<T> implements AdapterFactory<T> {
    private final List<T> adapters = new ArrayList<T>();
    private final Map<PortInfo, String> addressMap = new HashMap<PortInfo, String>();

    // TODO: documented because it's used by AS
    @Override
    public T createAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
        T t = createHttpAdapter(name, urlPattern, endpoint);
        adapters.add(t);
        WSDLPort port = endpoint.getPort();
        if (port != null) {
            PortInfo portInfo = new PortInfo(port.getOwner().getName(),port.getName().getLocalPart(), endpoint.getImplementationClass());
            addressMap.put(portInfo, getValidPath(urlPattern));
        }
        return t;
    }

    /**
     * Implementations need to override this one to create a concrete class
     * of HttpAdapter
     */
    protected abstract T createHttpAdapter(String name, String urlPattern, WSEndpoint<?> endpoint);

    /**
     * @return urlPattern without "/*"
     */
    private String getValidPath(@NotNull String urlPattern) {
        if (urlPattern.endsWith("/*")) {
            return urlPattern.substring(0, urlPattern.length() - 2);
        } else {
            return urlPattern;
        }
    }

    /**
     * Creates a PortAddressResolver that maps portname to its address
     *
     * @param endpointImpl application endpoint Class that eventually serves the request.
     */
    public PortAddressResolver createPortAddressResolver(final String baseAddress, final Class<?> endpointImpl) {
        return new PortAddressResolver() {
            @Override
            public String getAddressFor(@NotNull QName serviceName, @NotNull String portName) {
                String urlPattern = addressMap.get(new PortInfo(serviceName,portName, endpointImpl));
                if (urlPattern == null) {
                    //if a WSDL defines more ports, urlpattern is null (portName does not match endpointImpl)
                    //so fallback to the default behaviour where only serviceName/portName is checked
                    for (Entry<PortInfo, String> e : addressMap.entrySet()) {
                        if (serviceName.equals(e.getKey().serviceName) && portName.equals(e.getKey().portName)) {
                                urlPattern = e.getValue();
                                break;
                        }
                    }
                }
                return (urlPattern == null) ? null : baseAddress+urlPattern;
            }
        };
    }


    @Override
    public T get(int index) {
        return adapters.get(index);
    }

    @Override
    public int size() {
        return adapters.size();
    }

    private static class PortInfo {
        private final QName serviceName;
        private final String portName;
        private final Class<?> implClass;

        PortInfo(@NotNull QName serviceName, @NotNull String portName, Class<?> implClass) {
            this.serviceName = serviceName;
            this.portName = portName;
            this.implClass = implClass;
        }

        @Override
        public boolean equals(Object portInfo) {
            if (portInfo instanceof PortInfo) {
                PortInfo that = (PortInfo)portInfo;
                if (this.implClass == null) {
                    return this.serviceName.equals(that.serviceName) && this.portName.equals(that.portName) && that.implClass == null;
                }
                return this.serviceName.equals(that.serviceName) && this.portName.equals(that.portName) && this.implClass.equals(that.implClass);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int retVal = serviceName.hashCode()+portName.hashCode();
            return implClass != null ? retVal + implClass.hashCode() : retVal;
        }
    }
}
