/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package whitebox.serializable.client;

import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.util.ByteArrayBuffer;
import junit.framework.TestCase;

import javax.xml.ws.WebServiceException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

/**
 * Tests serialzability of RI exceptions extending JAXWSExceptionBase
 * @author Rama Pulavarthi
 */
public class SerializableWebServiceExceptionTest extends TestCase {

    /**
     *  Tests Localizable with no args
     * @throws Exception
     */
    public void testClientTransportExSerial1() throws Exception {
        ClientTransportException cte_orig = new ClientTransportException(ClientMessages.localizableEPR_WITHOUT_ADDRESSING_ON());
        cte_orig.fillInStackTrace();

        ByteArrayBuffer buffer = new ByteArrayBuffer();
        ObjectOutputStream oos = new ObjectOutputStream(buffer);
        oos.writeObject(cte_orig);
        ObjectInputStream ois = new ObjectInputStream(buffer.newInputStream());
        ClientTransportException cte_ser = (ClientTransportException) ois.readObject();

        assertEquals(cte_orig.getLocalizedMessage(),cte_ser.getLocalizedMessage());
        assertEquals(cte_orig.getResourceBundleName(), cte_ser.getResourceBundleName());
        assertEquals(cte_orig.getKey(), cte_ser.getKey());
        assert(cte_orig.getArguments().length == cte_ser.getArguments().length);
    }

    /**
     * Tests Nested Exception
     * @throws Exception
     */
    public void testClientTransportExSerial2() throws Exception {
        ClientTransportException cte_orig = new ClientTransportException(ClientMessages.localizableEPR_WITHOUT_ADDRESSING_ON());
        cte_orig.fillInStackTrace();

        ByteArrayBuffer buffer = new ByteArrayBuffer();
        ObjectOutputStream oos = new ObjectOutputStream(buffer);
        oos.writeObject(new WebServiceException(cte_orig));
        ObjectInputStream ois = new ObjectInputStream(buffer.newInputStream());
        WebServiceException wse_ser = (WebServiceException) ois.readObject();
        ClientTransportException cte_ser = (ClientTransportException) wse_ser.getCause();

        assertEquals(cte_orig.getLocalizedMessage(),cte_ser.getLocalizedMessage());
        assertEquals(cte_orig.getResourceBundleName(), cte_ser.getResourceBundleName());
        assertEquals(cte_orig.getKey(), cte_ser.getKey());
        assert(cte_orig.getArguments().length == cte_ser.getArguments().length);

    }

    /**
     * Tests argument with null
     * @throws Exception
     */
    public void testClientTransportExSerial3() throws Exception {
        WebServiceException wse_orig = new ClientTransportException(ClientMessages.localizableFAILED_TO_PARSE(new URL("http://example.com?wsdl"), null));
        ClientTransportException cte_orig = (ClientTransportException)wse_orig;
        ByteArrayBuffer buffer = new ByteArrayBuffer();
        ObjectOutputStream oos = new ObjectOutputStream(buffer);
        oos.writeObject(wse_orig);

        ObjectInputStream ois = new ObjectInputStream(buffer.newInputStream());
        WebServiceException wse_ser = (WebServiceException) ois.readObject();
        ObjectInputStream ois1 = new ObjectInputStream(buffer.newInputStream());
        ClientTransportException cte_ser = (ClientTransportException) ois1.readObject();

        assertEquals(cte_orig.getLocalizedMessage(),cte_ser.getLocalizedMessage());
        assertEquals(cte_orig.getResourceBundleName(), cte_ser.getResourceBundleName());
        assertEquals(cte_orig.getKey(), cte_ser.getKey());
        assert(cte_orig.getArguments().length == cte_ser.getArguments().length);
        assertEquals(cte_orig.getArguments()[0], cte_ser.getArguments()[0]);
        assertEquals(cte_orig.getArguments()[1], cte_ser.getArguments()[1]);
    }

    /**
     * Tests with two serializable arguments
     * @throws Exception
     */
    public void testClientTransportExSerial4() throws Exception {
        WebServiceException wse_orig = new ClientTransportException(ClientMessages.localizableFAILED_TO_PARSE(new URL("http://example.com?wsdl"), new java.io.IOException("Can't access url")));
        ClientTransportException cte_orig = (ClientTransportException)wse_orig;
        ByteArrayBuffer buffer = new ByteArrayBuffer();
        ObjectOutputStream oos = new ObjectOutputStream(buffer);
        oos.writeObject(cte_orig);

        ObjectInputStream ois = new ObjectInputStream(buffer.newInputStream());
        WebServiceException wse_ser = (WebServiceException) ois.readObject();
        ObjectInputStream ois1 = new ObjectInputStream(buffer.newInputStream());
        ClientTransportException cte_ser = (ClientTransportException) ois1.readObject();

        assertEquals(cte_orig.getLocalizedMessage(),cte_ser.getLocalizedMessage());
        assertEquals(cte_orig.getResourceBundleName(), cte_ser.getResourceBundleName());
        assertEquals(cte_orig.getKey(), cte_ser.getKey());
        assert(cte_orig.getArguments().length == cte_ser.getArguments().length);
        assertEquals(cte_orig.getArguments()[0], cte_ser.getArguments()[0]);
        assertEquals(cte_orig.getArguments()[1].getClass(), cte_ser.getArguments()[1].getClass());
        assertEquals(cte_orig.getArguments()[1].toString(), cte_ser.getArguments()[1].toString());
    }

    /**
     * Tests non-serializable argument, should serialize as String
     * @throws Exception
     */
    public void testClientTransportExSerial5() throws Exception {
        ClientTransportException cte_orig = new ClientTransportException(ClientMessages.localizableFAILED_TO_PARSE(new URL("http://example.com?wsdl"), Thread.currentThread()));
        ByteArrayBuffer buffer = new ByteArrayBuffer();
        ObjectOutputStream oos = new ObjectOutputStream(buffer);
        oos.writeObject(cte_orig);

        ObjectInputStream ois = new ObjectInputStream(buffer.newInputStream());
        WebServiceException wse_ser = (WebServiceException) ois.readObject();
        ObjectInputStream ois1 = new ObjectInputStream(buffer.newInputStream());
        ClientTransportException cte_ser = (ClientTransportException) ois1.readObject();

        assertEquals(cte_orig.getLocalizedMessage(),cte_ser.getLocalizedMessage());
        assertEquals(cte_orig.getResourceBundleName(), cte_ser.getResourceBundleName());
        assertEquals(cte_orig.getKey(), cte_ser.getKey());
        assert(cte_orig.getArguments().length == cte_ser.getArguments().length);
        assertEquals(cte_orig.getArguments()[0], cte_ser.getArguments()[0]);
        assert(cte_ser.getArguments()[1] instanceof String);
        assertEquals(cte_orig.getArguments()[1].toString(), cte_ser.getArguments()[1].toString());
    }

}
