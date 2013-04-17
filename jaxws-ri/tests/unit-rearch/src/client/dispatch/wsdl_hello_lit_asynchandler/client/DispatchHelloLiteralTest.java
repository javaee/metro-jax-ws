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

package client.dispatch.wsdl_hello_lit_asynchandler.client;

import junit.framework.TestCase;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import testutil.ClientServerTestUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.Future;

import client.common.client.DispatchTestCase;

/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchHelloLiteralTest extends DispatchTestCase {

    private String helloMsg = "<Hello xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></Hello>";

    private String helloResponse = "<HelloOutput xmlns=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></HelloOutput>";
    private String voidMsg = "<VoidTest xmlns=\"urn:test:types\"/>";
    private String voidResponse = "<VoidTestResponse xmlns=\"urn:test:types\"/>";

    private QName serviceQName = new QName("urn:test", "Hello");
    private QName portQName = new QName("urn:test", "HelloPort");
    ;
    private String bindingIdString = SOAPBinding.SOAP11HTTP_BINDING;
    private String endpointAddress;
  
    private Service service;
    private Dispatch dispatch;

    //assertions for these tests are in handlers
    public DispatchHelloLiteralTest(String name) {
        super(name);

        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:/jaxrpc-client_dispatch_wsdl_hello_lit_asynchandler/hello";
    }

    private static javax.xml.bind.JAXBContext createJAXBContext() {
        try {
            return javax.xml.bind.JAXBContext.newInstance(client.dispatch.wsdl_hello_lit_asynchandler.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }


    private void createService() {

        service = Service.create(serviceQName);
        //does service.addPort(portQName, bindingIdString, endpointAddress
        addPort(service, portQName, bindingIdString, endpointAddress);
    }


    private Dispatch createDispatchJAXB() {
        JAXBContext context = createJAXBContext();
        try {

            dispatch = service.createDispatch(portQName, context, Service.Mode.PAYLOAD);
        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch createDispatchSource() {
        try {

            dispatch = service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);
        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch getDispatchJAXB() {
        createService();
        return createDispatchJAXB();
    }

    private Dispatch getDispatchSource() {
        createService();
        return createDispatchSource();
    }

    public void testHelloAsyncHandlerJAXB() {

        JAXBContext jc = null;
        Hello_Type hello = null;
        try {
            jc = createJAXBContext();
            hello = new Hello_Type();
        } catch (Exception jbe) {
            jbe.printStackTrace();
        }
        Future result = null;

        try {
            for (int i = 0; i < 500; i++) {
                hello.setExtra("Test ");
                hello.setArgument("Dispatch ");

                Dispatch dispatch = getDispatchJAXB();

                JAXBAsyncHandler handler = new JAXBAsyncHandler(hello);
                result = dispatch.invokeAsync(hello, handler);
                if (i == 100)
                    System.out.println("Count is 100");
                if (i == 300)
                    System.out.println("Count is 300");
                if (i == 499)
                    System.out.println("Count is 499");
            }
        } catch (WebServiceException e) {
            e.printStackTrace();
        }

    }


    public void testHelloAsyncHandlerJAXB2() {

        JAXBContext jc = null;
        Hello_Type hello = null;
        try {
            jc = createJAXBContext();
            hello = new Hello_Type();
        } catch (Exception jbe) {
            jbe.printStackTrace();
        }
        Future result = null;

        hello.setExtra("Test ");
        hello.setArgument("Dispatch ");

        Dispatch dispatch = getDispatchJAXB();

        JAXBAsyncHandler handler = new JAXBAsyncHandler(hello);
        result = dispatch.invokeAsync(hello, handler);

        System.out.println("Response " + result.getClass().getName());
        int i = 0;
        while (!result.isDone()) {
            if (i == 1) System.out.println("Waiting for handler to complete 1");
            if (i == 500) System.out.println("Waiting for handler to complete 500");
            if (i == 1500) System.out.println("Waiting for handler to complete 1500");

            i++;
        }
        System.out.println("Handler is done ");
    }

    /* public void testHelloAsyncHandlerCancelJAXB() {

        JAXBContext jc = null;
        Hello_Type hello = null;

        try {
            jc = createJAXBContext();
            hello = new Hello_Type();

        } catch (Exception jbe) {
            jbe.printStackTrace();
        }

        try {
            hello.setExtra("Test ");
            hello.setArgument("Dispatch ");

            Dispatch dispatch = getDispatchJAXB();

            JAXBAsyncHandler handler = new JAXBAsyncHandler(hello);
            Future result = dispatch.invokeAsync(hello, handler);
            try {
                System.out.println("Response retured to test Thread " + Thread.currentThread().getId());
                System.out.println("Response " + result.getClass().getName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            while (!result.isDone()) {
                System.out.println("Not done");
                result.cancel(false);
                assertTrue(result.isCancelled());
                System.out.println("Canceled result");
            }

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
    }
   */

    public void kkktestHelloAsyncHandlerCancelInterruptJAXB() {

        JAXBContext jc = null;
        Hello_Type hello = null;

        try {
            jc = createJAXBContext();
            hello = new Hello_Type();

        } catch (Exception jbe) {
            jbe.printStackTrace();
        }

        hello.setExtra("Test ");
        hello.setArgument("Dispatch ");

        Dispatch dispatch = getDispatchJAXB();

        JAXBAsyncHandler handler = new JAXBAsyncHandler(hello);
        Future result = dispatch.invokeAsync(hello, handler);
        while (!result.isDone()) {
            result.cancel(true);
            assertTrue(result.isCancelled());
            System.out.println("Cancel interupt passed");
        }
    }


    public void testHelloAsyncHandlerXML() {

        Dispatch dispatch = getDispatchSource();
        Collection<Source> sourceList = makeMsgSource(helloMsg);
        Collection<Source> responseList = makeMsgSource(helloResponse);
        int i = 0;
        for (Iterator iter = sourceList.iterator(); iter.hasNext();) {
            Object sourceObject = iter.next();
            XMLAsyncHandler handler =
                    new XMLAsyncHandler((Source) ((List) responseList).get(i++));
            Future result = dispatch.invokeAsync(sourceObject, handler);
            System.out.println("Result " + result.getClass().getName());

        }
    }


    public void testVoidAsyncHandlerJAXB() {

        JAXBContext jc = null;
        VoidTest voidTest = null;
        VoidTestResponse voidTestResult = null;
        try {
            jc = createJAXBContext();

            voidTest = new VoidTest();
            voidTestResult = new VoidTestResponse();
        } catch (Exception jbe) {
            jbe.printStackTrace();
        }

        Dispatch dispatch = getDispatchJAXB();

        JAXBAsyncHandler handler = new JAXBAsyncHandler(voidTest);
        Future result = dispatch.invokeAsync(voidTest, handler);
        System.out.println("Response " + result.getClass().getName());

    }

    public void testVoidAsyncHandlerXML() {

        Dispatch dispatch = getDispatchSource();
        Collection<Source> sourceList = makeMsgSource(voidMsg);
        Collection<Source> responseList = makeMsgSource(voidResponse);
        int i = 0;
        for (Iterator iter = sourceList.iterator(); iter.hasNext();) {

            Object sourceObject = iter.next();
            XMLAsyncHandler handler =
                    new XMLAsyncHandler((Source) ((List) responseList).get(i++));
            Future result = dispatch.invokeAsync(sourceObject, handler);


        }
    }

}
