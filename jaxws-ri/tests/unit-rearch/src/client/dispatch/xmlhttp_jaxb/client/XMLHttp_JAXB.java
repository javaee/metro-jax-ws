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

package client.dispatch.xmlhttp_jaxb.client;

import client.dispatch.xmlhttp_jaxb.client.atom.FeedType;
import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author JAX-RPC RI Development Team
 */
public class XMLHttp_JAXB extends TestCase {

    // see google-custom-search.txt in project root ...
    private static final String KEY = "AIzaSyAuu1mWRLODp-bQPod76AXUf-ih6gZsrRQ";
    private static final String CX = "007386487642334705425:rffb5lj8r98";

    private static final QName GOOGLE_PORT_NAME = new QName("google_port", "http://google.com/");
    private static final QName GOOGLE_SERVICE_NAME = new QName("google", "http://google.com/");

    private static final Logger LOGGER = Logger.getLogger(XMLHttp_JAXB.class.getName());

    public XMLHttp_JAXB(String name) {
        super(name);
    }

    void setTransport(Dispatch dispatch) {
        try {
            // create helper class
            ClientServerTestUtil util = new ClientServerTestUtil();
            // set transport
            OutputStream log = null;
            log = System.out;
            util.setTransport(dispatch, (OutputStream) log);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    SOAPMessage getSOAPMessage(Source msg) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent((Source) msg);
        message.saveChanges();
        return message;
    }

    public void testXMLHttpJAXBPAYLOAD() throws JAXBException {

        if (ClientServerTestUtil.useLocal()) {
            return;
        }

        Service s = Service.create(GOOGLE_SERVICE_NAME);
        s.addPort(GOOGLE_PORT_NAME, HTTPBinding.HTTP_BINDING, getGoogleURL("JAXB"));

        // Get a Dispatch interface for the Yahoo News Search service and  configure it
        JAXBContext jbc = JAXBContext.newInstance("client.dispatch.xmlhttp_jaxb.client.atom");

        Dispatch<Object> d = s.createDispatch(GOOGLE_PORT_NAME, jbc, Service.Mode.PAYLOAD);
        d.getRequestContext().put(MessageContext.HTTP_REQUEST_METHOD, "GET");

        // Execute the search iterate through the results
        JAXBElement<FeedType> o = (JAXBElement<FeedType>) d.invoke(null);
        FeedType feed = o.getValue();
        assertNotNull(feed);
        List<Object> authorOrCategoryOrContributor = feed.getAuthorOrCategoryOrContributor();
        assertNotNull(authorOrCategoryOrContributor);
        assertTrue(authorOrCategoryOrContributor.size() > 0);
        LOGGER.finest("Google Custom Search successfully read as JAXB Object: authorOrCategoryOrContributor.size() = " + authorOrCategoryOrContributor.size());
    }

    public static String getGoogleURL(String query) {
        return "https://www.googleapis.com/customsearch/v1?key=" + KEY + "&cx=" + CX + "&q=" + query + "&alt=atom";
    }

}
