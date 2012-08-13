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

package com.sun.xml.ws.cts.jws_webparam1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the oracle.j2ee.ws.jaxws.cts.jws_webparam1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Name4_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "name4");
    private final static QName _Name8_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "name8");
    private final static QName _HelloString6_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "helloString6");
    private final static QName _Employee_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "Employee");
    private final static QName _HelloString5_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "helloString5");
    private final static QName _Name2_QNAME = new QName("helloString2/name", "name2");
    private final static QName _Name3_QNAME = new QName("helloString3/name", "name3");
    private final static QName _Name_QNAME = new QName("helloString3/Name", "Name");
    private final static QName _HelloString5Response_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "helloString5Response");
    private final static QName _HelloString8_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "helloString8");
    private final static QName _Name6_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "name6");
    private final static QName _HelloString8Response_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "helloString8Response");
    private final static QName _String1_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "string1");
    private final static QName _HelloString2Response_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "helloString2Response");
    private final static QName _HelloString6Response_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "helloString6Response");
    private final static QName _MyEmployee_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "MyEmployee");
    private final static QName _NameException_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "NameException");
    private final static QName _HelloStringResponse_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "helloStringResponse");
    private final static QName _Name7_QNAME = new QName("http://server.webparam1.webparam.jws.tests.ts.sun.com/", "name7");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: oracle.j2ee.ws.jaxws.cts.jws_webparam1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NameException }
     * 
     */
    public NameException createNameException() {
        return new NameException();
    }

    /**
     * Create an instance of {@link HelloString8Response }
     * 
     */
    public HelloString8Response createHelloString8Response() {
        return new HelloString8Response();
    }

    /**
     * Create an instance of {@link Department }
     * 
     */
    public Department createDepartment() {
        return new Department();
    }

    /**
     * Create an instance of {@link Salary }
     * 
     */
    public Salary createSalary() {
        return new Salary();
    }

    /**
     * Create an instance of {@link Address }
     * 
     */
    public Address createAddress() {
        return new Address();
    }

    /**
     * Create an instance of {@link Employee }
     * 
     */
    public Employee createEmployee() {
        return new Employee();
    }

    /**
     * Create an instance of {@link HelloString8 }
     * 
     */
    public HelloString8 createHelloString8() {
        return new HelloString8();
    }

    /**
     * Create an instance of {@link Name }
     * 
     */
    public Name createName() {
        return new Name();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "name4")
    public JAXBElement<String> createName4(String value) {
        return new JAXBElement<String>(_Name4_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Name }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "name8")
    public JAXBElement<Name> createName8(Name value) {
        return new JAXBElement<Name>(_Name8_QNAME, Name.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "helloString6")
    public JAXBElement<Integer> createHelloString6(Integer value) {
        return new JAXBElement<Integer>(_HelloString6_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Employee }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "Employee")
    public JAXBElement<Employee> createEmployee(Employee value) {
        return new JAXBElement<Employee>(_Employee_QNAME, Employee.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "helloString5")
    public JAXBElement<String> createHelloString5(String value) {
        return new JAXBElement<String>(_HelloString5_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "helloString2/name", name = "name2")
    public JAXBElement<String> createName2(String value) {
        return new JAXBElement<String>(_Name2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "helloString3/name", name = "name3")
    public JAXBElement<String> createName3(String value) {
        return new JAXBElement<String>(_Name3_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Name }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "helloString3/Name", name = "Name")
    public JAXBElement<Name> createName(Name value) {
        return new JAXBElement<Name>(_Name_QNAME, Name.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "helloString5Response")
    public JAXBElement<String> createHelloString5Response(String value) {
        return new JAXBElement<String>(_HelloString5Response_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HelloString8 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "helloString8")
    public JAXBElement<HelloString8> createHelloString8(HelloString8 value) {
        return new JAXBElement<HelloString8>(_HelloString8_QNAME, HelloString8 .class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "name6")
    public JAXBElement<String> createName6(String value) {
        return new JAXBElement<String>(_Name6_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HelloString8Response }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "helloString8Response")
    public JAXBElement<HelloString8Response> createHelloString8Response(HelloString8Response value) {
        return new JAXBElement<HelloString8Response>(_HelloString8Response_QNAME, HelloString8Response.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "string1")
    public JAXBElement<String> createString1(String value) {
        return new JAXBElement<String>(_String1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "helloString2Response")
    public JAXBElement<String> createHelloString2Response(String value) {
        return new JAXBElement<String>(_HelloString2Response_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "helloString6Response")
    public JAXBElement<String> createHelloString6Response(String value) {
        return new JAXBElement<String>(_HelloString6Response_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Employee }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "MyEmployee")
    public JAXBElement<Employee> createMyEmployee(Employee value) {
        return new JAXBElement<Employee>(_MyEmployee_QNAME, Employee.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NameException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "NameException")
    public JAXBElement<NameException> createNameException(NameException value) {
        return new JAXBElement<NameException>(_NameException_QNAME, NameException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "helloStringResponse")
    public JAXBElement<String> createHelloStringResponse(String value) {
        return new JAXBElement<String>(_HelloStringResponse_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://server.webparam1.webparam.jws.tests.ts.sun.com/", name = "name7")
    public JAXBElement<String> createName7(String value) {
        return new JAXBElement<String>(_Name7_QNAME, String.class, null, value);
    }

}
