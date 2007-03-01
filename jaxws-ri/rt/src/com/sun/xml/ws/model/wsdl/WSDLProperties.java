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
package com.sun.xml.ws.model.wsdl;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.PropertySet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

/**
 * Properties exposed from {@link WSDLPort} for {@link MessageContext}.
 * Donot add this satellite if {@link WSDLPort} is null.
 *
 * @author Jitendra Kotamraju
 */
public final class WSDLProperties extends PropertySet {

    private static final PropertyMap model;
    static {
        model = parse(WSDLProperties.class);
    }

    private final @NotNull WSDLPort port;

    public WSDLProperties(@NotNull WSDLPort port) {
        this.port = port;
    }

    @Property(MessageContext.WSDL_SERVICE)
    public QName getWSDLService() {
        return port.getOwner().getName();
    }

    @Property(MessageContext.WSDL_PORT)
    public QName getWSDLPort() {
        return port.getName();
    }

    @Property(MessageContext.WSDL_INTERFACE)
    public QName getWSDLPortType() {
        return port.getBinding().getPortTypeName();
    }

    @Override
    protected PropertyMap getPropertyMap() {
        return model;
    }

}
