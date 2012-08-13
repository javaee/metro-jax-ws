/*
 * $Id: HelloServiceImpl.java,v 1.2 2008-07-01 22:45:36 jitu Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_session_scope.server;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import javax.jws.WebMethod;
import javax.jws.WebService;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;

import com.sun.xml.ws.developer.servlet.HttpSessionScope;

/**
 * @HttpSessionScope test
 *
 * @author Jitendra Kotamraju
 */
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
@HttpSessionScope
public class HelloServiceImpl {
    int counter = 0;
    public int getCounter() {
        return counter++;
    }
}
