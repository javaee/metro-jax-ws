/**
 * $Id: PortInfoImpl.java,v 1.1 2005-08-26 21:42:19 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.handler;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.PortInfo;

/**
 *
 * @author WS Development Team
 */
public class PortInfoImpl implements PortInfo {
    
    private String bindingId;
    private QName portName;
    private QName serviceName;
        
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

    public int hashCode() {
        return bindingId.hashCode();
    }
    
}
