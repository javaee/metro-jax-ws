/*
 * $Id: HaTest.java,v 1.1 2010-11-20 01:08:54 jitu Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_multi_cookie_servlet.client;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.Service;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * HTTP HA test
 *
 * @author Jitendra Kotamraju
 */
public class MultiCookieTest extends TestCase {


    public MultiCookieTest(String name) {
        super(name);
    }

    /*
    * With maintain property set to true, session
    * should be maintained.
    */
    public void test3() throws Exception {
        Hello proxy = new HelloService().getHelloPort();

        // Set the adress with upper case hostname
        Map<String, Object> requestContext =
            ((BindingProvider) proxy).getRequestContext();
        requestContext.put(
            BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);

        proxy.introduce();
        assertTrue("client session should be maintained", proxy.rememberMe());
    }
    
}
