/**
 * $Id: SecurityConfigurationImpl.java,v 1.1 2005-05-23 22:47:43 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.security;

import javax.xml.ws.security.SecurityConfiguration;
import javax.xml.ws.WebServiceException;
import javax.security.auth.callback.CallbackHandler;

public class SecurityConfigurationImpl implements SecurityConfiguration {
    public void setInboundConfigId(String s) {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
    }

    public String getInboundConfigId() {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
        //return null;
    }

    public  void setInboundFeatures(SecurityFeature... securityFeatures) {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
    }

    public SecurityFeature[] getInbound() {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
        //return new SecurityFeature[0];
    }

    public  void setOutboundFeatures(SecurityFeature... securityFeatures) {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
    }

    public SecurityFeature[] getOutbound() {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
        //return new SecurityFeature[0];
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
    }

    public CallbackHandler getCallbackHandler() {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
        //return null;
    }

    public void setOutboundConfigId(String s) {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
    }

    public String getOutboundConfigId() {
        throw new WebServiceException("Security is not implemented for JAXRPC 2.0 Early Access.");
        //return null;
    }
}
