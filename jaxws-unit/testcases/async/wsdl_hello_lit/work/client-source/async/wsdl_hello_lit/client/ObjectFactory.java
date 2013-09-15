
package async.wsdl_hello_lit.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the async.wsdl_hello_lit.client package. 
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

    private final static QName _Hello1_QNAME = new QName("urn:test:types", "Hello1");
    private final static QName _Header_QNAME = new QName("urn:test:types", "Header");
    private final static QName _ExtraHeader_QNAME = new QName("urn:test:types", "ExtraHeader");
    private final static QName _HelloOut0_QNAME = new QName("urn:test:types", "Hello_out0");
    private final static QName _HelloIn0_QNAME = new QName("urn:test:types", "Hello_in0");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: async.wsdl_hello_lit.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Hello1WorldResponse }
     * 
     */
    public Hello1WorldResponse createHello1WorldResponse() {
        return new Hello1WorldResponse();
    }

    /**
     * Create an instance of {@link HelloOutput }
     * 
     */
    public HelloOutput createHelloOutput() {
        return new HelloOutput();
    }

    /**
     * Create an instance of {@link HelloType }
     * 
     */
    public HelloType createHelloType() {
        return new HelloType();
    }

    /**
     * Create an instance of {@link Hello_Type }
     * 
     */
    public Hello_Type createHello_Type() {
        return new Hello_Type();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HelloType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:test:types", name = "Hello1")
    public JAXBElement<HelloType> createHello1(HelloType value) {
        return new JAXBElement<HelloType>(_Hello1_QNAME, HelloType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HelloType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:test:types", name = "Header")
    public JAXBElement<HelloType> createHeader(HelloType value) {
        return new JAXBElement<HelloType>(_Header_QNAME, HelloType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:test:types", name = "ExtraHeader")
    public JAXBElement<String> createExtraHeader(String value) {
        return new JAXBElement<String>(_ExtraHeader_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:test:types", name = "Hello_out0")
    public JAXBElement<Integer> createHelloOut0(Integer value) {
        return new JAXBElement<Integer>(_HelloOut0_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "urn:test:types", name = "Hello_in0")
    public JAXBElement<Integer> createHelloIn0(Integer value) {
        return new JAXBElement<Integer>(_HelloIn0_QNAME, Integer.class, null, value);
    }

}
