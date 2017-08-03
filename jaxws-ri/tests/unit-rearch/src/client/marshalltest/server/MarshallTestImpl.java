/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package client.marshalltest.server;


import java.io.ByteArrayInputStream;

import javax.xml.ws.WebServiceException;

import java.util.*;

import java.math.BigInteger;
import java.math.BigDecimal;
import javax.xml.namespace.QName;

// Service Implementation Class - as outlined in JAX-RPC Specification

import javax.jws.WebService;

@WebService(
    serviceName="MarshallTestService",
    endpointInterface="client.marshalltest.server.MarshallTest"
)
public class MarshallTestImpl implements MarshallTest {

    // ====================================================================
    // Java Primitive Types
    // ====================================================================
    public BooleanTestResponse booleanTest(BooleanTest v)  {

	System.out.println("boolean="+v.isBooleanValue());
	BooleanTestResponse r;
	try {
	    r = new BooleanTestResponse();
	    r.setBooleanValue(v.isBooleanValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public ByteTestResponse byteTest(ByteTest v)  {

	System.out.println("byte="+v.getByteValue());
	ByteTestResponse r;
	try {
	    r = new ByteTestResponse();
	    r.setByteValue(v.getByteValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public ShortTestResponse shortTest(ShortTest v)  {

	System.out.println("short="+v.getShortValue());
	ShortTestResponse r;
	try {
	    r = new ShortTestResponse();
	    r.setShortValue(v.getShortValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public IntTestResponse intTest(IntTest v)  {

	System.out.println("int="+v.getIntValue());
	IntTestResponse r;
	try {
	    r = new IntTestResponse();
	    r.setIntValue(v.getIntValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public LongTestResponse longTest(LongTest v)  {

	System.out.println("long="+v.getLongValue());
	LongTestResponse r;
	try {
	    r = new LongTestResponse();
	    r.setLongValue(v.getLongValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public FloatTestResponse floatTest(FloatTest v)  {

	System.out.println("float="+v.getFloatValue());
	FloatTestResponse r;
	try {
	    r = new FloatTestResponse();
	    r.setFloatValue(v.getFloatValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public DoubleTestResponse doubleTest(DoubleTest v)  {

	System.out.println("double="+v.getDoubleValue());
	DoubleTestResponse r;
	try {
	    r = new DoubleTestResponse();
	    r.setDoubleValue(v.getDoubleValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    // ====================================================================
    // Java Primitive Type Arrays (Single Dimensional)
    // ====================================================================
    public BooleanArrayTestResponse booleanArrayTest(BooleanArrayTest v)  {

	BooleanArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getBooleanArray());
	    r = new BooleanArrayTestResponse();
	    r.getBooleanArray().addAll(v.getBooleanArray());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public ByteArrayTestResponse byteArrayTest(ByteArrayTest v)  {

	ByteArrayTestResponse r;
	try {
	    JAXWS_Data.dumpArrayValues(v.getByteArray(), "byte");
	    r = new ByteArrayTestResponse();
	    r.setByteArray(v.getByteArray());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public ShortArrayTestResponse shortArrayTest(ShortArrayTest v)  {

	ShortArrayTestResponse r;
	try {
	    //JAXWS_Data.dumpListValues(v.getShortArray());
	    r = new ShortArrayTestResponse();
	    r.getShortArray().addAll(v.getShortArray());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public IntArrayTestResponse intArrayTest(IntArrayTest v)  {

	IntArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getIntArray());
	    r = new IntArrayTestResponse();
	    r.getIntArray().addAll(v.getIntArray());
        }
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public LongArrayTestResponse longArrayTest(LongArrayTest v)  {

	LongArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getLongArray());
	    r = new LongArrayTestResponse();
	    r.getLongArray().addAll(v.getLongArray());
        }
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public FloatArrayTestResponse floatArrayTest(FloatArrayTest v)  {

	FloatArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getFloatArray());
	    r = new FloatArrayTestResponse();
	    r.getFloatArray().addAll(v.getFloatArray());
        }
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public DoubleArrayTestResponse doubleArrayTest(DoubleArrayTest v)  {

	DoubleArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getDoubleArray());
	    r = new DoubleArrayTestResponse();
	    r.getDoubleArray().addAll(v.getDoubleArray());
        }
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public QNameArrayTestResponse qnameArrayTest(QNameArrayTest v)  {

        QNameArrayTestResponse r;
	try {
            JAXWS_Data.dumpListValues(v.getQnameArray1());
            r = new QNameArrayTestResponse();
	    r.getResult().addAll(v.getQnameArray1());
        }
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
        return r;
    }


    public QNameTestResponse qnameTest(QNameTest v)  {

        System.out.println("QName="+v.getQname1());
        System.out.println("QName="+v.getQname1());
        QNameTestResponse res;
	try {
            res = new QNameTestResponse();
            res.setResult(v.getQname1());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
        return res;
    }

    public Base64BinaryTestResponse base64BinaryTest(Base64BinaryTest v)  {

        Base64BinaryTestResponse res;
	try {
            res = new Base64BinaryTestResponse();
            res.setResult(v.getBase64Binary1());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
        return res;
    }

    public HexBinaryTestResponse hexBinaryTest(HexBinaryTest v)  {

        HexBinaryTestResponse res;
	try {
            res = new HexBinaryTestResponse();
            res.setResult(v.getHexBinary1());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
        return res;
    }

    // ====================================================================
    // Standard Java Classes (Scalar, Single Dimensional Arrays)
    // ====================================================================
    public StringTestResponse stringTest(StringTest v)  {

	StringTestResponse r;
	try {
	    System.out.println("String="+v.getStringValue());
	    r = new StringTestResponse();
	    r.setStringValue(v.getStringValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public StringArrayTestResponse stringArrayTest(StringArrayTest v)  {

	StringArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getStringArray());
	    r = new StringArrayTestResponse();
	    r.getStringArray().addAll(v.getStringArray());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public CalendarTestResponse calendarTest(CalendarTest v)  {

	System.out.println("Calendar="+v.getCalendar());
	CalendarTestResponse r;
	try {
	    r = new CalendarTestResponse();
	    r.setCalendar(v.getCalendar());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public CalendarArrayTestResponse calendarArrayTest(CalendarArrayTest v)  {

	CalendarArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getCalendarArray());
	    r = new CalendarArrayTestResponse();
	    r.getCalendarArray().addAll(v.getCalendarArray());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public BigIntegerTestResponse bigIntegerTest(BigIntegerTest v)  {

	System.out.println("BigInteger="+v.getBigInteger());
	BigIntegerTestResponse r;
	try {
	    r = new BigIntegerTestResponse();
	    r.setBigInteger(v.getBigInteger());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public BigIntegerArrayTestResponse bigIntegerArrayTest(BigIntegerArrayTest v)  {

	BigIntegerArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getBigIntegerArray());
	    r = new BigIntegerArrayTestResponse();
	    r.getBigIntegerArray().addAll(v.getBigIntegerArray());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public BigDecimalTestResponse bigDecimalTest(BigDecimalTest v)  {

	System.out.println("BigDecimal="+v.getBigDecimal());
	BigDecimalTestResponse r;
	try {
	    r = new BigDecimalTestResponse();
	    r.setBigDecimal(v.getBigDecimal());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public BigDecimalArrayTestResponse bigDecimalArrayTest(BigDecimalArrayTest v)  {

	BigDecimalArrayTestResponse r;
	try {
	    JAXWS_Data.dumpListValues(v.getBigDecimalArray());
	    r = new BigDecimalArrayTestResponse();
	    r.getBigDecimalArray().addAll(v.getBigDecimalArray());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    // ====================================================================
    // JavaBeans Class (Scalar, Single Dimensional Arrays)
    // ====================================================================
    public JavaBeanTestResponse javaBeanTest(JavaBeanTest v)  {

	System.out.println("JavaBean="+v.getJavaBean());
	JavaBeanTestResponse r;
	try {
	    r = new JavaBeanTestResponse();
	    r.setJavaBean(v.getJavaBean());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    public JavaBeanArrayTestResponse javaBeanArrayTest(JavaBeanArrayTest v)  {

	JavaBeanArrayTestResponse r;
	try {
	    r = new JavaBeanArrayTestResponse();
	    for(JavaBean e : v.getJavaBeanArray())
	        r.getJavaBeanArray().add(e);
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    // ====================================================================
    // The void type
    // ====================================================================
    public VoidTestResponse voidTest(VoidTest v)  {

	VoidTestResponse r;
	try {
	    r = new VoidTestResponse();
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return r;
    }

    // ====================================================================
    // other types
    // ====================================================================

    public GYearMonthTestResponse gYearMonthTest(GYearMonthTest v) {

	GYearMonthTestResponse g;
     try {
         g = new GYearMonthTestResponse();
	    g.setResult(v.getValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return g;
    }

    public GYearTestResponse gYearTest(GYearTest v) {

	GYearTestResponse g;
     try {
         g = new GYearTestResponse();
	    g.setResult(v.getValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return g;
    }


    public GMonthDayTestResponse gMonthDayTest(GMonthDayTest v) {

	GMonthDayTestResponse g;
     try {
         g = new GMonthDayTestResponse();
	    g.setResult(v.getValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return g;
    }


    public GDayTestResponse gDayTest(GDayTest v) {

	GDayTestResponse g;
     try {
         g = new GDayTestResponse();
	    g.setResult(v.getValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return g;
    }


    public GMonthTestResponse gMonthTest(GMonthTest v) {

	GMonthTestResponse g;
     try {
         g = new GMonthTestResponse();
	    g.setResult(v.getValue());
	}
	catch (Exception e) {
	    throw new WebServiceException("Failed on object creation: " + e);
	}
	return g;
    }

    public DurationTestResponse durationTest(DurationTest v)  {
     
     DurationTestResponse r;
     try {
         System.out.println("Duration="+v.getDurationValue());
         r = new DurationTestResponse();
         r.setDurationValue(v.getDurationValue());
     }
     catch (Exception e) {
         throw new WebServiceException("Failed on object creation: " + e);
     }
     return r;
    }

}
