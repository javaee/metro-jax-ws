/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.util.RuntimeVersion;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import java.net.URL;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * @author Harold Carr
 */
@ManagedObject
@Description("Metro Web Service endpoint")
@AMXMetadata(type="WSEndpoint")
public final class MonitorRootService extends MonitorBase {

    private final WSEndpoint endpoint;

    MonitorRootService(final WSEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    //
    // Items from WSEndpoint
    //

    @ManagedAttribute
    @Description("Policy associated with Endpoint")
    public String policy() {
        return endpoint.getPolicyMap() != null ?
               endpoint.getPolicyMap().toString() : null;
    }

    @ManagedAttribute
    @Description("Container")
    public @NotNull Container container() {
        return endpoint.getContainer();
    }


    @ManagedAttribute
    @Description("Port name")
    public @NotNull QName portName() {
        return endpoint.getPortName();
    }

    @ManagedAttribute
    @Description("Service name")
    public @NotNull QName serviceName() {
        return endpoint.getServiceName();
    }

    //
    // Items from WSBinding
    //

    @ManagedAttribute
    @Description("Binding SOAP Version")
    public String soapVersionHttpBindingId() {
        return endpoint.getBinding().getSOAPVersion().httpBindingId;
    }

    @ManagedAttribute
    @Description("Binding Addressing Version")
    public AddressingVersion addressingVersion() {
        return endpoint.getBinding().getAddressingVersion();
    }

    @ManagedAttribute
    @Description("Binding Identifier")
    public @NotNull BindingID bindingID() {
        return endpoint.getBinding().getBindingId();
    }

    @ManagedAttribute
    @Description("Binding features")
    public @NotNull WSFeatureList features() {
        return endpoint.getBinding().getFeatures();
    }

    //
    // Items from WSDLPort
    //

    @ManagedAttribute
    @Description("WSDLPort bound port type")
    public QName wsdlPortTypeName() {
        return endpoint.getPort() != null ?
               endpoint.getPort().getBinding().getPortTypeName() : null;
    }

    @ManagedAttribute
    @Description("Endpoint address")
    public EndpointAddress wsdlEndpointAddress() {
        return endpoint.getPort() != null ?
               endpoint.getPort().getAddress() : null;
    }

    //
    // Items from ServiceDefinition
    //

    @ManagedAttribute
    @Description("Documents referenced")
    public Set<String> serviceDefinitionImports() {
        return endpoint.getServiceDefinition() != null ?
               endpoint.getServiceDefinition().getPrimary().getImports() : null;
    }

    @ManagedAttribute
    @Description("System ID where document is taken from")
    public URL serviceDefinitionURL() {
        return endpoint.getServiceDefinition() != null ?
               endpoint.getServiceDefinition().getPrimary().getURL() : null;
    }

    //
    // Items from SEIModel
    //

    @ManagedAttribute
    @Description("SEI model WSDL location")
    public String seiModelWSDLLocation() {
        return endpoint.getSEIModel() != null ? 
               endpoint.getSEIModel().getWSDLLocation() : null;
    }

    //
    // Items from RuntimeVersion
    //

    @ManagedAttribute
    @Description("JAX-WS runtime version")
    public String jaxwsRuntimeVersion() {
        return RuntimeVersion.VERSION.toString();
    }

    //
    // Items from HttpAdapter
    //

    @ManagedAttribute
    @Description("If true: show what goes across HTTP transport")
    public boolean dumpHTTPMessages() { return HttpAdapter.dump; }


    @ManagedAttribute
    @Description("Show what goes across HTTP transport")
    public void dumpHTTPMessages(final boolean x) { HttpAdapter.setDump(x); }

}

// End of file.
