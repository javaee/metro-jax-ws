/*
 * $Id: BasicAuthTest.java,v 1.1 2009-10-01 20:21:45 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.basic_auth.client;

import java.util.Map;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.Service;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * HTTP Basic Auth test
 *
 * @author Jitendra Kotamraju
 */
public class BasicAuthTest extends TestCase {

    public BasicAuthTest(String name) {
        super(name);
    }

    /*
     * Tests Standard HTTP Authorization header on server side
     */
    public void testHttpMsgCtxt() throws Exception {
        Hello proxy = new HelloService().getHelloPort();
        BindingProvider bp = (BindingProvider)proxy;
        bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "auth-user");
        bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "auth-pass");
        proxy.testHttpProperties();
    }
    
}
