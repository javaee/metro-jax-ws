/*
 * $Id: HttpSessionScopeTest.java,v 1.1 2008-06-16 23:15:00 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_session_scope.client;

import java.util.Map;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.Service;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * HttpSessionScope test
 *
 * @author Jitendra Kotamraju
 */
public class HttpSessionScopeTest extends TestCase {

    public HttpSessionScopeTest(String name) {
        super(name);
    }

    /*
     * Without setting the maintain property, session
     * should not be maintained.
     */
    public void test1() throws Exception {
        Hello proxy = new HelloService().getHelloPort();
        assertEquals(0, proxy.getCounter());
        assertEquals(0, proxy.getCounter());
    }
    
    /*
     * With maintain property set to false, session
     * should not be maintained.
     */
    public void test2() throws Exception {
        Hello proxy = new HelloService().getHelloPort();
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.FALSE);
        assertEquals(0, proxy.getCounter());
        assertEquals(0, proxy.getCounter());
    }
    
    /*
     * With maintain property set to true, session
     * should be maintained.
     */
    public void test3() throws Exception {
        Hello proxy = new HelloService().getHelloPort();
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        assertEquals(0, proxy.getCounter());
        assertEquals(1, proxy.getCounter());
        assertEquals(2, proxy.getCounter());
    }

    
}
