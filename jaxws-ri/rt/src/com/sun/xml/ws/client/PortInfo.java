/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.client;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.policy.PolicyResolverFactory;
import com.sun.xml.ws.api.policy.PolicyResolver;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

/**
 * Information about a port.
 * <p/>
 * This object is owned by {@link WSServiceDelegate} to keep track of a port,
 * since a port maybe added dynamically.
 *
 * @author JAXWS Development Team
 */
public class PortInfo implements WSPortInfo {
    private final @NotNull WSServiceDelegate owner;

    public final @NotNull QName portName;
    public final @NotNull EndpointAddress targetEndpoint;
    public final @NotNull BindingID bindingId;

    public final @NotNull PolicyMap policyMap;
    /**
     * If a port is known statically to a WSDL, {@link PortInfo} may
     * have the corresponding WSDL model. This would occur when the
     * service was created with the WSDL location and the port is defined
     * in the WSDL.
     * <p/>
     * If this is a {@link SEIPortInfo}, then this is always non-null.
     */
    public final @Nullable WSDLPort portModel;

    public PortInfo(WSServiceDelegate owner, EndpointAddress targetEndpoint, QName name, BindingID bindingId) {
        this.owner = owner;
        this.targetEndpoint = targetEndpoint;
        this.portName = name;
        this.bindingId = bindingId;
        this.portModel = getPortModel(owner, name);
        this.policyMap = createPolicyMap();

    }

    public PortInfo(@NotNull WSServiceDelegate owner, @NotNull WSDLPort port) {
        this.owner = owner;
        this.targetEndpoint = port.getAddress();
        this.portName = port.getName();
        this.bindingId = port.getBinding().getBindingId();
        this.portModel = port;
        this.policyMap = createPolicyMap();
    }

    public PolicyMap getPolicyMap() {
        return policyMap;
    }

    public PolicyMap createPolicyMap() {
       PolicyMap map;
       if(portModel != null) {
            map = ((WSDLModelImpl) portModel.getOwner().getParent()).getPolicyMap();
       } else {
           map = PolicyResolverFactory.create().resolve(new PolicyResolver.ClientContext(null,owner.getContainer()));
       }
       //still map is null, create a empty map
       if(map == null)
           map = PolicyMap.createPolicyMap(null);
       return map;
    }
    /**
     * Creates {@link BindingImpl} for this {@link PortInfo}.
     *
     * @param webServiceFeatures
     *      User-specified features.
     * @param portInterface
     *      Null if this is for dispatch. Otherwise the interface the proxy is going to implement
     * @return
     *      The initialized BindingImpl
     */
    public BindingImpl createBinding(WebServiceFeature[] webServiceFeatures, Class<?> portInterface) {
        WebServiceFeatureList r = new WebServiceFeatureList(webServiceFeatures);

        Iterable<WebServiceFeature> configFeatures;

        //TODO incase of Dispatch, provide a way to User for complete control of the message processing by giving
        // ability to turn off the WSDL/Policy based features and its associated tubes.

        //Even in case of Dispatch, merge all features configured via WSDL/Policy or deployment configuration
        if (portModel != null) {
            // could have merged features from this.policyMap, but some features are set in WSDLModel which are not there in PolicyMap
            // for ex: <wsaw:UsingAddressing> wsdl extn., and since the policyMap features are merged into WSDLModel anyway during postFinished(),
            // So, using here WsdlModel for merging is right.

            // merge features from WSDL
            configFeatures = portModel.getFeatures();
        } else {
            configFeatures = PolicyUtil.getPortScopedFeatures(policyMap, owner.getServiceName(),portName);
        }
        r.mergeFeatures(portModel.getFeatures(), false);

        // merge features from interceptor
        r.mergeFeatures(owner.serviceInterceptor.preCreateBinding(this,portInterface,r), false);

        BindingImpl bindingImpl = BindingImpl.create(bindingId, r.toArray());
        owner.getHandlerConfigurator().configureHandlers(this,bindingImpl);

        return bindingImpl;
    }

    //This method is used for Dispatch client only
    private WSDLPort getPortModel(WSServiceDelegate owner, QName portName) {

        if (owner.getWsdlService() != null){
            Iterable<WSDLPortImpl> ports = owner.getWsdlService().getPorts();
            for (WSDLPortImpl port : ports){
                if (port.getName().equals(portName))
                    return port;                
            }
        }
        return null;
    }

//
// implementation of API PortInfo interface
//

    @Nullable
    public WSDLPort getPort() {
        return portModel;
    }

    @NotNull
    public WSService getOwner() {
        return owner;
    }

    @NotNull
    public BindingID getBindingId() {
        return bindingId;
    }

    @NotNull
    public EndpointAddress getEndpointAddress() {
        return targetEndpoint;
    }

    /**
     * @deprecated
     *      Only meant to be used via {@link javax.xml.ws.handler.PortInfo}.
     *      Use {@link WSServiceDelegate#getServiceName()}.
     */
    public QName getServiceName() {
        return owner.getServiceName();
    }

    /**
     * @deprecated
     *      Only meant to be used via {@link javax.xml.ws.handler.PortInfo}.
     *      Use {@link #portName}.
     */
    public QName getPortName() {
        return portName;
    }

    /**
     * @deprecated
     *      Only meant to be used via {@link javax.xml.ws.handler.PortInfo}.
     *      Use {@link #bindingId}.
     */
    public String getBindingID() {
        return bindingId.toString();
    }
}

