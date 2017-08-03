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

package benchmark.doclit.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;
import testutil.benchmark.Benchmark;
import testutil.benchmark.Const;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;

/**
 * Base-class for the doc/lit benchmark.
 *
 * @author JAX-RPC RI Development Team
 */
public abstract class DoclitTest extends TestCase implements Const, Benchmark {

    public DoclitTest(String name) {
        super(name);
        try {
            complexType = of.createComplexType();
            complexType.setVarString(string);
            complexType.setVarInt(intNumber);
            complexType.setVarFloat(floatNumber);

            for (int i=0; i< 20; i++) {
                complexTypeArray[i] = of.createComplexType();
                complexTypeArray[i].setVarString(string);
                complexTypeArray[i].setVarInt(intNumber);
                complexTypeArray[i].setVarFloat(floatNumber);
            }

            nestedComplexType = of.createNestedComplexType();
            nestedComplexType.setVarString(string);
            nestedComplexType.setVarInt(intNumber);
            nestedComplexType.setVarFloat(floatNumber);
            nestedComplexType.setVarComplexType(complexType);

            DatatypeFactory dtf = DatatypeFactory.newInstance();
            Calendar cal = Calendar.getInstance();
            gregorianDate = dtf.newXMLGregorianCalendarDate(2005, 1, 12, DatatypeConstants.FIELD_UNDEFINED);

            // move the stub creation cost out of the benchmark
            createStub();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    protected final EchoPortType getStub() throws Exception {
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
    protected ComplexType[] complexTypeArray = new ComplexType[20];
    protected NestedComplexType nestedComplexType = null;
    protected Enum enumBitFive = Enum.BIT_FIVE;
    protected XMLGregorianCalendar gregorianDate = null;

}
