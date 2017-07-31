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

package com.sun.xml.ws.transport.async_client_transport;

import com.oracle.webservices.api.message.MessageContextFactory;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.istack.NotNull;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.WebServiceFeature;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Supresses a feature from WSBinding 
 * @author Rama.Pulavarthi@sun.com
 */
public class FeatureSupressingWSBinding implements WSBinding {
    WSBinding original;
    final WSFeatureList newFtrs;
    public FeatureSupressingWSBinding(Class<? extends WebServiceFeature> supressedftr, WSBinding binding) {
        this.original = binding;
        WebServiceFeature[] origFtrs= original.getFeatures().toArray();
        List<WebServiceFeature> newFtrList =  new ArrayList<WebServiceFeature>();
        for(WebServiceFeature ftr: origFtrs) {
            if(!ftr.getClass().equals(supressedftr)) {
                newFtrList.add(ftr);
            }
        }
        newFtrs = new WebServiceFeatureList(newFtrList.toArray(new WebServiceFeature[newFtrList.size()]));
    }

    @Override
    public SOAPVersion getSOAPVersion() {
        return original.getSOAPVersion();
    }

    @Override
    public AddressingVersion getAddressingVersion() {
        return original.getAddressingVersion();
    }

    @Override
    public BindingID getBindingId() {
        return original.getBindingId();
    }

    @Override
    public List<Handler> getHandlerChain() {
        return original.getHandlerChain();
    }

    @Override
    public void setHandlerChain(List<Handler> chain) {
        original.setHandlerChain(chain);
    }
    
    @Override
    public Set<QName> getKnownHeaders() {
    	return original.getKnownHeaders();
    }
    
    @Override
    public boolean addKnownHeader(QName knownHeader) {
        return original.addKnownHeader(knownHeader);
    }

    @Override
    public String getBindingID() {
        return original.getBindingID();
    }

    @Override
    public boolean isFeatureEnabled(@NotNull Class<? extends WebServiceFeature> feature) {
        return newFtrs.isEnabled(feature);
    }

    @Override
    public <F extends WebServiceFeature> F getFeature(@NotNull Class<F> featureType) {
        return newFtrs.get(featureType);
    }

    @Override
    public WSFeatureList getFeatures() {
        return newFtrs;
    }

    @Override
    public boolean isOperationFeatureEnabled(Class<? extends WebServiceFeature> type, QName qname) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <F extends WebServiceFeature> F getOperationFeature(Class<F> type, QName qname) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WSFeatureList getOperationFeatures(QName qname) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WSFeatureList getInputMessageFeatures(QName qname) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WSFeatureList getOutputMessageFeatures(QName qname) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WSFeatureList getFaultMessageFeatures(QName qname, QName qname1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageContextFactory getMessageContextFactory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
