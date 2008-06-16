/*
 * $Id: HelloServiceImpl.java,v 1.1 2008-06-16 23:15:01 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_session_scope.server;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;

import javax.jws.WebMethod;
import javax.jws.WebService;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

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
