/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.jaxws195.client;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;

import com.sun.xml.ws.api.SOAPVersion;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Client extends TestCase {

    public Client(String name) {
        super(name);
    }

    private static final String jaxwsMsg="<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><ns2:echoDate xmlns:ns2=\"http://server.jaxws195.fromjava/\"><arg0>0006-05-01T10:00:00.000Z</arg0></ns2:echoDate></S:Body></S:Envelope>";
    private static final String axisMsg="<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><ns2:echoDate xmlns:ns2=\"http://server.jaxws195.fromjava/\"><arg0 xsi:type=\"xs:dateTime\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">0006-05-01T10:00:00.000Z</arg0></ns2:echoDate></S:Body></S:Envelope>";
    public void testUsingJaxwsMsg() throws IOException, SOAPException {
        TestServiceService service = new TestServiceService();
        Dispatch<SOAPMessage> disp = service.createDispatch(new QName("http://server.jaxws195.fromjava/", "TestServicePort"), SOAPMessage.class, Service.Mode.MESSAGE);
        MimeHeaders mhs = new MimeHeaders();
        mhs.addHeader("Content-Type", "text/xml");
        SOAPMessage msg = SOAPVersion.SOAP_11.saajMessageFactory.createMessage(mhs, new ByteArrayInputStream(jaxwsMsg.getBytes()));
        disp.invoke(msg);
    }

    public void testUsingAxisMsg() throws IOException, SOAPException {
        TestServiceService service = new TestServiceService();
        Dispatch<SOAPMessage> disp = service.createDispatch(new QName("http://server.jaxws195.fromjava/", "TestServicePort"), SOAPMessage.class, Service.Mode.MESSAGE);
        MimeHeaders mhs = new MimeHeaders();
        mhs.addHeader("Content-Type", "text/xml");
        SOAPMessage msg = SOAPVersion.SOAP_11.saajMessageFactory.createMessage(mhs, new ByteArrayInputStream(axisMsg.getBytes()));
        disp.invoke(msg);
    }




}
