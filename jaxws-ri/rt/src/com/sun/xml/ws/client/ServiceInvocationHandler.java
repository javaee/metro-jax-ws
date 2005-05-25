/*
 * $Id: ServiceInvocationHandler.java,v 1.2 2005-05-25 20:44:09 kohlert Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.server.RuntimeContext;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;

/**
 * $author: JAXWS Development Team
 */
public class ServiceInvocationHandler extends WebService implements InvocationHandler {

    public ServiceInvocationHandler(RuntimeContext context, Class si, URL wsdlDocumentLocation) {
        super(context, si, wsdlDocumentLocation);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isSIMethod(method, si))
            return invokeSIMethod(method, args);
        else
            return method.invoke(this, args);
    }

    public Object invokeSIMethod(Method method, Object[] args) throws WebServiceException {
        if (!method.isAccessible())
            method.setAccessible(true);
        return getXXXPort(method);
    }

    private Object getXXXPort(Method method) throws WebServiceException {

        String methodName = method.getName();
        Class returnType = method.getReturnType();
        String portName = null;
        if (returnType != null) {
            portName = getPortName(methodName, returnType.getSimpleName());
            if (portName != null)
                return getPort(returnType, portName);
        } else
            throw new WebServiceException("No Return type, " + method.getName() + "must have a return Class");
        return null;
    }

    private Object getPort(Class returnType, String portName) throws WebServiceException {
        Class sei = null;
        Object port = null;
        if (returnType.isInterface()) {
            try {
                sei = Thread.currentThread().getContextClassLoader().loadClass(returnType.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                if (sei != null) {
                    port = getPort(new QName("", portName), sei);
                    if (port == null)
                        throw new WebServiceException("Unable to create Port");
                } else
                    throw new WebServiceException("No serviceEndpointInterface Class found, Unable to create Port.");
            } catch (RuntimeException rex) {
                throw new WebServiceException("Error creating dynamic stub");
            }
        }
        return port;
    }

    public void setProxy(Proxy proxy) {
        serviceProxy = proxy;
    }

    boolean isSIMethod(Method method, Class si) {
        return (si.equals(method.getDeclaringClass())) ? true : false;
    }

    //TODO: bug- will be portName in wsdl not necessarily PORT
    String getPortName(String mName, String rtName) {
        String mngName = mName.substring(GET_LEN);
        if (rtName.indexOf(mName) != 0) {
            if (mName.startsWith(GET, 0)) {
                //tbd
                if (mName.indexOf(PORT) != 0) {
                    int mLen = mName.length();
                    if ((mLen - EXCLUDE_LEN) <= rtName.length()) {
                        //we are looking good here -
                        String mpName = mName.substring(GET_LEN, mLen - PORT_LEN);
                        //if (mpName.equals(rtName))
                        return mpName;
                    }
                }

            }
        }
        return null;
    }

}
