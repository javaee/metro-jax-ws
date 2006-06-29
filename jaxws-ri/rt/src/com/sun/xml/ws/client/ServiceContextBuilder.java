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

        if (wsdlLocation != null){
            WSDLContext wsCtx = new WSDLContext(wsdlLocation, er);

            //check if the serviceName is a valid one, if its not in the given WSDL fail
            if(!wsCtx.contains(serviceName))
                throw new ClientConfigurationException("service.invalidServiceName", serviceName, wsdlLocation);

            serviceContext.setWsdlContext(wsCtx);
        }
        
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

    public static void completeServiceContext(QName portName, ServiceContext serviceContext, Class portInterface) {
        if (portInterface != null)
            processAnnotations(portName, serviceContext, portInterface);
    }

    private static void processAnnotations(QName portName, ServiceContext serviceContext, Class portInterface) throws WebServiceException {
        WSDLContext wsdlContext = serviceContext.getWsdlContext();
        EndpointIFContext eifc = serviceContext.getEndpointIFContext(portInterface.getName());
        if ((eifc != null) && (eifc.getRuntimeContext() != null)) {
            return;
        }
        if (eifc == null) {
            eifc = new EndpointIFContext(portInterface);
            serviceContext.addEndpointIFContext(eifc);
        }

        QName serviceName = serviceContext.getServiceName();

        //if portName is null get it from the WSDL
        if (portName == null) {
            //get the first port corresponding to the SEI
            QName portTypeName = RuntimeModeler.getPortTypeName(portInterface);
            portName = wsdlContext.getWsdlDocument().getPortName(serviceContext.getServiceName(), portTypeName);
        }

        //still no portName, fail
        if(portName == null)
            throw new ClientConfigurationException("service.noPortName", portInterface.getName(), wsdlContext.getWsdlLocation().toString());

        eifc.setPortName(portName);
        String bindingId = wsdlContext.getBindingID(serviceName, portName);
        RuntimeModeler modeler = new RuntimeModeler(portInterface,
            serviceName, bindingId);
        modeler.setPortName(portName);
        RuntimeModel model = modeler.buildRuntimeModel();

        eifc.setRuntimeContext(new RuntimeContext(model));
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
