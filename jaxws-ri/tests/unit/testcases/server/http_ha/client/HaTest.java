/*
 * $Id: HaTest.java,v 1.1 2010-11-20 01:08:54 jitu Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_ha.client;

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
public class HaTest extends TestCase {

    public HaTest(String name) {
        super(name);
    }

    public void test1() throws Exception {
        Hello proxy = new HelloService().getHelloPort();
        Map<String, List<String>> hdrs = new HashMap<String, List<String>>();
        hdrs.put("proxy-jroute", Collections.singletonList("instance2"));
        hdrs.put("Cookie", Collections.singletonList("METRO_KEY=key1;JREPLICA=replica1;JROUTE=instance1"));
        ((BindingProvider)proxy).getRequestContext().put(
            MessageContext.HTTP_REQUEST_HEADERS, hdrs);
        proxy.testHa();
    }
    
}
