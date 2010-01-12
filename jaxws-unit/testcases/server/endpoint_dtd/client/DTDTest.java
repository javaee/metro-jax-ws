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
package server.endpoint_dtd.client;

import java.io.StringReader;
import java.net.URL;
 
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.Oneway;
import javax.xml.namespace.QName;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import testutil.ClientServerTestUtil;
import testutil.HTTPResponseInfo;

import junit.framework.TestCase;

/**
 * Tests for DTD in SOAP requests
 *
 * @author Jitendra Kotamraju
 */
public class DTDTest extends TestCase
{
    private static final String NS = "http://echo.org/";

/*
    public void testXXE() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:"+port+"/xxe";
        Endpoint endpoint = Endpoint.create(new MyEndpoint());
        endpoint.publish(address);
        try {
            HTTPResponseInfo rInfo = sendXXE(address);
            String resp = rInfo.getResponseBody();
            if (resp.contains("root")) {
                fail("XXE attack with external entity is working");
            }
            int code = rInfo.getResponseCode();
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, code);
        } finally {
            endpoint.stop();
        }
    }

    private HTTPResponseInfo sendXXE(String address) throws Exception {

        String message = "<?xml version='1.0' ?><!DOCTYPE S:Envelope[<!ENTITY passwd SYSTEM '/etc/passwd'>]><S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body><ns0:echoString xmlns:ns0='http://echo.org/'><arg0>&passwd;</arg0></ns0:echoString></S:Body></S:Envelope>";

        return ClientServerTestUtil.sendPOSTRequest(address, message, "text/xml");
    }
*/

    public void testEntity() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:"+port+"/entity";
        Endpoint endpoint = Endpoint.create(new MyEndpoint());
        endpoint.publish(address);
        try {
            HTTPResponseInfo rInfo = sendEntity(address);
            String resp = rInfo.getResponseBody();
            if (resp.contains("x1y1")) {
                fail("Entity is getting resolved");
            }
            int code = rInfo.getResponseCode();
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, code);
        } finally {
            endpoint.stop();
        }
    }

    private HTTPResponseInfo sendEntity(String address) throws Exception {
        String message = "<?xml version='1.0' ?><!DOCTYPE S:Envelope[<!ENTITY xy  'x1y1'><!ENTITY xy2 '&xy; &xy;'>]><S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body><ns0:echoString xmlns:ns0='http://echo.org/'><arg0>&xy2;</arg0></ns0:echoString></S:Body></S:Envelope>";

        return ClientServerTestUtil.sendPOSTRequest(address, message, "text/xml");
    }

    @WebService(targetNamespace="http://echo.org/")
    @SOAPBinding(style=Style.RPC)
    public static class MyEndpoint {
        public String echoString(String arg) {
            return arg;
        }
    }
}
