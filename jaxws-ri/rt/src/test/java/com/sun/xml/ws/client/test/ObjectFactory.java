
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
