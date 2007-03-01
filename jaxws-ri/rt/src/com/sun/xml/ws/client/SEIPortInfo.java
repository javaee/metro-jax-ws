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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.SOAPBindingImpl;
import com.sun.xml.ws.model.SOAPSEIModel;

import javax.xml.ws.WebServiceFeature;


/**
 * {@link PortInfo} that has {@link SEIModel}.
 *
 * This object is created statically when {@link WSServiceDelegate} is created
 * with an service interface.
 *
 * @author Kohsuke Kawaguchi
 */
final class SEIPortInfo extends PortInfo {
    public final Class sei;
    /**
     * Model of {@link #sei}.
     */
    public final SOAPSEIModel model;

    public SEIPortInfo(WSServiceDelegate owner, Class sei, SOAPSEIModel model, @NotNull WSDLPort portModel) {
        super(owner,portModel);
        this.sei = sei;
        this.model = model;
        assert sei!=null && model!=null;
    }

    public BindingImpl createBinding(WebServiceFeature[] webServiceFeatures, Class<?> portInterface) {
         BindingImpl bindingImpl = super.createBinding(webServiceFeatures,portInterface);
         if(bindingImpl instanceof SOAPBindingImpl) {
            ((SOAPBindingImpl)bindingImpl).setPortKnownHeaders(model.getKnownHeaders());
         }
         //Not needed as set above in super.createBinding() call
         //bindingImpl.setFeatures(webServiceFeatures);
         return bindingImpl;
    }
}
