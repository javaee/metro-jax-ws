/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package bugs.jaxws1049.client;

import com.sun.xml.ws.client.RequestContext;
import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY;
import static javax.xml.ws.BindingProvider.SOAPACTION_USE_PROPERTY;

/**
 * Test for JAX_WS-1049: problem with filling packet from {@link RequestContext} - setting different properties to
 * {@link RequestContext} and checking on server side that those were applied correctly (specifically SOAPAction +
 * custom HTTP headers)
 *
 * @author Miroslav Kos (miroslav.kos at oracle.com)
 */
public class ClientTest extends TestCase {

    public void test() throws SOAPException, IOException {

        // -- TEST different SOAPACTION properties in RequestContext
        EchoImpl port = new EchoImplService().getEchoImplPort();
        Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();

        //THIS next line is causing the SOAPACTION http header to go blank, if fix JAX_WS-1049 not applied ...
        requestContext.keySet();

        port.doSomething();

        requestContext.put(SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        requestContext.put(SOAPACTION_URI_PROPERTY, "customSOAPAction");
        port.doSomething();

        requestContext.put(SOAPACTION_USE_PROPERTY, false);
        port.doSomething();

        requestContext.put(SOAPACTION_USE_PROPERTY, null);
        requestContext.put(SOAPACTION_URI_PROPERTY, "");
        port.doSomething();

        // -- TEST dispatch: http://java.net/jira/browse/JAX_WS-1014 : Bug <12883765>
        String jaxwsMsg = "<?xml version='1.0' encoding='UTF-8'?>" +
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<S:Body><doSomething xmlns=\"http://server.jaxws1049.bugs/\"/></S:Body></S:Envelope>";

        EchoImplService service = new EchoImplService();
        Dispatch<SOAPMessage> dispatch = service.createDispatch(new QName("http://server.jaxws1049.bugs/", "EchoImplPort"), SOAPMessage.class, Service.Mode.MESSAGE);

        Map<String, List> headersMap = new HashMap<String, List>();
        headersMap.put("X-ExampleHeader2", Collections.singletonList("Value"));
        dispatch.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headersMap);

        MimeHeaders mhs = new MimeHeaders();

        mhs.addHeader("My-Content-Type", "text/xml");

        SOAPMessage msg = MessageFactory.newInstance().createMessage(mhs, new ByteArrayInputStream(jaxwsMsg.getBytes()));
        dispatch.invoke(msg);
    }

}
