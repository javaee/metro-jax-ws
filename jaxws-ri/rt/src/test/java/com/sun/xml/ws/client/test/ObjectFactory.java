/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.client.test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fromjava.bare_710.client package. 
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

    private final static QName _EchoHeadersResponse_QNAME = new QName("http://echo.org/", "echoHeadersResponse");
    private final static QName _Add_QNAME = new QName("http://echo.org/", "add");
    private final static QName _AddNumbers_QNAME = new QName("http://echo.org/", "addNumbers");
    private final static QName _Arg3_QNAME = new QName("http://echo.org/", "arg3");
    private final static QName _AddResponse_QNAME = new QName("http://echo.org/", "addResponse");
    private final static QName _Arg2_QNAME = new QName("http://echo.org/", "arg2");
    private final static QName _Arg1_QNAME = new QName("http://echo.org/", "arg1");
    private final static QName _EchoString_QNAME = new QName("http://echo.org/", "echoString");
    private final static QName _EchoHeaders_QNAME = new QName("http://echo.org/", "echoHeaders");
    private final static QName _AddNumbersResponse_QNAME = new QName("http://echo.org/", "addNumbersResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fromjava.bare_710.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NumbersRequest }
     * 
     */
    public NumbersRequest createNumbersRequest() {
        return new NumbersRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "echoHeadersResponse")
    public JAXBElement<String> createEchoHeadersResponse(String value) {
        return new JAXBElement<String>(_EchoHeadersResponse_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NumbersRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "add")
    public JAXBElement<NumbersRequest> createAdd(NumbersRequest value) {
        return new JAXBElement<NumbersRequest>(_Add_QNAME, NumbersRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NumbersRequest }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "addNumbers")
    public JAXBElement<NumbersRequest> createAddNumbers(NumbersRequest value) {
        return new JAXBElement<NumbersRequest>(_AddNumbers_QNAME, NumbersRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "arg3")
    public JAXBElement<String> createArg3(String value) {
        return new JAXBElement<String>(_Arg3_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "addResponse")
    public JAXBElement<Integer> createAddResponse(Integer value) {
        return new JAXBElement<Integer>(_AddResponse_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "arg2")
    public JAXBElement<String> createArg2(String value) {
        return new JAXBElement<String>(_Arg2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "arg1")
    public JAXBElement<String> createArg1(String value) {
        return new JAXBElement<String>(_Arg1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "echoString")
    public JAXBElement<String> createEchoString(String value) {
        return new JAXBElement<String>(_EchoString_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "echoHeaders")
    public JAXBElement<String> createEchoHeaders(String value) {
        return new JAXBElement<String>(_EchoHeaders_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://echo.org/", name = "addNumbersResponse")
    public JAXBElement<Integer> createAddNumbersResponse(Integer value) {
        return new JAXBElement<Integer>(_AddNumbersResponse_QNAME, Integer.class, null, value);
    }

}
