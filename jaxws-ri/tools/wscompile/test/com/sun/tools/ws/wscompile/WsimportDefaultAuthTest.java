/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.tools.ws.wscompile;

import com.sun.istack.NotNull;
import com.sun.tools.ws.processor.modeler.wsdl.ConsoleErrorReporter;
import junit.framework.TestCase;
import java.io.File;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Rama Pulavarthi
 */
public class WsimportDefaultAuthTest extends TestCase {

    private static class MyAuthenticator extends DefaultAuthenticator {

        public MyAuthenticator(@NotNull ErrorReceiver receiver, @NotNull File authfile) throws BadCommandLineException {
            super(receiver, authfile);
        }

        protected URL getRequestingURL() {
            try {
                return new URL("http://foo.com/myservice?wsdl");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void testDefaultAuth() throws Exception{
        URL url = getResourceAsUrl("com/sun/tools/ws/wscompile/.auth");
        DefaultAuthenticator da = new MyAuthenticator(new ConsoleErrorReporter(System.out), new File(url.toURI()));
        PasswordAuthentication pa = da.getPasswordAuthentication();
        assertTrue(pa != null && pa.getUserName().equals("duke") && Arrays.equals(pa.getPassword(), "test".toCharArray()));

    }

    public void testGetDefaultAuth() throws Exception {
        URL url = getResourceAsUrl("com/sun/tools/ws/wscompile/.auth");
        DefaultAuthenticator da = new MyAuthenticator(new ConsoleErrorReporter(System.out), new File(url.toURI()));
//        assertNull(DefaultAuthenticator.getCurrentAuthenticator());
        Authenticator.setDefault(da);
        Authenticator auth = DefaultAuthenticator.getCurrentAuthenticator();
        assertNotNull(auth);
        assertEquals(da, auth);
        Authenticator.setDefault(null);
        assertNull(DefaultAuthenticator.getCurrentAuthenticator());
    }
    
    private static URL getResourceAsUrl(String resourceName) throws RuntimeException {
        URL input = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (input == null) {
            throw new RuntimeException("Failed to find resource \"" + resourceName + "\"");
        }
        return input;
    }

}
