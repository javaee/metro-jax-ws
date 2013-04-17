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

package fromwsdl.wsdl_rpclit_get_catalog.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TestApp extends TestCase{

    private static RetailerPortType stub;
    public TestApp(String name) throws Exception{
        super(name);
        RetailerService service = new RetailerService();
        stub = service.getRetailerPort();
        ClientServerTestUtil.setTransport(stub);
    }

    public void testGetCatalog() throws Exception {
        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem[] cil = {new fromwsdl.wsdl_rpclit_get_catalog.client.ObjectFactory().createCatalogItem(), new fromwsdl.wsdl_rpclit_get_catalog.client.ObjectFactory().createCatalogItem()};
        cil[0].setName("JAXRPC SI 1.1.2");
        cil[0].setBrand("Sun Microsystems, Inc.");
        cil[0].setCategory("Web Services");
        cil[0].setProductNumber(1234);
        cil[0].setDescription("Coolest Webservice Product");
        cil[0].setPrice(new BigDecimal("100"));

        cil[1].setName("JAXRPC SI 2.0");
        cil[1].setBrand("Sun Microsystems, Inc.");
        cil[1].setCategory("Web Services");
        cil[1].setProductNumber(5678);
        cil[1].setDescription("Coolest Webservice Product");
        cil[1].setPrice(new BigDecimal("200"));

        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogType ret = stub.getCatalog();
        for(int i = 0; i < ret.getItem().size(); i++){
            fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem gci = (fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem)ret.getItem().get(i);
            assertTrue(gci.getName().equals(cil[i].getName()));
            assertTrue(gci.getBrand().equals(cil[i].getBrand()));
            assertTrue(gci.getCategory().equals(cil[i].getCategory()));
            assertTrue(gci.getProductNumber() == cil[i].getProductNumber());
            assertTrue(gci.getDescription().equals(cil[i].getDescription()));
            assertTrue(gci.getPrice().equals(cil[i].getPrice()));

        }
    }

    public void testEchoCatalog() throws Exception {
        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogType in = new fromwsdl.wsdl_rpclit_get_catalog.client.ObjectFactory().createCatalogType();
        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem[] cil = {new fromwsdl.wsdl_rpclit_get_catalog.client.ObjectFactory().createCatalogItem(), new fromwsdl.wsdl_rpclit_get_catalog.client.ObjectFactory().createCatalogItem()};
        cil[0].setName("JAXRPC SI 1.1.2");
        cil[0].setBrand("Sun Microsystems, Inc.");
        cil[0].setCategory("Web Services");
        cil[0].setProductNumber(1234);
        cil[0].setDescription("Coolest Webservice Product");
        cil[0].setPrice(new BigDecimal("100"));
        cil[1].setName("JAXRPC SI 2.0");
        cil[1].setBrand("Sun Microsystems, Inc.");
        cil[1].setCategory("Web Services");
        cil[1].setProductNumber(5678);
        cil[1].setDescription("Coolest Webservice Product");
        cil[1].setPrice(new BigDecimal("200"));
        in.getItem().add(cil[0]);
        in.getItem().add(cil[1]);
        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogType ret = stub.echoCatalog(in);
        for(int i = 0; i < ret.getItem().size(); i++){
            fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem gci = (fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem)ret.getItem().get(i);

//                System.out.println("\nName: "+gci.getName());
//                System.out.println("Brand: "+gci.getBrand());
//                System.out.println("Category: "+gci.getCategory());
//                System.out.println("ProductNumber: "+gci.getProductNumber());
//                System.out.println("Description: "+gci.getDescription());
//                System.out.println("Price: "+gci.getPrice());

            assertTrue(gci.getName().equals(cil[i].getName()));
            assertTrue(gci.getBrand().equals(cil[i].getBrand()));
            assertTrue(gci.getCategory().equals(cil[i].getCategory()));
            assertTrue(gci.getProductNumber() == cil[i].getProductNumber());
            assertTrue(gci.getDescription().equals(cil[i].getDescription()));
            assertTrue(gci.getPrice().equals(cil[i].getPrice()));

        }
    }




    public void testTestCatalog() throws Exception {
        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogType in = new fromwsdl.wsdl_rpclit_get_catalog.client.ObjectFactory().createCatalogType();
        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem cil = new fromwsdl.wsdl_rpclit_get_catalog.client.ObjectFactory().createCatalogItem();
        cil.setName("JAXRPC SI 1.1.2");
        cil.setBrand("Sun Microsystems, Inc.");
        cil.setCategory("Web Services");
        cil.setProductNumber(1234);
        cil.setDescription("Coolest Webservice Product");
        cil.setPrice(new BigDecimal("100"));

        String expectedName = "Sun Microsystems";

        javax.xml.ws.Holder<java.lang.String> name = new javax.xml.ws.Holder<java.lang.String>();
        name.value = "Sun";

        int index = 1234;

        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogType ret = stub.testCatalog(name, index);
        assertEquals(name.value, expectedName);

        fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem gci = (fromwsdl.wsdl_rpclit_get_catalog.client.CatalogItem)ret.getItem().get(0);
        assertEquals(gci.getName(), cil.getName());
        assertEquals(gci.getBrand(), cil.getBrand());
        assertEquals(gci.getCategory(), cil.getCategory());
        assertTrue(gci.getProductNumber() == cil.getProductNumber());
        assertEquals(gci.getDescription(), cil.getDescription());
        assertEquals(gci.getPrice(), cil.getPrice());
    }

    public void testHelloOneWay() throws Exception{
        String str = "JAXRPC 2.0";
        stub.helloOneWay(str);
    }


    public void testHolders() throws Exception{
        String str = "1";
        javax.xml.ws.Holder<java.lang.Integer> inout = new javax.xml.ws.Holder<java.lang.Integer>();
        inout.value = 1;
        double out = stub.testHolders(str, inout);
        assertEquals((int)inout.value, 2);
        assertEquals(out, 1.0);
    }

    public void testShortArrayTest() throws Exception{
        ShortArrayTest req = new ObjectFactory().createShortArrayTest();
        req.getShortArray().add((short)100);
        req.getShortArray().add((short)200);
        req.getShortArray().add((short)300);
        ShortArrayTestResponse resp = stub.shortArrayTest(req);
        assertTrue((resp.getShortArray().get(0) == 100) && (resp.getShortArray().get(1)== 200) && (resp.getShortArray().get(2) == 300));
    }

    public void testParameterOrder() throws Exception {
        int bar = 1;
        String foo = "Hello World!";
        javax.xml.ws.Holder<java.lang.String> foo1 = new javax.xml.ws.Holder<java.lang.String>();
        int resp = stub.testParameterOrder(bar, foo, foo1);
        assertTrue(bar == resp && foo.equals(foo1.value));
    }
    
    /**
     *
     * @throws Exception
     */
    public void testUnboundedParts() throws Exception{
        String foo = "3";
        String foo1 = "4";
        javax.xml.ws.Holder<java.lang.Integer> bar = new javax.xml.ws.Holder<java.lang.Integer>();
        javax.xml.ws.Holder<java.lang.Integer> bar1 = new javax.xml.ws.Holder<java.lang.Integer>();
        int resp = stub.testUnboundedParts(foo, foo1, bar, bar1);
        assertTrue(bar.value.intValue() == 3);
        assertTrue(foo1.equals("4"));
        assertTrue(bar1.value == null);
        assertTrue(resp == 0);
    }

   public void testGCBug() throws Exception{
        String fn = "foo";
        String ln = "bar";
        String mn = "duke";
        int age = 100;
        javax.xml.ws.Holder<fromwsdl.wsdl_rpclit_get_catalog.client.NameType> nameHolder = new javax.xml.ws.Holder<fromwsdl.wsdl_rpclit_get_catalog.client.NameType>();
        javax.xml.ws.Holder<fromwsdl.wsdl_rpclit_get_catalog.client.AddressType> addressHolder = new javax.xml.ws.Holder<fromwsdl.wsdl_rpclit_get_catalog.client.AddressType>();
        javax.xml.ws.Holder<fromwsdl.wsdl_rpclit_get_catalog.client.PersonalDetailsType> personalHolder = new javax.xml.ws.Holder<fromwsdl.wsdl_rpclit_get_catalog.client.PersonalDetailsType>();
        stub.getGCBug(fn, ln, mn, age, nameHolder, personalHolder,addressHolder);
        assertTrue(nameHolder.value.getFn().equals(fn));
        assertTrue(nameHolder.value.getLn().equals(ln));
    }
    
}
