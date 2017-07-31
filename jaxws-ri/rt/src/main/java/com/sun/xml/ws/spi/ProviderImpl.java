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

package com.sun.xml.ws.spi;


import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.ServiceSharedFeatureMarker;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.ContainerResolver;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.client.WSServiceDelegate;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.resources.ProviderApiMessages;
import com.sun.xml.ws.transport.http.server.EndpointImpl;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.spi.Invoker;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;

/**
 * The entry point to the JAX-WS RI from the JAX-WS API.
 *
 * @author WS Development Team
 */
public class ProviderImpl extends Provider {

    private final static ContextClassloaderLocal<JAXBContext> eprjc = new ContextClassloaderLocal<JAXBContext>() {
        @Override
        protected JAXBContext initialValue() throws Exception {
            return getEPRJaxbContext();
        }
    };

    /**
     * Convenient singleton instance.
     */
    public static final ProviderImpl INSTANCE = new ProviderImpl();

    @Override
    public Endpoint createEndpoint(String bindingId, Object implementor) {
        return new EndpointImpl(
            (bindingId != null) ? BindingID.parse(bindingId) : BindingID.parse(implementor.getClass()),
            implementor);
    }

    @Override
    public ServiceDelegate createServiceDelegate( URL wsdlDocumentLocation, QName serviceName, Class serviceClass) {
         return new WSServiceDelegate(wsdlDocumentLocation, serviceName, serviceClass);
    }

    public ServiceDelegate createServiceDelegate( URL wsdlDocumentLocation, QName serviceName, Class serviceClass,
                                                  WebServiceFeature ... features) {
        for (WebServiceFeature feature : features) {
            if (!(feature instanceof ServiceSharedFeatureMarker))
            throw new WebServiceException("Doesn't support any Service specific features");
        }
        return new WSServiceDelegate(wsdlDocumentLocation, serviceName, serviceClass, features);
    }

    public ServiceDelegate createServiceDelegate( Source wsdlSource, QName serviceName, Class serviceClass) {
        return new WSServiceDelegate(wsdlSource, serviceName, serviceClass);
   }

    @Override
    public Endpoint createAndPublishEndpoint(String address,
                                             Object implementor) {
        Endpoint endpoint = new EndpointImpl(
            BindingID.parse(implementor.getClass()),
            implementor);
        endpoint.publish(address);
        return endpoint;
    }

    public Endpoint createEndpoint(String bindingId, Object implementor, WebServiceFeature... features) {
        return new EndpointImpl(
            (bindingId != null) ? BindingID.parse(bindingId) : BindingID.parse(implementor.getClass()),
            implementor, features);
    }

    public Endpoint createAndPublishEndpoint(String address, Object implementor, WebServiceFeature... features) {
        Endpoint endpoint = new EndpointImpl(
            BindingID.parse(implementor.getClass()), implementor, features);
        endpoint.publish(address);
        return endpoint;
    }

    public Endpoint createEndpoint(String bindingId, Class implementorClass, Invoker invoker, WebServiceFeature... features) {
        return new EndpointImpl(
            (bindingId != null) ? BindingID.parse(bindingId) : BindingID.parse(implementorClass),
            implementorClass, invoker, features);
    }
    
    public EndpointReference readEndpointReference(final Source eprInfoset) {

        try {
            Unmarshaller unmarshaller = eprjc.get().createUnmarshaller();
            return (EndpointReference) unmarshaller.unmarshal(eprInfoset);
        } catch (JAXBException e) {
            throw new WebServiceException("Error creating Marshaller or marshalling.", e);
        }

    }

    public <T> T getPort(EndpointReference endpointReference, Class<T> clazz, WebServiceFeature... webServiceFeatures) {
        /*
        final @NotNull MemberSubmissionEndpointReference msepr =
                EndpointReferenceUtil.transform(MemberSubmissionEndpointReference.class, endpointReference);
                WSService service = new WSServiceDelegate(msepr.toWSDLSource(), msepr.serviceName.name, Service.class);
                */
        if(endpointReference == null)
            throw new WebServiceException(ProviderApiMessages.NULL_EPR());
        WSEndpointReference wsepr =  new WSEndpointReference(endpointReference);
        WSEndpointReference.Metadata metadata = wsepr.getMetaData();
        WSService service;
        if(metadata.getWsdlSource() != null)
            service = (WSService) createServiceDelegate(metadata.getWsdlSource(), metadata.getServiceName(), Service.class);
        else
            throw new WebServiceException("WSDL metadata is missing in EPR");
        return service.getPort(wsepr, clazz, webServiceFeatures);
    }

