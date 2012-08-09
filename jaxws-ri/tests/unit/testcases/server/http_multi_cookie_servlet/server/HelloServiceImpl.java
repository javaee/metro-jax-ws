/*
 * $Id: HelloServiceImpl.java,v 1.1 2010-11-20 01:08:54 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_multi_cookie_servlet.server;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

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
        MessageContext mc = wsc.getMessageContext();
        HttpServletResponse sr = (HttpServletResponse)mc.get(MessageContext.SERVLET_RESPONSE);
        Cookie cookie1 = new Cookie("JREPLICA", "instance02");
        sr.addCookie(cookie1);

        Cookie cookie2 = new Cookie("METRO_KEY", "HASHABLE_KEY_264");
        sr.addCookie(cookie2);

        Cookie cookie3 = new Cookie("JROUTE", "kmH+");
        sr.addCookie(cookie3);
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
