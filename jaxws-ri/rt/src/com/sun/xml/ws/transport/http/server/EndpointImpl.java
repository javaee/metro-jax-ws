/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.transport.http.server;

import com.sun.istack.Nullable;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.server.EndpointFactory;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.istack.NotNull;

import java.net.MalformedURLException;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.ws.*;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.w3c.dom.Element;


/**
 * Implements {@link Endpoint}.
 * <p/>
 * <p/>
 * This class accumulates the information necessary to create
 * {@link WSEndpoint}, and then when {@link #publish} method
 * is called it will be created.
 * <p/>
 * <p/>
 * This object also allows accumulated information to be retrieved.
 *
 * @author Jitendra Kotamraju
 */
public class EndpointImpl extends Endpoint {

    private static final WebServicePermission ENDPOINT_PUBLISH_PERMISSION =
            new WebServicePermission("publishEndpoint");

    /**
     * Once the service is published, this field will
     * be set to the {@link HttpEndpoint} instance.
     * <p/>
     * But don't declare the type as {@link HttpEndpoint}
     * to avoid static type dependency that cause the class loading to
     * fail if the LW HTTP server doesn't exist.
     */
    private Object actualEndpoint;

    // information accumulated for creating WSEndpoint
    private final WSBinding binding;
    private final Object implementor;
    private List<Source> metadata;
    private Executor executor;
    private Map<String, Object> properties = Collections.emptyMap(); // always non-null
    private boolean stopped;


    public EndpointImpl(@NotNull BindingID bindingId, @NotNull Object impl) {
        binding = BindingImpl.create(bindingId);
        implementor = impl;
    }

    /**
     * Wraps an already created {@link WSEndpoint} into an {@link EndpointImpl},
     * and immediately publishes it with the given context.
     *
     * @deprecated This is a backdoor method. Don't use it unless you know what you are doing.
     */
    public EndpointImpl(WSEndpoint wse, Object serverContext) {
        actualEndpoint = new HttpEndpoint(wse, executor);
        ((HttpEndpoint) actualEndpoint).publish(serverContext);
        binding = wse.getBinding();
        implementor = null; // this violates the semantics, but hey, this is a backdoor.
    }

    public Binding getBinding() {
        return binding;
    }

    public Object getImplementor() {
        return implementor;
    }

    public void publish(String address) {
        canPublish();
        URL url;
        try {
            url = new URL(address);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Cannot create URL for this address " + address);
        }
        if (!url.getProtocol().equals("http")) {
            throw new IllegalArgumentException(url.getProtocol() + " protocol based address is not supported");
        }
        if (!url.getPath().startsWith("/")) {
            throw new IllegalArgumentException("Incorrect WebService address=" + address +
                    ". The address's path should start with /");
        }
        createEndpoint();
        ((HttpEndpoint) actualEndpoint).publish(address);
    }

    public void publish(Object serverContext) {
        canPublish();
        if (!com.sun.net.httpserver.HttpContext.class.isAssignableFrom(serverContext.getClass())) {
            throw new IllegalArgumentException(serverContext.getClass() + " is not a supported context.");
        }
        createEndpoint();
        ((HttpEndpoint) actualEndpoint).publish(serverContext);
    }

    public void stop() {
        if (isPublished()) {
            ((HttpEndpoint) actualEndpoint).stop();
            actualEndpoint = null;
            stopped = true;
        }
    }

    public boolean isPublished() {
        return actualEndpoint != null;
    }

    public List<Source> getMetadata() {
        return metadata;
    }

    public void setMetadata(java.util.List<Source> metadata) {
        if (isPublished()) {
            throw new IllegalStateException("Cannot set Metadata. Endpoint is already published");
        }
        this.metadata = metadata;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Map<String, Object> getProperties() {
        return new HashMap<String, Object>(properties);
    }

    public void setProperties(Map<String, Object> map) {
        this.properties = new HashMap<String, Object>(map);
    }

    /*
    * Checks the permission of "publishEndpoint" before accessing HTTP classes.
    * Also it checks if there is an available HTTP server implementation.
    */
    private void createEndpoint() {
        // Checks permission for "publishEndpoint"
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(ENDPOINT_PUBLISH_PERMISSION);
        }

        // See if HttpServer implementation is available
        try {
            Class.forName("com.sun.net.httpserver.HttpServer");
        } catch (Exception e) {
            throw new UnsupportedOperationException("NOT SUPPORTED");
        }

        WSEndpoint wse = EndpointFactory.createEndpoint(
                implementor.getClass(), true,
                InstanceResolver.createSingleton(implementor),
                getProperty(QName.class, Endpoint.WSDL_SERVICE),
                getProperty(QName.class, Endpoint.WSDL_PORT),
                null /* no container */,
                binding,
                getPrimaryWsdl(),
                buildDocList(),
                null,
                false
        );
        // Don't load HttpEndpoint class before as it may load HttpServer classes
        actualEndpoint = new HttpEndpoint(wse, executor);
    }

    private <T> T getProperty(Class<T> type, String key) {
        Object o = properties.get(key);
        if (o == null) return null;
        if (type.isInstance(o))
            return type.cast(o);
        else
            throw new IllegalArgumentException("Property " + key + " has to be of type " + type);   // i18n
    }

    /**
     * Convert metadata sources using identity transform. So that we can
     * reuse the Source object multiple times.
     */
    private List<SDDocumentSource> buildDocList() {
        List<SDDocumentSource> r = new ArrayList<SDDocumentSource>();

        if (metadata != null) {
            Transformer transformer = XmlUtil.newTransformer();
            for (Source source : metadata) {
                try {
                    XMLStreamBufferResult xsbr = new XMLStreamBufferResult();
                    transformer.transform(source, xsbr);
                    String systemId = source.getSystemId();

                    r.add(SDDocumentSource.create(new URL(systemId), xsbr.getXMLStreamBuffer()));
                } catch (TransformerException te) {
                    throw new ServerRtException("server.rt.err", te);
                } catch (IOException te) {
                    throw new ServerRtException("server.rt.err", te);
                }
            }
        }

        return r;
    }

    /**
     * Gets wsdl from @WebService or @WebServiceProvider
     */
    private @Nullable SDDocumentSource getPrimaryWsdl() {
        Class implType = implementor.getClass();
        // Takes care of @WebService, @WebServiceProvider's wsdlLocation
        String wsdlLocation = EndpointFactory.getWsdlLocation(implType);
        if (wsdlLocation != null) {
            ClassLoader cl = implType.getClassLoader();
            URL url = cl.getResource(wsdlLocation);
            if (url != null) {
                return SDDocumentSource.create(url);
            }
            throw new ServerRtException("cannot.load.wsdl", wsdlLocation);
        }
        return null;
    }

    private void canPublish() {
        if (isPublished()) {
            throw new IllegalStateException(
                    "Cannot publish this endpoint. Endpoint has been already published.");
        }
        if (stopped) {
            throw new IllegalStateException(
                    "Cannot publish this endpoint. Endpoint has been already stopped.");
        }
    }

    public EndpointReference getEndpointReference(Element...referenceParameters) {
        return getEndpointReference(W3CEndpointReference.class, referenceParameters);
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element...referenceParameters) {
        if (!isPublished()) {
            throw new WebServiceException("Endpoint is not published yet");
        }
        return ((HttpEndpoint)actualEndpoint).getEndpointReference(clazz,referenceParameters);
    }

}

