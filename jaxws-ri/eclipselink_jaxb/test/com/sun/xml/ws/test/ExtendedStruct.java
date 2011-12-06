
package com.sun.xml.ws.test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for extendedStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="extendedStruct">
 *   &lt;complexContent>
 *     &lt;extension base="{http://performance.bea.com}baseStruct">
 *       &lt;sequence>
 *         &lt;element name="anotherIntMessage" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="intMessage" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="stringMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "extendedStruct", propOrder = {
    "anotherIntMessage",
    "intMessage",
    "stringMessage"
})
@XmlSeeAlso({
    MoreExtendedStruct.class
})
public class ExtendedStruct
    extends BaseStruct
{

    protected int anotherIntMessage;
    protected int intMessage;
    protected String stringMessage;

    /**
     * Gets the value of the anotherIntMessage property.
     * 
     */
    public int getAnotherIntMessage() {
        return anotherIntMessage;
    }

    /**
     * Sets the value of the anotherIntMessage property.
     * 
     */
    public void setAnotherIntMessage(int value) {
        this.anotherIntMessage = value;
    }

    /**
     * Gets the value of the intMessage property.
     * 
     */
    public int getIntMessage() {
        return intMessage;
    }

    /**
     * Sets the value of the intMessage property.
     * 
     */
    public void setIntMessage(int value) {
        this.intMessage = value;
    }

    /**
     * Gets the value of the stringMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStringMessage() {
        return stringMessage;
    }

    /**
     * Sets the value of the stringMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStringMessage(String value) {
        this.stringMessage = value;
    }

}
