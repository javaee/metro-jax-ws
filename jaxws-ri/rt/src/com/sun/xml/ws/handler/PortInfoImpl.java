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
package com.sun.xml.ws.handler;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.PortInfo;

/**
 * <p>Implementation of the PortInfo interface. This is just a simple
 * class used to hold the info necessary to uniquely identify a port,
 * including the port name, service name, and binding ID. This class
 * is only used on the client side.
 *
 * <p>An instance is created by
 * {@link com.sun.xml.ws.client.ServiceContextBuilder} when used to
 * place a handler chain into the HandlerResolver map. Another is
 * created later by
 * {@link com.sun.xml.ws.client.WSServiceDelegate} to retrieve the
 * necessary handler chain to set on a binding instance.
 *
 * @see com.sun.xml.ws.client.ServiceContextBuilder
 * @see com.sun.xml.ws.client.WSServiceDelegate
 * @see HandlerResolverImpl
 *
 * @author WS Development Team
 */
public class PortInfoImpl implements PortInfo {
    
    private String bindingId;
    private QName portName;
    private QName serviceName;
        
    /**
     * The class is constructed with the information needed to identify
     * a port. This information cannot be changed later.
     *
     * @param bindingId The binding ID string.
     * @param portName The QName of the port.
     * @param serviceName The QName of the service.
     */
    public PortInfoImpl(String bindingId, QName portName, QName serviceName) {
        if (bindingId == null) {
            throw new RuntimeException("bindingId cannot be null");
        }
        if (portName == null) {
            throw new RuntimeException("portName cannot be null");
        }
        if (serviceName == null) {
            throw new RuntimeException("serviceName cannot be null");
        }
        this.bindingId = bindingId;
        this.portName = portName;
        this.serviceName = serviceName;
    }

    public String getBindingID() {
        return bindingId;
    }

    public QName getPortName() {
        return portName;
    }

    public QName getServiceName() {
        return serviceName;
    }
    
    /**
     * Object.equals is overridden here so that PortInfo objects
     * can be compared when using them as keys in the map in
     * HandlerResolverImpl. This method relies on the equals()
     * methods of java.lang.String and javax.xml.namespace.QName.
     *
     * @param obj The PortInfo object to test for equality.
     * @return True if they match, and false if they do not or
     * if the object passed in is not a PortInfo.
     */
    public boolean equals(Object obj) {
        if (obj instanceof PortInfo) {
            PortInfo info = (PortInfo) obj;
            if (bindingId.equals(info.getBindingID()) &&
                portName.equals(info.getPortName()) &&
                serviceName.equals(info.getServiceName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Needed so PortInfoImpl can be used as a key in a map. This
     * method just delegates to the hashCode method of java.lang.String.
     */
    public int hashCode() {
        return bindingId.hashCode();
    }
    
}
