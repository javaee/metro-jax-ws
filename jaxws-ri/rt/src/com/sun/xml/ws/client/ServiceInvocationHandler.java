/*
 * $Id: ServiceInvocationHandler.java,v 1.5 2005-07-28 21:03:05 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;


/**
 * $author: JAXWS Development Team
 */
public class ServiceInvocationHandler extends WebService
    implements InvocationHandler {

    public ServiceInvocationHandler(ServiceContext serviceContext) {
        super(serviceContext);
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

        try {
            if (isSIMethod(method, serviceContext.getServiceInterface())) {
                return invokeSIMethod(method, args);
            } else {
                return method.invoke(this, args);
            }
        } catch (java.lang.reflect.UndeclaredThrowableException ex) {
            throw new WebServiceException(ex.getMessage(), ex.getCause());
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw new WebServiceException(ex.getMessage(), ex.getCause());
        } catch (java.lang.reflect.GenericSignatureFormatError ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        } catch (java.lang.reflect.MalformedParameterizedTypeException ex) {
            throw new WebServiceException(ex.getMessage(), ex);
        }
    }

    private Object invokeSIMethod(Method method, Object[] args)
        throws WebServiceException {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }

        return getXXXPort(method);
    }

    private Object getXXXPort(Method method) throws WebServiceException {
        String methodName = method.getName();
        Class returnType = method.getReturnType();
        String portLocalName = null;

        if (returnType != null) {
            portLocalName = getPortName(methodName, returnType.getSimpleName());

            if (portLocalName != null) {
                return getPort(returnType, portLocalName);
            } else {
                throw new WebServiceException("port name undefined, must have port name");
            }
        } else {
            throw new WebServiceException("No Return type, " +
                method.getName() + "must have a return Class");
        }
    }

    private Object getPort(Class returnType, String portName)
        throws WebServiceException {
        Class sei = null;
        Object port = null;

        QName portQName = validatePortName(portName);

        if (returnType.isInterface()) {
            try {
                sei = Thread.currentThread().getContextClassLoader().loadClass(returnType.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                if (sei != null) {
                    String ns = serviceContext.getServiceName().getNamespaceURI();
                    port = getPort(portQName, sei);

                    if (port == null) {
                        throw new WebServiceException("Unable to create Port");
                    }
                } else {
                    throw new WebServiceException("No serviceEndpointInterface Class found, Unable to create Port.");
                }
            } catch (RuntimeException rex) {
                throw new WebServiceException("Error creating dynamic stub", rex);
            }
        }

        return port;
    }

    public void setProxy(Proxy proxy) {
        serviceProxy = proxy;
    }

    private boolean isSIMethod(Method method, Class si) {
        return (si.equals(method.getDeclaringClass())) ? true : false;
    }

    //TODO: will require rework
    private String getPortName(String mName, String rtName) {
        //can only check to see if method is a getter.
        //if so the assumption is that rt type name is the portname.
        //this portname is currently needed when sei proxy is
        //created as the handler registry requires a known port on creation.
        //The handler registry is instantiated when sei proxy is generated.
        //assumption rt name is used in method name
        if (rtName.indexOf(mName) != 0) {
            //just check to make sure method is a getter
            if (mName.startsWith(GET, 0)) {
                return mName.substring(3, mName.length());
            }
        }

        return rtName;
    }

    QName validatePortName(String portName) {
        Set<QName> validPort = serviceContext.getWsdlContext().contains(serviceContext.getServiceName(),
            new QName(serviceContext.getServiceName().getNamespaceURI(), portName));

        if (validPort.size() == 0) {
            throw new WebServiceException("Port with name " + portName + " is not a valid Port");
        }

        if (validPort.size() == 1) {
            return serviceContext.getWsdlContext().getPortName();
        }

        if (validPort.size() > 1) {
            String portNS = serviceContext.getWsdlContext().getPortName().getNamespaceURI();
            return new QName(portNS, portName);
        }
        return null;
    }
}