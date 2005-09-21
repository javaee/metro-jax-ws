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
    public static ServiceContext build(URL wsdlLocation, Class service, EntityResolver er) throws WebServiceException {

        ServiceContext serviceContext = new ServiceContext(er);
        SCAnnotations serviceCAnnotations;
        if ((service != null)) {

            serviceCAnnotations = getSCAnnotations(service);
            if ((serviceCAnnotations == null) && (service != javax.xml.ws.Service.class))
                throw new WebServiceException("Service Interface Annotations required, exiting...");
            else
                serviceContext.setSCAnnotations(serviceCAnnotations);

            if ((wsdlLocation == null) && (serviceCAnnotations != null)) {
                try {
                    wsdlLocation = new URL(JAXWSUtils.getFileOrURLName(serviceCAnnotations.wsdlLocation));
                } catch (MalformedURLException e) {
                    throw new WebServiceException(e);
                }
            }

            if (wsdlLocation != null)
                serviceContext.setWsdlContext(new WSDLContext(wsdlLocation, er));

            if (serviceCAnnotations != null) {
                serviceContext.setServiceClass(service);
                for (Class clazz : serviceCAnnotations.classes) {
                    processAnnotations(serviceContext, clazz);
                }
            }
        }
        return serviceContext;
    }

    public static void completeServiceContext(ServiceContext serviceContext, Class portInterface) {
        if ((serviceContext.getWsdlContext() == null) && (portInterface != null)) {
            URL wsdlLocation = null;
            try {
                wsdlLocation = new URL(JAXWSUtils.getFileOrURLName(getWSDLLocation(portInterface)));
            } catch (MalformedURLException e) {
                throw new WebServiceException(e);
            }

            serviceContext.setWsdlContext(new WSDLContext(wsdlLocation, serviceContext.getEntityResolver()));
        }

        if ((portInterface != null) && (serviceContext.getEndpointIFContext().isEmpty()))
            processAnnotations(serviceContext, portInterface);
    }

    private static QName getServiceName(Class serviceInterface) {
        WebServiceClient wsClient = (WebServiceClient) serviceInterface.getAnnotation(WebServiceClient.class);
        QName serviceName = null;
        if (wsClient != null) {
            String name = wsClient.name();
            String namespace = wsClient.targetNamespace();
            serviceName = new QName(namespace, name);
        }
        return serviceName;
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
        if ((eifc == null) || (eifc.getRuntimeContext() == null)) {

            if (eifc == null) {
                eifc = new EndpointIFContext(portInterface);
                serviceContext.addEndpointIFContext(eifc);
            }

            //toDo:
            QName serviceName = serviceContext.getServiceName();
            QName portName = eifc.getPortName();
            if (serviceContext.getServiceClass() != null) {
                if (serviceName == null)
                    serviceName = getServiceName(serviceContext.getServiceClass());
                if (portName == null)
                    portName = getPortName(portInterface, serviceContext.getServiceClass());
            }

            if (portName == null) {
                portName = serviceContext.getWsdlContext().getPortName();
            }

            //todo:use SCAnnotations and put in map
            RuntimeModeler modeler = new RuntimeModeler(portInterface, serviceName,
                serviceContext.getWsdlContext().getBindingID().toString());
            modeler.setPortName(portName);
            RuntimeModel model = modeler.buildRuntimeModel();

            eifc.setRuntimeContext(new RuntimeContext(model));

            // get handler information
            String bindingId = modeler.getBindingId();
            HandlerAnnotationInfo chainInfo =
                HandlerAnnotationProcessor.buildHandlerInfo(portInterface,
                    model.getServiceQName(), model.getPortName(), bindingId);

            if (serviceContext.getServiceName() == null)
                serviceContext.setServiceName(serviceContext.getWsdlContext().getFirstServiceName());

            if (chainInfo != null) {
                HandlerResolverImpl resolver =
                    getHandlerResolver(serviceContext);
                resolver.setHandlerChain(new PortInfoImpl(
                    bindingId,
                    model.getPortName(),
                    model.getServiceQName()),
                    chainInfo.getHandlers());
                serviceContext.setHandlerResolver(resolver);
                serviceContext.setRoles(chainInfo.getRoles());

            }
        }
    }

    private static HandlerResolverImpl getHandlerResolver(
        ServiceContext serviceContext) {
        if (serviceContext.getHandlerResolver() == null) {
            serviceContext.setHandlerResolver(new HandlerResolverImpl());
        }
        return serviceContext.getHandlerResolver();
    }

    private ArrayList<Class> getSEI(final Class sc) {

        if (sc == null) {
            throw new WebServiceException();
        }

        //check to make sure this is a service
        if (!Service.class.isAssignableFrom(sc)) {
            throw new WebServiceException("service.interface.required" +
                sc.getName());
        }

        final ArrayList<Class> classes = new ArrayList();
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Method[] methods = sc.getDeclaredMethods();
                for (final Method method : methods) {
                    method.setAccessible(true);
                    Class seiClazz = method.getReturnType();
                    if ((seiClazz != null) && (!seiClazz.equals("void"))) 
                        classes.add(seiClazz);

                }
                return null;
            }
        });

        return classes;
    }

    /**
     * Utility method to get wsdlLocation attribute from @WebService annotation on sei.
     *
     * @return the URL of the location of the WSDL for the sei, or null if none was found.
     */
//this will change
    private static String getWSDLLocation(Class<?> sei) throws MalformedURLException {
        WebService ws = sei.getAnnotation(WebService.class);
        if (ws == null)
            return null;
        return ws.wsdlLocation();
    }

//this will change

    private static SCAnnotations getSCAnnotations(Class sc) {

        SCAnnotations SCAnnotations = new SCAnnotations();
        ArrayList<QName> portQNames = new ArrayList<QName>();
        if (sc != null) {
            WebServiceClient wsc = (WebServiceClient) sc.getAnnotation(WebServiceClient.class);
            if (wsc != null) {
                String name = wsc.name();
                String tns = wsc.targetNamespace();
                SCAnnotations.tns = tns;
                if (name != null)
                    SCAnnotations.serviceQName = new QName(tns, name);
                SCAnnotations.wsdlLocation = wsc.wsdlLocation();

                Method[] methods = sc.getDeclaredMethods();
                if (methods != null) {
                    ArrayList<Class<?>> classes = new ArrayList<Class<?>>(methods.length);
                    for (final Method method : methods) {

                        AccessController.doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                method.setAccessible(true);
                                return null; // nothing to return
                            }
                        });

                        WebEndpoint webEndpoint = method.getAnnotation(WebEndpoint.class);
                        if (webEndpoint != null) {
                            String endpointName = webEndpoint.name();
                            QName portQName = new QName(tns, endpointName);
                            portQNames.add(portQName);
                        }
                        Class<?> seiClazz = method.getReturnType();
                        if ((seiClazz != null) && (!seiClazz.equals("void"))) {
                            classes.add(seiClazz);
                        }
                    }
                    SCAnnotations.portQNames.addAll(portQNames);
                    SCAnnotations.classes.addAll(classes);
                }
            }
        }
        return SCAnnotations;
    }
}
