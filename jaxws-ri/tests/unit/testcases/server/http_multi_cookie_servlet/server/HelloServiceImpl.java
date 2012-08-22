/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
