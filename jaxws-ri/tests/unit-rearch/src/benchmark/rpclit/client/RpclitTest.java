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

package benchmark.rpclit.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;
import testutil.benchmark.Benchmark;
import testutil.benchmark.Const;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.List;

/**
 * Base-class for the rpc/lit benchmark.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class RpclitTest extends TestCase implements Const, Benchmark {
    public RpclitTest(String name) throws Exception {
        super(name);

        complexType = of.createComplexType();
        complexType.setVarString(string);
        complexType.setVarInt(intNumber);
        complexType.setVarFloat(floatNumber);

        complexTypeArray = of.createComplexTypeArray();
        List<ComplexType> complexTypeList = complexTypeArray.getValue();
        for (int i=0; i<20; i++) {
            ComplexType complexType = of.createComplexType();
            complexType.setVarString(string);
            complexType.setVarInt(intNumber);
            complexType.setVarFloat(floatNumber);
            complexTypeList.add(i, complexType);
        }

        nestedComplexType = of.createNestedComplexType();
        nestedComplexType.setVarString(string);
        nestedComplexType.setVarInt(intNumber);
        nestedComplexType.setVarFloat(floatNumber);
        nestedComplexType.setVarComplexType(complexType);

        rpclitStringArray = of.createStringArray();
        List<String> stringList = rpclitStringArray.getValue();
        for (int i=0; i<stringArray.length; i++)
            stringList.add(i, stringArray[i]);

        rpclitIntArray = of.createIntegerArray();
        List<Integer> intList = rpclitIntArray.getValue();
        for (int i=0; i<intArray.length; i++)
            intList.add(i, new Integer(intArray[i]));

        rpclitFloatArray = of.createFloatArray();
        List<Float> floatList = rpclitFloatArray.getValue();
        for (int i=0; i<floatArray.length; i++)
            floatList.add(i, new Float(floatArray[i]));

        DatatypeFactory dtf = DatatypeFactory.newInstance();
        Calendar cal = Calendar.getInstance();
        gregorianDate = dtf.newXMLGregorianCalendarDate(2005, 1, 12, DatatypeConstants.FIELD_UNDEFINED);

        // move the stub creation cost out of the benchmark
        createStub();
    }

    public EchoPortType getStub() {
        return stub;
    }

    private void createStub() throws Exception {
        if (stub == null) {
            EchoService service = new EchoService();
            stub = service.getEchoPort();
            ClientServerTestUtil.setTransport(stub);
        }
    }

    static EchoPortType stub = null;
    ObjectFactory of = new ObjectFactory();
    protected ComplexType complexType = null;
    protected ComplexTypeArray complexTypeArray = null;
    protected NestedComplexType nestedComplexType = null;
    protected Enum enumBitFive = Enum.BIT_FIVE;
    protected XMLGregorianCalendar gregorianDate;

    protected StringArray rpclitStringArray = null;
    protected IntegerArray rpclitIntArray = null;
    protected FloatArray rpclitFloatArray = null;
}
