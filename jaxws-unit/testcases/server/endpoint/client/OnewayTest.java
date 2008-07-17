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

import java.io.StringReader;
import java.net.URL;
 
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.Oneway;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.BindingProvider;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import junit.framework.TestCase;

/**
 * @author Jitendra Kotamraju
 */

public class OnewayTest extends TestCase
{
    private static final String NS = "http://echo.org/";

    public void testOneway() throws Exception {
        int port = Util.getFreePort();
        String address = "http://localhost:"+port+"/oneway";
        Endpoint endpoint = Endpoint.create(new OnewayEndpoint());
        endpoint.publish(address);

        int status = getHttpStatus(address);
        assertEquals(202, status);

        assertTrue(verify(address).contains("12345"));
        
        endpoint.stop();
    }

    private int getHttpStatus(String address) throws Exception {
        QName portName = new QName(NS, "OnewayEndpointPort");
        QName serviceName = new QName(NS, "OnewayEndpointService");
        Service service = Service.create(new URL(address+"?wsdl"), serviceName);
        Dispatch<Source> d = service.createDispatch(portName, Source.class,
            Service.Mode.PAYLOAD);
        String body = "<ns0:echoInteger xmlns:ns0='"+NS+
            "'><arg0>12345</arg0></ns0:echoInteger>";
        d.invokeOneWay(new StreamSource(new StringReader(body)));
        Map<String, Object> rc = ((BindingProvider)d).getResponseContext();
        return (Integer)rc.get(MessageContext.HTTP_RESPONSE_CODE);
    }

    private String verify(String address) throws Exception {
        QName portName = new QName(NS, "OnewayEndpointPort");
        QName serviceName = new QName(NS, "OnewayEndpointService");
        Service service = Service.create(new URL(address+"?wsdl"), serviceName);
        Dispatch<Source> d = service.createDispatch(portName, Source.class,
            Service.Mode.PAYLOAD);
        String body = "<ns0:verifyInteger xmlns:ns0='"+NS+"'/>";
        Source response = d.invoke(new StreamSource(new StringReader(body)));

        ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(response, new StreamResult(bos));
        bos.close();
        return new String(bos.toByteArray());
    }
   
    @WebService(targetNamespace="http://echo.org/")
    @SOAPBinding(style=Style.RPC)
    public static class OnewayEndpoint {
        volatile int prev;

        @Oneway
        public void echoInteger(int arg0) {
            prev = arg0;
        }

        public int verifyInteger() {
            return prev;
        }
    }
}
