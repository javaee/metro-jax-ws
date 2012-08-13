/*
 * $Id: HelloServiceImpl.java,v 1.1 2010-11-20 01:08:54 jitu Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_multi_cookie_portable.server;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.util.*;

/**
 * Making sure that cookies are returned by client
 *
 * @author Jitendra Kotamraju
 */
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
public class HelloServiceImpl {
    
    @Resource
    private WebServiceContext wsc;

    public void introduce() {
        Map<String, List<String>> hdrs = new HashMap<String, List<String>>();
        List<String> cookies = new ArrayList<String>();
        cookies.add("JREPLICA=instance02");
        cookies.add("METRO_KEY=HASHABLE_KEY_264");
        cookies.add("JROUTE=kmH+");
        hdrs.put("Set-Cookie", cookies);
        MessageContext mc = wsc.getMessageContext();
        mc.put(MessageContext.HTTP_RESPONSE_HEADERS, hdrs);
    }

    public boolean rememberMe() {
        MessageContext mc = wsc.getMessageContext();
        Map<String, List<String>> hdrs = (Map<String, List<String>>)mc.get(MessageContext.HTTP_REQUEST_HEADERS);
        List<String> cookieList = hdrs.get("Cookie");
        int noOfCookies = 0;
        System.out.println("******* server cookieList ********"+cookieList);
        for(String cookie : cookieList) {
            if (cookie.equals("JREPLICA=instance02")) {
                noOfCookies++;
            } else if (cookie.equals("METRO_KEY=HASHABLE_KEY_264")) {
                noOfCookies++;
            } else if (cookie.equals("JROUTE=kmH+")) {
                noOfCookies++;
            }
        }
        if (noOfCookies != 3) {
            throw new WebServiceException("Didn't receive all the cookies. Received:"+cookieList);
        }
        return true;
    }
}