    public W3CEndpointReference createW3CEndpointReference(String address, QName serviceName, QName portName, List<Element> metadata, String wsdlDocumentLocation, List<Element> referenceParameters) {
        return createW3CEndpointReference(address, null, serviceName, portName, metadata, wsdlDocumentLocation, referenceParameters, null, null);
    }

    public W3CEndpointReference createW3CEndpointReference(String address, QName interfaceName, QName serviceName, QName portName,
            List<Element> metadata, String wsdlDocumentLocation, List<Element> referenceParameters,
            List<Element> elements, Map<QName, String> attributes) {
        Container container = ContainerResolver.getInstance().getContainer();
        if (address == null) {
            if (serviceName == null || portName == null) {
                throw new IllegalStateException(ProviderApiMessages.NULL_ADDRESS_SERVICE_ENDPOINT());
            } else {
                //check if it is run in a Java EE Container and if so, get address using serviceName and portName
                Module module = container.getSPI(Module.class);
                if (module != null) {
                    List<BoundEndpoint> beList = module.getBoundEndpoints();
                    for (BoundEndpoint be : beList) {
                        WSEndpoint wse = be.getEndpoint();
                        if (wse.getServiceName().equals(serviceName) && wse.getPortName().equals(portName)) {
                            try {
                                address = be.getAddress().toString();
                            } catch (WebServiceException e) {
                                // May be the container does n't support this
                                //just ignore the exception
                            }
                            break;
                        }
                    }
                }
                //address is still null? may be its not run in a JavaEE Container
                if (address == null)
                    throw new IllegalStateException(ProviderApiMessages.NULL_ADDRESS());
            }
        }
        if((serviceName==null) && (portName != null)) {
            throw new IllegalStateException(ProviderApiMessages.NULL_SERVICE());
        }
        //Validate Service and Port in WSDL
        String wsdlTargetNamespace = null;
        if (wsdlDocumentLocation != null) {
            try {
                EntityResolver er = XmlUtil.createDefaultCatalogResolver();

                URL wsdlLoc = new URL(wsdlDocumentLocation);
                WSDLModel wsdlDoc = RuntimeWSDLParser.parse(wsdlLoc, new StreamSource(wsdlLoc.toExternalForm()), er,
                        true, container, ServiceFinder.find(WSDLParserExtension.class).toArray());
                if (serviceName != null) {
                    WSDLService wsdlService = wsdlDoc.getService(serviceName);
                    if (wsdlService == null)
                        throw new IllegalStateException(ProviderApiMessages.NOTFOUND_SERVICE_IN_WSDL(
                                serviceName,wsdlDocumentLocation));
                    if (portName != null) {
                        WSDLPort wsdlPort = wsdlService.get(portName);
                        if (wsdlPort == null)
                            throw new IllegalStateException(ProviderApiMessages.NOTFOUND_PORT_IN_WSDL(
                                    portName,serviceName,wsdlDocumentLocation));
                    }
                    wsdlTargetNamespace = serviceName.getNamespaceURI();
                } else {
                    QName firstService = wsdlDoc.getFirstServiceName();
                    wsdlTargetNamespace = firstService.getNamespaceURI();
                }
            } catch (Exception e) {
                throw new IllegalStateException(ProviderApiMessages.ERROR_WSDL(wsdlDocumentLocation),e);
            }
        }
        //wcf3.0/3.5 rejected empty metadata element.
        if (metadata != null && metadata.size() == 0) {
           metadata = null;
        }
        return new WSEndpointReference(
            AddressingVersion.fromSpecClass(W3CEndpointReference.class),
            address, serviceName, portName, interfaceName, metadata, wsdlDocumentLocation, wsdlTargetNamespace,referenceParameters, elements, attributes).toSpec(W3CEndpointReference.class);

    }

    private static JAXBContext getEPRJaxbContext() {
        // EPRs have package and private fields, so we need privilege escalation.
        // this access only fixed, known set of classes, so doing that
        // shouldn't introduce security vulnerability.
        return AccessController.doPrivileged(new PrivilegedAction<JAXBContext>() {
            public JAXBContext run() {
                try {
                    return JAXBContext.newInstance(MemberSubmissionEndpointReference.class, W3CEndpointReference.class);
                } catch (JAXBException e) {
                    throw new WebServiceException("Error creating JAXBContext for W3CEndpointReference. ", e);
                }
            }
        });
    }
}
