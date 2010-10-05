/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.transport.http.client;

import java.net.URL;
import java.util.*;

/**
 * Generic class to hold onto HTTP cookies.  Can record, retrieve, and
 * persistently store cookies associated with particular URLs.
 *
 * @author WS Development Team
 */
public class CookieJar {

    // The representation of cookies is relatively simple right now:
    // a hash table with key being the domain and the value being
    // a vector of cookies for that domain.
    // REMIND: create this on demand in the future
    private transient Hashtable<String,Vector<HttpCookie>> cookieJar = new Hashtable<String, Vector<HttpCookie>>();

    /**
     * Create a new, empty cookie jar.
     */
    public CookieJar() {
    }

    /**
     * Records any cookies which have been sent as part of an HTTP response.
     * The connection parameter must be already have been opened, so that
     * the response headers are available.  It's ok to pass a non-HTTP
     * URL connection, or one which does not have any set-cookie headers.
     */


    public synchronized void recordAnyCookies(URL url, Map<String, List<String>> resHeaders) {
        List<String> cookies = resHeaders.get("Set-Cookie");
        if (cookies != null) {
            for(String cookie : cookies) {
                recordCookie(url, cookie);
            }
        }
    }

    /**
     * Create a cookie from the cookie, and use the HttpURLConnection to
     * fill in unspecified values in the cookie with defaults.
     */
    private void recordCookie(URL url, String cookieValue) {

        HttpCookie cookie = new HttpCookie(url, cookieValue);

        // First, check to make sure the cookie's domain matches the
        // server's, and has the required number of '.'s
        String twodot[] = { "com", "edu", "net", "org", "gov", "mil", "int" };
        String domain = cookie.getDomain();

        if (domain == null) {
            return;
        }

        domain = domain.toLowerCase();

        String host = url.getHost();

        host = host.toLowerCase();

        boolean domainOK = host.equals(domain);

        if (!domainOK && host.endsWith(domain)) {
            int dotsNeeded = 2;

            for (int i = 0; i < twodot.length; i++) {
                if (domain.endsWith(twodot[i])) {
                    dotsNeeded = 1;
                }
            }

            int lastChar = domain.length();

            for (;(lastChar > 0) && (dotsNeeded > 0); dotsNeeded--) {
                lastChar = domain.lastIndexOf('.', lastChar - 1);
            }

            if (lastChar > 0) {
                domainOK = true;
            }
        }

        if (domainOK) {
            recordCookie(cookie);
        }
    }

    /**
     * Record the cookie in the in-memory container of cookies.  If there
     * is already a cookie which is in the exact same domain with the
     * exact same
     */
    private void recordCookie(HttpCookie cookie) {
        recordCookieToJar(cookie, cookieJar, true);
    }


    /**
     * Adds a Cookie for a given URL to the Cookie Jar.
     * <P>
     * New connections to the given URL will include the Cookie.
     * <P>
     * It allows to add Cookie information, to the Cookie jar, received
     * by other mean.
     * <P>
     *
     * @param url the URL to bind the Cookie to.
     *
     * @param cookieHeader String defining the Cookie:
     *        <P>
     *        &lt;name&gt;=&lt;value&gt;[;expires=<WHEN>]
     *        [;path=<PATH>][;domain=<DOMAIN>][;secure]
     *        <P>
     *        Refer to <A HREF=
     *          "http://home.netscape.com/newsref/std/cookie_spec.htm">
     *        Netscape Cookie specification</A> for the complete documentation.
     *
     */
    private void setCookie(URL url, String cookieHeader) {

        HttpCookie cookie = new HttpCookie(url, cookieHeader);

        this.recordCookie(cookie);
    }

    //
    // Records the given cookie to the desired jar.  If doNotify is true,
    // tell globals to inform interested parties.  It *only* makes since for
    // doNotify to be true if jar is the static jar (i.e. Cookies.cookieJar).
    //
    //
    private void recordCookieToJar(
        HttpCookie cookie,
        Hashtable<String,Vector<HttpCookie>> jar,
        boolean doNotify) {

        if (shouldRejectCookie(cookie)) {
            return;
        }

        String domain = cookie.getDomain().toLowerCase();
        Vector<HttpCookie> cookieList = jar.get(domain);

        if (cookieList == null) {
            cookieList = new Vector<HttpCookie>();
        }

        if (addOrReplaceCookie(cookieList, cookie, doNotify)) {
            jar.put(domain, cookieList);
        }
    }

