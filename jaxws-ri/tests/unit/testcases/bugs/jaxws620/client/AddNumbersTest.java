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
package bugs.jaxws620.client;

import bugs.jaxws620.client.soap11.Envelope;
import junit.framework.TestCase;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;


public class AddNumbersTest extends TestCase {
    
    private static final Logger LOGGER = Logger.getLogger(AddNumbersTest.class.getName());

    private static final QName portQName = new QName("http://duke.example.org", "AddNumbersPort");

    //SOAP MESSAGEs
    private static String messageRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><addNumbers xmlns=\"http://duke.example.org\"><arg0>10</arg0><arg1>20</arg1></addNumbers></soapenv:Body></soapenv:Envelope>";
    private static String messageResponse = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><addNumbersResponse xmlns=\"http://duke.example.org\"><return>30</return></addNumbersResponse></S:Body></S:Envelope>";

    // MESSAGE PAYLOADs
    private static String payloadRequest = "<addNumbers xmlns=\"http://duke.example.org\"><arg0>10</arg0><arg1>20</arg1></addNumbers>";
    private static String payloadResponse = "<ns0:addNumbersResponse xmlns:ns0=\"http://duke.example.org\"><return>30</return></ns0:addNumbersResponse>";

    public void testNonDispatch() {
        AddNumbers port = new AddNumbersImplService().getAddNumbersPort();
        int res = port.addNumbers(1, 2);
        LOGGER.finest("1+2 = " + res);
        assertTrue(res == 3);
    }

    @SuppressWarnings("unchecked")
    public void testModeMESSAGE() throws JAXBException {
        doDispatchTesting("bugs.jaxws620.client.soap11", messageRequest, Service.Mode.MESSAGE, Envelope.class);
    }

    @SuppressWarnings("unchecked")
    public void testModePAYLOAD() throws JAXBException {
        doDispatchTesting("bugs.jaxws620.client", payloadRequest, Service.Mode.PAYLOAD, AddNumbersResponse.class);
    }

    private void doDispatchTesting(String contextPath, String request, Service.Mode mode, Class expectedClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
        Unmarshaller u = jaxbContext.createUnmarshaller();
        Object obj = u.unmarshal(new StreamSource(new StringReader(request)));

        LOGGER.finest("---Marshaller parsed and wrote back----\n" + reconstructMsg(jaxbContext, obj) + "\n---------\n");

        Service service = new AddNumbersImplService();

        Dispatch dispatch = service.createDispatch(portQName, jaxbContext, mode);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "run:tns:AddNumbersService");

        JAXBElement response = (JAXBElement) dispatch.invoke(obj);
        assertTrue(expectedClass.equals(response.getDeclaredType()));

        LOGGER.finest("---result:" + response.getValue().toString() + "---\n"
                + reconstructMsg(jaxbContext, response) + "\n---------\n");
    }

    private String reconstructMsg(JAXBContext jaxbContext, Object obj) throws JAXBException {
        Marshaller m = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();
        m.marshal(obj, writer);
        return writer.toString();
    }

}


