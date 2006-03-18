/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.handler.HandlerResolverImpl;
import com.sun.xml.ws.handler.PortInfoImpl;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
import com.sun.xml.ws.util.JAXWSUtils;
import com.sun.xml.ws.wsdl.WSDLContext;
import org.xml.sax.EntityResolver;
import javax.jws.HandlerChain;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.jws.WebService;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

/**
 * $author: WS Development Team
 */
public abstract class ServiceContextBuilder {
    private ServiceContextBuilder() {
    }  // no instantication please

    /**
     * Creates a new {@link ServiceContext}.
     */
    public static ServiceContext build(URL wsdlLocation, QName serviceName, final Class service, EntityResolver er) throws WebServiceException {
        ServiceContext serviceContext = new ServiceContext(service, serviceName, er);

        if (wsdlLocation != null)
            serviceContext.setWsdlContext(new WSDLContext(wsdlLocation, er));
        
        //if @HandlerChain present, set HandlerResolver on service context
        HandlerChain handlerChain = (HandlerChain)
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return service.getAnnotation(HandlerChain.class);
            }
        });
        if(handlerChain != null) {
            HandlerResolverImpl hresolver = new HandlerResolverImpl(serviceContext);
            serviceContext.setHandlerResolver(hresolver);
        }
        return serviceContext;
    }

    public static void completeServiceContext(ServiceContext serviceContext, Class portInterface) {
        if (portInterface != null)
            processAnnotations(serviceContext, portInterface);
    }

    private static QName getPortName(Class portInterface, Class serviceInterface) {
        QName portName = null;
        WebServiceClient wsClient = (WebServiceClient) serviceInterface.getAnnotation(WebServiceClient.class);
        for (Method method : serviceInterface.getMethods()) {
            if (!method.getDeclaringClass().equals(serviceInterface)) {
                continue;
            }
            WebEndpoint webEndpoint = method.getAnnotation(WebEndpoint.class);
            if (webEndpoint == null) {
                continue;
            }
            if (method.getGenericReturnType().equals(portInterface)) {
                if (method.getName().startsWith("get")) {
                    portName = new QName(wsClient.targetNamespace(), webEndpoint.name());
                    break;
                }
            }
        }
        return portName;
    }

    //does any necessagy checking and validation

    //todo: valid port in wsdl
    private static void processAnnotations(ServiceContext serviceContext, Class portInterface) throws WebServiceException {
        EndpointIFContext eifc = serviceContext.getEndpointIFContext(portInterface.getName());
        if ((eifc != null) && (eifc.getRuntimeContext() != null)) {
            return;
        }
        if (eifc == null) {
            eifc = new EndpointIFContext(portInterface);
            serviceContext.addEndpointIFContext(eifc);
        }

        QName serviceName = serviceContext.getServiceName();
        QName portName = eifc.getPortName();
        if (serviceContext.getServiceClass() != null) {
            if (portName == null)
                portName = getPortName(portInterface, serviceContext.getServiceClass());
        }

        if (portName == null) {
            portName = serviceContext.getWsdlContext().getPortName();
        }

        String bindingId = serviceContext.getWsdlContext().getBindingID(
            serviceName, portName);
        RuntimeModeler modeler = new RuntimeModeler(portInterface,
            serviceName, bindingId);
        modeler.setPortName(portName);
        RuntimeModel model = modeler.buildRuntimeModel();

        eifc.setRuntimeContext(new RuntimeContext(model));
        if (serviceContext.getServiceName() == null)
            serviceContext.setServiceName(serviceContext.getWsdlContext().getFirstServiceName());
    }

    private ArrayList<Class<?>> getSEI(final Class sc) {

        if (sc == null) {
            throw new WebServiceException();
        }

        //check to make sure this is a service
        if (!Service.class.isAssignableFrom(sc)) {
            throw new WebServiceException("service.interface.required" +
                sc.getName());
        }

        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Method[] methods = sc.getDeclaredMethods();
                for (final Method method : methods) {
                    method.setAccessible(true);
                    Class<?> seiClazz = method.getReturnType();
                    if ((seiClazz != null) && (!seiClazz.equals("void")))
                        classes.add(seiClazz);

                }
                return null;
            }
        });

        return classes;
    }

}
