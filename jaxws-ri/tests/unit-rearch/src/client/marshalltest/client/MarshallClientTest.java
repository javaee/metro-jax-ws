/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package client.marshalltest.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import java.io.OutputStream;
import java.math.BigInteger;


/**
 * @author JAX-RPC RI Development Team
 */
public class MarshallClientTest extends TestCase {

    // main method added for debugging
    public static void main(String[] args) {
        try {
            System.setProperty("uselocal", "true");
            MarshallClientTest test = new MarshallClientTest("MarshallClientTest");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MarshallTest marshallStub;
    private static NewSchemaTest newSchemaStub;
    private static CompoundTest compoundStub;
    private static OneWayTest oneWayStub;

    public MarshallClientTest(String name) {
        super(name);
    }

    public Service createService() {
        MarshallTestService service = new MarshallTestService();
        return (Service)service;
    }

    public void setUp() throws Exception {
        //multiple port test- tck
        OutputStream out = null;
        MarshallTestService service = (MarshallTestService) createService();

        //get Proxy stubs
        marshallStub = service.getMarshallTestPort1();
        if (marshallStub != null) {
            System.out.println(marshallStub.getClass().getName());
            ClientServerTestUtil.setTransport(marshallStub, out);
        } else
            fail("service.getMarshallTestPort1() failed");

        newSchemaStub = service.getMarshallTestPort2();
        if (newSchemaStub != null) {
            ClientServerTestUtil.setTransport(newSchemaStub, out);
            System.out.println(newSchemaStub.getClass().getName());
        } else
            fail("service.getMarshallTestPort2() failed");

        compoundStub = service.getMarshallTestPort3();
        if (compoundStub != null) {
            ClientServerTestUtil.setTransport(compoundStub, out);
            System.out.println(compoundStub.getClass().getName());
        } else
            fail("service.getMarshallTestPort3() failed");

        oneWayStub = service.getMarshallTestPort4();
        if (oneWayStub != null) {
            ClientServerTestUtil.setTransport(oneWayStub, out);
            System.out.println(oneWayStub.getClass().getName());
        } else
            fail("service.getMarshallTestPort4() failed");
    }

    public void testvariousSchemaTypesTest() {
        boolean pass = true;
        try {
            init_FooVariousSchemaTypes_Data();


            FooVariousSchemaTypes request = FooVariousSchemaTypes_data;
            System.out.println("Send: " + request.getFooA() + "|" +
                request.getFooB() + "|" + request.getFooC() +
                "|" + request.getFooD() + "|" + request.getFooE() +
                "|" + request.getFooF());
            FooVariousSchemaTypes response = newSchemaStub.echoVariousSchemaTypesTest(request);
            System.out.println("Recv: " + response.getFooA() + "|" +
                response.getFooB() + "|" + response.getFooC() +
                "|" + response.getFooD() + "|" + response.getFooE() +
                "|" + response.getFooF());
            if (response.getFooA() == request.getFooA() &&
                response.getFooB().equals(request.getFooB()) &&
                response.getFooC().equals(request.getFooC()) &&
                response.getFooD().equals(request.getFooD()) &&
                response.getFooE() == request.getFooE() &&
                response.getFooF() == request.getFooF()) {
                System.out.println("Result match");
            } else {
                System.err.println("Result mismatch");
                pass = false;
            }
        } catch (Exception e) {
            System.err.println("Caught exception: " + e.getMessage());
            e.printStackTrace();
            fail("MarshallVariousSchemaTypesTest failed");
        }

        if (!pass)
            fail("MarshallVariousSchemaTypesTest failed");
    }

    private FooType FooType_data = null;
    private FooType FooType_array_data[] = null;
    private FooVariousSchemaTypes FooVariousSchemaTypes_data = null;
    private FooVariousSchemaTypes FooVariousSchemaTypes_array_data[] = null;
    private FooVariousSchemaTypesListType FooVariousSchemaTypesListType_data = null;

    private FooAnonymousType FooAnonymousType_data = null;

    private void init_FooVariousSchemaTypes_Data() throws Exception {
        FooVariousSchemaTypes_data = new FooVariousSchemaTypes();
        FooVariousSchemaTypes_data.setFooA(1);
        FooVariousSchemaTypes_data.setFooB(new BigInteger("1000"));
        FooVariousSchemaTypes_data.setFooC("NORMALIZEDSTRING");
        FooVariousSchemaTypes_data.setFooD("NMTOKEN");
        FooVariousSchemaTypes_data.setFooE(1);
        FooVariousSchemaTypes_data.setFooF((short) 1);

        FooVariousSchemaTypes_array_data = new FooVariousSchemaTypes[2];

        FooVariousSchemaTypes_array_data[0] = new FooVariousSchemaTypes();
        FooVariousSchemaTypes_array_data[1] = new FooVariousSchemaTypes();
        FooVariousSchemaTypes_array_data[0].setFooA(256);
        FooVariousSchemaTypes_array_data[1].setFooA(0);
        FooVariousSchemaTypes_array_data[0].setFooB(JAXWS_Data.BigInteger_data[0]);
        FooVariousSchemaTypes_array_data[1].setFooB(JAXWS_Data.BigInteger_data[1]);
        FooVariousSchemaTypes_array_data[0].setFooC("NORMALIZEDSTRING1");
        FooVariousSchemaTypes_array_data[1].setFooC("NORMALIZEDSTRING2");
        FooVariousSchemaTypes_array_data[0].setFooD("NMTOKEN1");
        FooVariousSchemaTypes_array_data[1].setFooD("NMTOKEN2");
        FooVariousSchemaTypes_array_data[0].setFooE(0);
        FooVariousSchemaTypes_array_data[1].setFooE(1);
        FooVariousSchemaTypes_array_data[0].setFooF((short) 0);
        FooVariousSchemaTypes_array_data[1].setFooF((short) 1);

        FooVariousSchemaTypesListType_data = new FooVariousSchemaTypesListType();

        for (int i = 0; i < FooVariousSchemaTypes_array_data.length; i++) {
            FooVariousSchemaTypesListType_data.getFooA().add(FooVariousSchemaTypes_array_data[i]);
        }
    }

    private void init_FooAnonymousType_Data() throws Exception {
        FooAnonymousType.FooAnonymousElement fe1 = new FooAnonymousType.FooAnonymousElement();
        FooAnonymousType.FooAnonymousElement fe2 = new FooAnonymousType.FooAnonymousElement();
        fe1.setFooA("foo");
        fe1.setFooB(1);
        fe1.setFooC(true);
        fe2.setFooA("bar");
        fe2.setFooB(0);
        fe2.setFooC(false);

        FooAnonymousType_data = new FooAnonymousType();
        FooAnonymousType_data.getFooAnonymousElement().add(fe1);
        FooAnonymousType_data.getFooAnonymousElement().add(fe2);
    }

    private boolean compareFooAnonymousTypeData(FooAnonymousType request,
                                                FooAnonymousType response) {
        boolean valid = true;

        Object[] req = request.getFooAnonymousElement().toArray();
        Object[] res = response.getFooAnonymousElement().toArray();
        if (req.length == res.length) {
            System.out.println("Array length match - checking array elements");
            for (int i = 0; i < req.length; i++) {
                FooAnonymousType.FooAnonymousElement exp = (FooAnonymousType.FooAnonymousElement) req[i];
                FooAnonymousType.FooAnonymousElement rec = (FooAnonymousType.FooAnonymousElement) res[i];
                System.out.println("Request: " + exp.getFooA() + "|" +
                    exp.getFooB() + "|" + exp.isFooC());
                System.out.println("Response: " + rec.getFooA() + "|" +
                    rec.getFooB() + "|" + rec.isFooC());
                if (!exp.getFooA().equals(rec.getFooA()) ||
                    exp.getFooB() != rec.getFooB() ||
                    exp.isFooC() != rec.isFooC()) {
                    valid = false;
                    System.err.println("Element results mismatch ...");
                    break;
                } else
                    System.out.println("Element results match ...");
            }
        } else {
            System.err.println("Array length mismatch - expected: " +
                req.length + ", received: " + res.length);
        }
        return valid;
    }

    private void init_FooType_Data() throws Exception {
        FooType_data = new FooType();
        FooType_data.setFooA(true);
        FooType_data.setFooB(Byte.MAX_VALUE);
        FooType_data.setFooC(Short.MAX_VALUE);
        FooType_data.setFooD(Integer.MAX_VALUE);
        FooType_data.setFooE(Long.MAX_VALUE);
        FooType_data.setFooF(Float.MAX_VALUE);
        FooType_data.setFooG(Double.MAX_VALUE);
        FooType_data.setFooH("foostringH");
        FooType_data.setFooI("123-ABC12");
        FooType_data.setFooJ(FooVariousSchemaTypes_data);
        FooType_data.setFooK(new BigInteger("101"));
        FooType_data.setFooM("hello,there");
        FooType_data.setFooN(FooAnonymousType_data);
    }


}
