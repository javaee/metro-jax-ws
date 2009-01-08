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
package server.endpoint_6790700.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.xml.ws.Endpoint;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.io.*;
import javax.xml.ws.Provider;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceProvider;
import java.io.StringReader;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;
import javax.xml.ws.WebServiceException;

import com.sun.net.httpserver.HttpExchange;
import testutil.*;

/**
 * @author Jitendra Kotamraju
 */
public class NamespaceTest extends TestCase {

    public void testNamespace() throws Exception {
        int port = Util.getFreePort();
        String address = "http://127.0.0.1:"+port+"/hello";
        Endpoint e = Endpoint.create(new MyProvider());
        e.publish(address);

        String message = 
"<s:Envelope xmlns:s='http://schemas.xmlsoap.org/soap/envelope/'>"+
"  <s:Body xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema'>"+
"<ping xmlns='urn:com:dassault_systemes:webservice'><iSentence xsi:nil='true'/></ping>"+
"</s:Body>"+
"</s:Envelope>";
        HTTPResponseInfo rInfo = ClientServerTestUtil.sendPOSTRequest(address,message);
        assertEquals(200, rInfo.getResponseCode());

        e.stop();
    }

}
