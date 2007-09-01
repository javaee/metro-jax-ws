/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.wsdl_hello_lit_context.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.WebServiceException;
import javax.jws.HandlerChain;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider(serviceName="Hello", portName="HelloPort",
    targetNamespace="urn:test",
    wsdlLocation="WEB-INF/wsdl/hello_literal_overridden.wsdl")
@HandlerChain(file="handlers.xml")
public class Hello_PortType_Impl extends ProviderImpl {
    @Resource(type=Object.class)
    protected WebServiceContext wsContext;

    private boolean injectionDone;

    public WebServiceContext getContext() {
        return wsContext;
    }

    @PostConstruct
    public void over() {
        System.out.println("PostConstruct Complete");
        injectionDone = true;
        boolean illegal = false;
        try {
        	wsContext.getMessageContext();
        } catch(IllegalStateException ie) {
			// No op. Expected to get this exception
            illegal = true;
        }
        if (!illegal) {
            throw new WebServiceException("IllegalStateException is not called");
        }
    }

    @PreDestroy
    public void destroy() {
        System.out.println("PreDestroy is called");
        boolean illegal = false;
        try {
        	wsContext.getMessageContext();
        } catch(IllegalStateException ie) {
			// No op. Expected to get this exception
            illegal = true;
        }
        if (!illegal) {
            throw new WebServiceException("IllegalStateException is not called");
        }
    }

    public boolean isInjectionDone() {
        return injectionDone;
    }
}
