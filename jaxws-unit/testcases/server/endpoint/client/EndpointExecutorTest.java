/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package server.endpoint.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import testutil.ClientServerTestUtil;

import javax.xml.ws.Endpoint;
import javax.xml.ws.http.HTTPBinding;
import java.util.concurrent.Executor;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Jitendra Kotamraju
 */
public class EndpointExecutorTest extends TestCase {

    public void testExecutor() throws Exception {
        int port = Util.getFreePort();
        String address = "http://127.0.0.1:"+port+"/exe";
        Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new MyProvider());
        MyExecutor executor = new MyExecutor();
        e.setExecutor(executor);
        e.publish(address);
        // Make GET request for WSDL. But no WSDL is published for this endpoint
        assertEquals(
            getHttpStatusCode(address), HttpURLConnection.HTTP_NOT_FOUND);
        // Check whether MyExecutor is invoked or not
        assertTrue(executor.isExecuted());
        e.stop();
    }

    private static class MyExecutor implements Executor {
        boolean executed;
        public boolean isExecuted() {
            return executed;
        }

        public void execute(Runnable command) {
            executed = true;
            command.run();
        }
    }

    private int getHttpStatusCode(String address) throws Exception {
        URL url = new URL(address+"?wsdl");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.connect();
        return con.getResponseCode();
    }

}

