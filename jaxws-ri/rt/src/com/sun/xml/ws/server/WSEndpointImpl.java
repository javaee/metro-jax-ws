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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.FiberContextSwitchInterceptor;
import com.sun.xml.ws.api.pipe.ServerPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.pipe.TubelineAssemblerFactory;
import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.model.wsdl.WSDLProperties;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.resources.HandlerMessages;
import com.sun.xml.ws.util.Pool;
import com.sun.xml.ws.util.Pool.TubePool;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.wsdl.OperationDispatcher;
import com.sun.xml.ws.addressing.EPRSDDocumentFilter;
import com.sun.xml.ws.addressing.WSEPRExtension;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.w3c.dom.Element;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.stream.XMLStreamException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link WSEndpoint} implementation.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public final class WSEndpointImpl<T> extends WSEndpoint<T> {
    private final @NotNull QName serviceName;
    private final @NotNull QName portName;
    private final WSBinding binding;
    private final SEIModel seiModel;
    private final @NotNull Container container;
    private final WSDLPort port;

    private final Tube masterTubeline;
    private final ServiceDefinitionImpl serviceDef;
    private final SOAPVersion soapVersion;
    private final Engine engine;
    private final @NotNull Codec masterCodec;
    private final @NotNull PolicyMap endpointPolicy;
    private final Pool<Tube> tubePool;
    private final OperationDispatcher operationDispatcher;
    private final @NotNull ManagedObjectManager managedObjectManager;
    private final @NotNull ServerTubeAssemblerContext context;

    private Map<QName, WSEndpointReference.EPRExtension> endpointReferenceExtensions = new HashMap<QName, WSEndpointReference.EPRExtension>();
    /**
     * Set to true once we start shutting down this endpoint.
     * Used to avoid running the clean up processing twice.
     *
     * @see #dispose()
     */
    private boolean disposed;

    private final Class<T> implementationClass;
    private final @Nullable WSDLProperties wsdlProperties;
    private final Set<EndpointComponent> componentRegistry = new LinkedHashSet<EndpointComponent>();

    WSEndpointImpl(@NotNull QName serviceName, @NotNull QName portName, WSBinding binding,
                   Container container, SEIModel seiModel, WSDLPort port,
                   Class<T> implementationClass,
                   @Nullable ServiceDefinitionImpl serviceDef,
                   InvokerTube terminalTube, boolean isSynchronous,
                   PolicyMap endpointPolicy) {
        this.serviceName = serviceName;
        this.portName = portName;
        this.binding = binding;
        this.soapVersion = binding.getSOAPVersion();
        this.container = container;
        this.port = port;
        this.implementationClass = implementationClass;
        this.serviceDef = serviceDef;
        this.seiModel = seiModel;
        this.endpointPolicy = endpointPolicy;

        this.managedObjectManager = 
            createManagedObjectManager(serviceName, portName);

        if (serviceDef != null) {
            serviceDef.setOwner(this);
        }

        TubelineAssembler assembler = TubelineAssemblerFactory.create(
                Thread.currentThread().getContextClassLoader(), binding.getBindingId(), container);
        assert assembler!=null;

        this.operationDispatcher = (port == null) ? null : new OperationDispatcher(port, binding, seiModel);

        context = new ServerPipeAssemblerContext(seiModel, port, this, terminalTube, isSynchronous);
        this.masterTubeline = assembler.createServer(context);

        Codec c = context.getCodec();
        if(c instanceof EndpointAwareCodec) {
            // create a copy to avoid sharing the codec between multiple endpoints 
            c = c.copy();
            ((EndpointAwareCodec)c).setEndpoint(this);
        }
        this.masterCodec = c;

        tubePool = new TubePool(masterTubeline);
        terminalTube.setEndpoint(this);
        engine = new Engine(toString());
        wsdlProperties = (port==null) ? null : new WSDLProperties(port);

        Map<QName, WSEndpointReference.EPRExtension> eprExtensions = new HashMap<QName, WSEndpointReference.EPRExtension>();
        try {
            if (port != null) {
                //gather EPR extrensions from WSDL Model
                WSEndpointReference wsdlEpr = ((WSDLPortImpl) port).getEPR();
                if (wsdlEpr != null) {
                    for (WSEndpointReference.EPRExtension extnEl : wsdlEpr.getEPRExtensions()) {
                        eprExtensions.put(extnEl.getQName(), extnEl);
                    }
                }
            }

            for (EndpointComponent ec : getComponentRegistry()) {
                EndpointReferenceExtensionContributor spi = ec.getSPI(EndpointReferenceExtensionContributor.class);
                if (spi != null) {
                    WSEndpointReference.EPRExtension wsdlEPRExtn = eprExtensions.remove(spi.getQName());
                    WSEndpointReference.EPRExtension endpointEprExtn = spi.getEPRExtension(wsdlEPRExtn);
                    if (endpointEprExtn != null) {
                        eprExtensions.put(endpointEprExtn.getQName(), endpointEprExtn);
                    }
                }
            }


            for (WSEndpointReference.EPRExtension extn : eprExtensions.values()) {
                endpointReferenceExtensions.put(extn.getQName(), new WSEPRExtension(
                        XMLStreamBuffer.createNewBufferFromXMLStreamReader(extn.readAsXMLStreamReader()),extn.getQName()));
            }
        } catch (XMLStreamException ex) {
            throw new WebServiceException(ex);
        }
        if(!eprExtensions.isEmpty()) {
            serviceDef.addFilter(new EPRSDDocumentFilter(this));
        }

    }

    public Collection<WSEndpointReference.EPRExtension> getEndpointReferenceExtensions() {
        return endpointReferenceExtensions.values();
    }
    /**
     * Nullable when there is no associated WSDL Model
     * @return
     */
    public @Nullable OperationDispatcher getOperationDispatcher() {
        return operationDispatcher;
    }

    public PolicyMap getPolicyMap() {
            return endpointPolicy;
    }

    public @NotNull Class<T> getImplementationClass() {
        return implementationClass;
    }

    public @NotNull WSBinding getBinding() {
        return binding;
    }

    public @NotNull Container getContainer() {
        return container;
    }

    public WSDLPort getPort() {
        return port;
    }

    @Override
    public @Nullable SEIModel getSEIModel() {
        return seiModel;
    }

    public void setExecutor(Executor exec) {
        engine.setExecutor(exec);
    }

    public void schedule(final Packet request, final CompletionCallback callback, FiberContextSwitchInterceptor interceptor) {
        request.endpoint = WSEndpointImpl.this;
        if (wsdlProperties != null) {
            request.addSatellite(wsdlProperties);
        }
        Fiber fiber = engine.createFiber();
        if (interceptor != null) {
            fiber.addInterceptor(interceptor);
        }
        final Tube tube = tubePool.take();
        fiber.start(tube, request, new Fiber.CompletionCallback() {
            public void onCompletion(@NotNull Packet response) {
                tubePool.recycle(tube);
                if (callback!=null) {
                    callback.onCompletion(response);
                }
            }

            public void onCompletion(@NotNull Throwable error) {
                // let's not reuse tubes as they might be in a wrong state, so not
                // calling tubePool.recycle()
                error.printStackTrace();
                // Convert all runtime exceptions to Packet so that transport doesn't
                // have to worry about converting to wire message
                // TODO XML/HTTP binding
                Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(
                        soapVersion, null, error);
                Packet response = request.createServerResponse(faultMsg, request.endpoint.getPort(), null,
                        request.endpoint.getBinding());
                if (callback!=null) {
                    callback.onCompletion(response);
                }
            }
        });
    }

    public @NotNull PipeHead createPipeHead() {
        return new PipeHead() {
            private final Tube tube = TubeCloner.clone(masterTubeline);

            public @NotNull Packet process(Packet request, WebServiceContextDelegate wscd, TransportBackChannel tbc) {
                request.webServiceContextDelegate = wscd;
                request.transportBackChannel = tbc;
                request.endpoint = WSEndpointImpl.this;
                if (wsdlProperties != null) {
                    request.addSatellite(wsdlProperties);
                }
                Fiber fiber = engine.createFiber();
                Packet response;
                try {
                    response = fiber.runSync(tube,request);
                } catch (RuntimeException re) {
                    // Catch all runtime exceptions so that transport doesn't
                    // have to worry about converting to wire message
                    // TODO XML/HTTP binding
                    re.printStackTrace();
                    Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(
                            soapVersion, null, re);
                    response = request.createServerResponse(faultMsg, request.endpoint.getPort(), null, request.endpoint.getBinding());
                }
                return response;
            }
        };
    }

    public synchronized void dispose() {
        if(disposed)
            return;
        disposed = true;

        masterTubeline.preDestroy();

        for (Handler handler : binding.getHandlerChain()) {
            for (Method method : handler.getClass().getMethods()) {
                if (method.getAnnotation(PreDestroy.class) == null) {
                    continue;
                }
                try {
                    method.invoke(handler);
                } catch (Exception e) {
                    logger.log(Level.WARNING, HandlerMessages.HANDLER_PREDESTROY_IGNORE(e.getMessage()), e);
                }
                break;
            }
        }

        try {
            managedObjectManager.close();
        } catch (java.io.IOException e) {
            logger.log(Level.WARNING, "TBD", e);
        }
    }

    public ServiceDefinitionImpl getServiceDefinition() {
        return serviceDef;
    }

    public Set<EndpointComponent> getComponentRegistry() {
        return componentRegistry;
    }

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.endpoint");

    public <T extends EndpointReference> T getEndpointReference(Class<T>
            clazz, String address, String wsdlAddress, Element... referenceParameters) {
        List<Element> refParams = null;
        if (referenceParameters != null) {
            refParams = Arrays.asList(referenceParameters);
        }
        return getEndpointReference(clazz, address, wsdlAddress, null, refParams);
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T>
            clazz, String address, String wsdlAddress, List<Element> metadata, List<Element> referenceParameters) {
        QName portType = null;
        if (port != null) {
            portType = port.getBinding().getPortTypeName();
        }

        AddressingVersion av = AddressingVersion.fromSpecClass(clazz);
        return new WSEndpointReference(
                    av, address, serviceName, portName, portType, metadata, wsdlAddress, referenceParameters,endpointReferenceExtensions.values(), null).toSpec(clazz);

    }

    public @NotNull QName getPortName() {
        return portName;
    }


    public @NotNull Codec createCodec() {
        return masterCodec.copy();
    }

    public @NotNull QName getServiceName() {
        return serviceName;
    }

    public @NotNull ManagedObjectManager getManagedObjectManager() {
        return managedObjectManager;
    }

    public @NotNull ServerTubeAssemblerContext getAssemblerContext() {
        return context;
    }

    private @Nullable ManagedObjectManager createManagedObjectManager(final QName serviceName, final QName portName) {
        if (!monitoring) {
            return ManagedObjectManagerFactory.createNOOP();
        }
        try {
            // TBD: Decide final root name.
            // Most likely it will be "metro" when running inside GlassFish,
            // (under the AMX node), and "com.sun.metro" otherwise.
            final ManagedObjectManager managedObjectManager =
                ManagedObjectManagerFactory.createStandalone("metro");

            if (typelibDebug != -1) {
                managedObjectManager.setTypelibDebug(typelibDebug);
            }
            if (registrationDebug.equals("FINE")) {
                managedObjectManager.setRegistrationDebug(ManagedObjectManager.RegistrationDebugLevel.FINE);
            } else if (registrationDebug.equals("NORMAL")) {
                managedObjectManager.setRegistrationDebug(ManagedObjectManager.RegistrationDebugLevel.NORMAL);
            } else {
                managedObjectManager.setRegistrationDebug(ManagedObjectManager.RegistrationDebugLevel.NONE);
            }
            managedObjectManager.setRuntimeDebug(runtimeDebug);

            managedObjectManager.stripPrefix(
                "com.sun.xml.ws.server",
                "com.sun.xml.ws.rx.rm.runtime.sequence");

            // Defer so we can register "this" as root from
            // within constructor.
            managedObjectManager.suspendJMXRegistration();

            // We include the service+portName to uniquely identify
            // the managed objects under it (since there can be multiple
            // services in the container).
            managedObjectManager.createRoot(
                new MonitorRootService(this),
                serviceName.toString() + portName.toString() + "-"
                + String.valueOf(unique++)); // TBD: only append unique
                                             // if clash. Waiting for GMBAL RFE

            return managedObjectManager;
        } catch (Throwable t) {
            // We let the service start up anyway, but it won't have monitoring.
            logger.log(Level.WARNING, "TBD", t);
        }
        return null;
    }

    private static boolean monitoring        = true;
    private static int     typelibDebug      = -1;
    private static String  registrationDebug = "NONE";
    private static boolean runtimeDebug      = false;

    static {
        try {
            // You can control "endpoint" independently of "client" monitoring.
            String s = System.getProperty("com.sun.xml.ws.monitoring.endpoint");
            if (s != null && s.toLowerCase().equals("false")) {
                monitoring = false;
            }

            // You can turn off both "endpoint" and "client" monitoring
            // at once.
            s = System.getProperty("com.sun.xml.ws.monitoring");
            if (s != null && s.toLowerCase().equals("false")) {
                monitoring = false;
            }

            Integer i = Integer.getInteger("com.sun.xml.ws.monitoring.typelibDebug");
            if (i != null) {
                typelibDebug = i;
            }
            s = System.getProperty("com.sun.xml.ws.monitoring.registrationDebug");
            if (s != null) {
                registrationDebug = s.toUpperCase();
            }
            s = System.getProperty("com.sun.xml.ws.monitoring.runtimeDebug");
            if (s != null && s.toLowerCase().equals("true")) {
                runtimeDebug = true;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "TBD", e);
        }
    }

    // Necessary because same serviceName+portName can live in
    // different apps at different addresses.  We do not know the
    // address until the first request comes in, which is after
    // monitoring is setup.

    // TBD: Add address field to Service class below and fill it
    // in on first request.  That will make it easier for users.
    private static long unique = 1;
}