    /**
     * Scans the vector of cookies looking for an exact match with the
     * given cookie.  Replaces it if there is one, otherwise adds
     * one at the end.  The vector is presumed to have cookies which all
     * have the same domain, so the domain of the cookie is not checked.
     * <p>
     * If doNotify is true, we'll do a vetoable notification of changing the
     * cookie.  This <b>only</b> makes since if the jar being operated on
     * is Cookies.cookieJar.
     * <p>
     * If this is called, it is assumed that the cookie jar is exclusively
     * held by the current thread.
     *
     * @return true if the cookie is actually set
     */
    private boolean addOrReplaceCookie(
        Vector<HttpCookie> cookies,
        final HttpCookie cookie,
        boolean doNotify) {

        int numCookies = cookies.size();
        String path = cookie.getPath();
        String name = cookie.getName();
        HttpCookie replaced = null;
        int replacedIndex = -1;

        for (int i = 0; i < numCookies; i++) {
            HttpCookie existingCookie = cookies.elementAt(i);
            String existingPath = existingCookie.getPath();

            if (path.equals(existingPath)) {
                String existingName = existingCookie.getName();

                if (name.equals(existingName)) {

                    // need to replace this one!
                    replaced = existingCookie;
                    replacedIndex = i;

                    break;
                }
            }
        }

        // Do the replace
        if (replaced != null) {
            cookies.setElementAt(cookie, replacedIndex);
        } else {
            cookies.addElement(cookie);
        }

        return true;
    }

    /**
     * Predicate function which returns true if the cookie appears to be
     * invalid somehow and should not be added to the cookie set.
     */
    private boolean shouldRejectCookie(HttpCookie cookie) {

        // REMIND: implement per http-state-mgmt Internet Draft
        return false;
    }

    public synchronized void applyRelevantCookies(URL url, Map<String, List<String>> reqHeaders) {
        String host = url.getHost();

        applyCookiesForHost(host, url, reqHeaders);

        // REMIND: should be careful about IP addresses here.
        int index;

        while ((index = host.indexOf('.', 1)) >= 0) {

            // trim off everything up to, and including the dot.
            host = host.substring(index + 1);

            applyCookiesForHost(host, url, reqHeaders);
        }
    }

    /**
     * Host may be a FQDN, or a partial domain name starting with a dot.
     * Adds any cookies which match the host and path to the
     * cookie set on the URL connection.
     */
    private void applyCookiesForHost(
        String host,
        URL url,
        Map<String, List<String>> reqHeaders) {

        //System.out.println("X0"+cookieJar.size());
        Vector<HttpCookie> cookieList = cookieJar.get(host);

        if (cookieList == null) {

            // Hax.debugln("no matching hosts" + host);
            return;
        }

        //System.out.println("X1"+cookieList.size());
        String path = url.getFile();
        int queryInd = path.indexOf('?');

        if (queryInd > 0) {

            // strip off the part following the ?
            path = path.substring(0, queryInd);
        }

        Enumeration<HttpCookie> cookies = cookieList.elements();
        Vector<HttpCookie> cookiesToSend = new Vector<HttpCookie>(10);

        while (cookies.hasMoreElements()) {
            HttpCookie cookie = cookies.nextElement();
            String cookiePath = cookie.getPath();

            if (path.startsWith(cookiePath)) {

                // larrylf: Actually, my documentation (from Netscape)
                // says that /foo should
                // match /foobar and /foo/bar.  Yuck!!!
                if (!cookie.hasExpired()) {
                    cookiesToSend.addElement(cookie);
                }

                /*
                   We're keeping this piece of commented out code around just in
                   case we decide to put it back.  the spec does specify the above.


                                int cookiePathLen = cookiePath.length();

                                // verify that /foo does not match /foobar by mistake
                                if ((path.length() == cookiePathLen)
                                    || (path.length() > cookiePathLen &&
                                        path.charAt(cookiePathLen) == '/')) {

                                    // We have a matching cookie!

                                    if (!cookie.hasExpired()) {
                                        cookiesToSend.addElement(cookie);
                                    }
                                }
                */
            }
        }

        // Now, sort the cookies in most to least specific order
        // Yes, its the deaded bubblesort!!
        // (it should be a small vector, so perf is not an issue...)
        if (cookiesToSend.size() > 1) {
            for (int i = 0; i < cookiesToSend.size() - 1; i++) {
                HttpCookie headC = cookiesToSend.elementAt(i);
                String head = headC.getPath();

                // This little excercise is a cheap way to get
                // '/foo' to read more specfic then '/'
                if (!head.endsWith("/")) {
                    head = head + "/";
                }

                for (int j = i + 1; j < cookiesToSend.size(); j++) {
                    HttpCookie scanC = cookiesToSend.elementAt(j);
                    String scan = scanC.getPath();

                    if (!scan.endsWith("/")) {
                        scan = scan + "/";
                    }

                    int headCount = 0;
                    int index = -1;

                    while ((index = head.indexOf('/', index + 1)) != -1) {
                        headCount++;
                    }

                    index = -1;

                    int scanCount = 0;

                    while ((index = scan.indexOf('/', index + 1)) != -1) {
                        scanCount++;
                    }

                    if (scanCount > headCount) {
                        cookiesToSend.setElementAt(headC, j);
                        cookiesToSend.setElementAt(scanC, i);

                        headC = scanC;
                        head = scan;
                    }
                }
            }
        }

        // And send the sorted cookies...
        cookies = cookiesToSend.elements();

        String cookieStr = null;

        while (cookies.hasMoreElements()) {
            HttpCookie cookie = cookies.nextElement();

            if (cookieStr == null) {
                cookieStr = cookie.getNameValue();
            } else {
                cookieStr = cookieStr + "; " + cookie.getNameValue();
            }
        }

        if (cookieStr != null) {
            reqHeaders.put("Cookie", Collections.singletonList(cookieStr));
        }
    }
}
