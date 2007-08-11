/*
 * $Id: TestClient.java,v 1.1 2007-08-11 00:57:07 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_only.client;

import java.util.Map;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.Service;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import testutil.ClientServerTestUtil;

/**
 * @author Jitendra Kotamraju
 */
public class TestClient extends TestCase {

    @WebServiceRef
    HelloPortType proxy;
    
    public TestClient(String name) {
        super(name);
    }

    /*
     * Without setting the maintain property, session
     * should not be maintained.
     */
    public void test1() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        proxy.introduce();
        assertFalse("client session should not be maintained",
            proxy.rememberMe());
    }
    
    /*
     * With maintain property set to false, session
     * should not be maintained.
     */
    public void test2() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.FALSE);
        proxy.introduce();
        assertFalse("client session should not be maintained",
            proxy.rememberMe());
    }
    
    /*
     * With maintain property set to true, session
     * should be maintained.
     */
    public void test3() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
        proxy.introduce();
        assertTrue("client session should be maintained", proxy.rememberMe());
    }

    /*
     * Tests Standard Servlet MessageContext properties
     */
    public void testServletMsgCtxt() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        proxy.testServletProperties();
    }

    /*
     * Tests Standard HTTP MessageContext properties on server side
     */
    public void testHttpMsgCtxt() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        proxy.testHttpProperties();
    }

    /*
     * Tests Standard HTTP MessageContext properties on client side
     */
    public void testClientHttpMsgCtxt() throws Exception {
        if (ClientServerTestUtil.useLocal()) {
            return;
        }
        proxy.introduce();
        Map<String, Object> responseContext =
            ((BindingProvider) proxy).getResponseContext();
        Integer code = (Integer)responseContext.get(MessageContext.HTTP_RESPONSE_CODE);
        assertTrue(code != null);
        assertEquals((int)code, 200);
        Map<String, List<String>> headers =
            (Map<String, List<String>>)responseContext.get(MessageContext.HTTP_RESPONSE_HEADERS);
        assertTrue(headers != null);
        System.out.println("Headers="+headers);
    }
    
}
