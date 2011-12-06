/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.model.wsdl;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of {@link WSDLService}
 *
 * @author Vivek Pandey
 */
public final class WSDLServiceImpl extends AbstractExtensibleImpl implements WSDLService {
    private final QName name;
    private final Map<QName, WSDLPortImpl> ports;
    private final WSDLModelImpl parent;

    public WSDLServiceImpl(XMLStreamReader xsr,WSDLModelImpl parent, QName name) {
        super(xsr);
        this.parent = parent;
        this.name = name;
        ports = new LinkedHashMap<QName,WSDLPortImpl>();
    }

    public @NotNull
    WSDLModelImpl getParent() {
        return parent;
    }

    public QName getName() {
        return name;
    }

    public WSDLPortImpl get(QName portName) {
        return ports.get(portName);
    }

    public WSDLPort getFirstPort() {
        if(ports.isEmpty())
            return null;
        else
            return ports.values().iterator().next();
    }

    public Iterable<WSDLPortImpl> getPorts(){
        return ports.values();
    }

    /**
    * gets the first port in this service which matches the portType
    */
    public @Nullable
    WSDLPortImpl getMatchingPort(QName portTypeName){
        for(WSDLPortImpl port : getPorts()){
            QName ptName = port.getBinding().getPortTypeName();
            assert (ptName != null);
            if(ptName.equals(portTypeName))
                return port;
        }
        return null;
    }

    /**
     * Populates the Map that holds port name as key and {@link WSDLPort} as the value.
     *
     * @param portName Must be non-null
     * @param port     Must be non-null
     * @throws NullPointerException if either opName or ptOp is null
     */
    public void put(QName portName, WSDLPortImpl port) {
        if (portName == null || port == null)
            throw new NullPointerException();
        ports.put(portName, port);
    }

    void freeze(WSDLModelImpl root) {
        for (WSDLPortImpl port : ports.values()) {
            port.freeze(root);
        }
    }
}
