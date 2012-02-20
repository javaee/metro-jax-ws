/*
 * $Id: HelloServiceImpl.java,v 1.1 2009-10-01 20:21:46 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.basic_auth.server;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;

import javax.annotation.Resource;

import javax.jws.WebMethod;
import javax.jws.WebService;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;

/**
 * HTTP basic auth test
 *
 * @author Jitendra Kotamraju
 */
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
public class HelloServiceImpl {
    
    @Resource
    private WebServiceContext wsc;

    @WebMethod
    public void testHttpProperties() {
        MessageContext ctxt = wsc.getMessageContext();
        Map<String, List<String>> headers = (Map<String, List<String>>)ctxt.get(MessageContext.HTTP_REQUEST_HEADERS);
        if (headers == null) {
            throw new WebServiceException("HTTP_HEADERS is not populated");
        }
        List<String> authHeader = headers.get("Authorization");
        if (authHeader == null) { 
            throw new WebServiceException("No Authorization Header="+authHeader);
        }
        if (authHeader.size() != 1) { 
            throw new WebServiceException("Incorrect Authorization Header="+authHeader);
        }
        String expected = "Basic YXV0aC11c2VyOmF1dGgtcGFzcw==";
        String got = authHeader.get(0);
        if (got == null || !got.equals(expected)) {
            throw new WebServiceException("Authorization value expected="+expected+" got="+got);
        }
    }
    
}
