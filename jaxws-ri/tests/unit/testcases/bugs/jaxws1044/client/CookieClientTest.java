/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package bugs.jaxws1044.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.util.*;
import java.util.logging.Logger;

/**
 * TODO: Write some description here ...
 *
 * @author Miroslav Kos (miroslav.kos at oracle.com)
 */
public class CookieClientTest extends TestCase {

    static final Logger logger = Logger.getLogger(CookieClientTest.class.getName());

    CookieAService portA = new CookieAServiceService().getA();
    int call;

    String[][] expectedCookies = {
            {"C=C0", "S0=S0"},
            {"C=C0", "S0=S0", "S1=S1"},
            {"C=C0", "S0=S0", "S1=S1", "S2=S2"},

            {"C=C1"},
            {"C=C1"},
            {"C=C1"},

            {"C=C2"},
            {"C=C2"},
            {"C=C2"},
    };

    public void test() {

        logger.fine("CookieClientTest.test");
        ((BindingProvider) portA).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        invokeA();
        invokeA();
        invokeA();

        invokeA();
        invokeA();
        invokeA();

        invokeA();
        invokeA();
        invokeA();
    }

    @SuppressWarnings("unchecked")
    private void invokeA() {

        // set cookie once per 3 calls:
        if (call % 3 == 0) {
            String cookie = "C" + "=C" + call / 3;
            logger.fine(" >> Client: " + cookie);
            setCookie((BindingProvider) portA, cookie);
        }

        portA.operation();

        // list cookies
        List<String> cookies = (List<String>) ((Map) ((BindingProvider) portA).getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS)).get("Set-Cookie");
        logger.fine(" << Client: " + cookies + "\n\n");
        checkExpectedCookies(cookies);

        call++;
    }

    private void checkExpectedCookies(List<String> cookies) {
        List<String> expected = Arrays.asList(expectedCookies[call]);
        assertTrue("Didn't get all the expected cookies from the service (call #" + call + ").\n" +
                "  expected: " + expected + "\n" +
                "  received: " + cookies,
                cookies.containsAll(expected));
    }

    private void setCookie(BindingProvider port, String cookieHeader) {
        Map<String, List<String>> reqHeaders = new HashMap<String, List<String>>();
        reqHeaders.put("Cookie", Collections.singletonList(cookieHeader));
        port.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, reqHeaders);
    }
}
