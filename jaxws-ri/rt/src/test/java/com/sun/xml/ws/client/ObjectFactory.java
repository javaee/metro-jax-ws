
package com.sun.xml.ws.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sun.xml.ws.client package. 
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

    private final static QName _FloatRateOutput_QNAME = new QName("http://weblogic/performance/benchmarks/jwswsee/doc", "FloatRateOutput");
    private final static QName _GetRate_QNAME = new QName("http://performance.bea.com", "getRate");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.xml.ws.client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Float }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://weblogic/performance/benchmarks/jwswsee/doc", name = "FloatRateOutput")
    public JAXBElement<Float> createFloatRateOutput(Float value) {
        return new JAXBElement<Float>(_FloatRateOutput_QNAME, Float.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://performance.bea.com", name = "getRate")
    public JAXBElement<String> createGetRate(String value) {
        return new JAXBElement<String>(_GetRate_QNAME, String.class, null, value);
    }

}
