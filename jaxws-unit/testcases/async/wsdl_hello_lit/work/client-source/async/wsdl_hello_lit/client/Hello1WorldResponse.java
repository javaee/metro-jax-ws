
package async.wsdl_hello_lit.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:test:types}HelloOutput"/>
 *         &lt;element ref="{urn:test:types}Header"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "helloOutput",
    "header"
})
@XmlRootElement(name = "hello1WorldResponse", namespace = "")
public class Hello1WorldResponse {

    @XmlElement(name = "HelloOutput", required = true)
    protected HelloOutput helloOutput;
    @XmlElement(name = "Header", required = true)
    protected HelloType header;

    /**
     * Gets the value of the helloOutput property.
     * 
     * @return
     *     possible object is
     *     {@link HelloOutput }
     *     
     */
    public HelloOutput getHelloOutput() {
        return helloOutput;
    }

    /**
     * Sets the value of the helloOutput property.
     * 
     * @param value
     *     allowed object is
     *     {@link HelloOutput }
     *     
     */
    public void setHelloOutput(HelloOutput value) {
        this.helloOutput = value;
    }

    /**
     * Gets the value of the header property.
     * 
     * @return
     *     possible object is
     *     {@link HelloType }
     *     
     */
    public HelloType getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     * 
     * @param value
     *     allowed object is
     *     {@link HelloType }
     *     
     */
    public void setHeader(HelloType value) {
        this.header = value;
    }

}
