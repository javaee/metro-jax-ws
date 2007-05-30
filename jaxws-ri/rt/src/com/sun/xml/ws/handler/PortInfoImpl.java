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
package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.client.WSServiceDelegate;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.PortInfo;

/**
 * <p>Implementation of the PortInfo interface. This is just a simple
 * class used to hold the info necessary to uniquely identify a port,
 * including the port name, service name, and binding ID. This class
 * is only used on the client side.
 *
 * <p>An instance is created by
 * {@link WSServiceDelegate} when used to
 * place a handler chain into the HandlerResolver map. Another is
 * created later by
 * {@link com.sun.xml.ws.client.WSServiceDelegate} to retrieve the
 * necessary handler chain to set on a binding instance.
 *
 * @see WSServiceDelegate
 * @see com.sun.xml.ws.client.HandlerResolverImpl
 *
 * @author WS Development Team
 */
public class PortInfoImpl implements PortInfo {

    private BindingID bindingId;
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
    public PortInfoImpl(BindingID bindingId, QName portName, QName serviceName) {
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
        return bindingId.toString();
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
            if (bindingId.toString().equals(info.getBindingID()) &&
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
