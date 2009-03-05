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
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.EndpointAwareCodec;
import com.sun.xml.ws.api.server.EndpointComponent;
import com.sun.xml.ws.api.server.TransportBackChannel;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.model.wsdl.WSDLProperties;
import com.sun.xml.ws.resources.HandlerMessages;
import com.sun.xml.ws.util.Pool;
import com.sun.xml.ws.util.Pool.TubePool;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.wsdl.OperationDispatcher;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import org.w3c.dom.Element;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.handler.Handler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link WSEndpoint} implementation.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
@ManagedObject
@Description("Web Service endpoint")
public final class WSEndpointImpl<T> extends WSEndpoint<T> {
    // Register JAX-WS JMX MBeans
    static {
        try {
            JMXAgent.getDefault();
        } catch (Throwable t) {
            // Ignore for now by logging the stack trace
            t.printStackTrace();
        }
    }

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
    private final ManagedObjectManager managedObjectManager;

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
                   InvokerTube terminalTube, boolean isSynchronous, PolicyMap endpointPolicy) {
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
        if (serviceDef != null) {
            serviceDef.setOwner(this);
        }

 	managedObjectManager = createManagedObjectManager(serviceName, portName);
        TubelineAssembler assembler = TubelineAssemblerFactory.create(
                Thread.currentThread().getContextClassLoader(), binding.getBindingId(), container);
        assert assembler!=null;

        this.operationDispatcher = (port == null) ? null : new OperationDispatcher(port, binding, seiModel);

        ServerTubeAssemblerContext context = new ServerPipeAssemblerContext(seiModel, port, this, terminalTube, isSynchronous);
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

    @ManagedAttribute
    @Description("port")
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

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, String address, String wsdlAddress, Element...referenceParameters) {
        QName portType = null;
        if(port != null) {
            portType = port.getBinding().getPortTypeName();            
        }
        List<Element> refParams = null;
        if(referenceParameters != null) {
            refParams = Arrays.asList(referenceParameters);
        }
        AddressingVersion av = AddressingVersion.fromSpecClass(clazz);
        return new WSEndpointReference(
                    av, address, serviceName, portName, portType, null, wsdlAddress, refParams).toSpec(clazz);        
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

    private ManagedObjectManager createManagedObjectManager(
        QName serviceName, QName portName) {

	ManagedObjectManager managedObjectManager = null;
	// TBD: for now the root is "metro"
	// It may become com.sun.metro
	managedObjectManager = ManagedObjectManagerFactory.createStandalone("metro");

	managedObjectManager.stripPrefix(
	    "com.sun.xml.ws",
	    "com.sun.xml.ws.rx.runtime");

	// TBD: We include the service+portName to uniquely identify
	// the managed objects under it (since there can be multiple
	// services in the container.
	// The existing format below is temporary and will change.
	managedObjectManager.createRoot(
            this, 
	    serviceName.toString().replace(':', '-')
	    + portName.toString().replace(':', '-'));

	return managedObjectManager;
    }
}
