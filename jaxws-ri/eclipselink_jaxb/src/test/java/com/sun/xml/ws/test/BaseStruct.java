
package com.sun.xml.ws.test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for baseStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="baseStruct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="floatMessage" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="shortMessage" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "baseStruct", propOrder = {
    "floatMessage",
    "shortMessage"
})
@XmlSeeAlso({
    ExtendedStruct.class
})
public class BaseStruct {

    protected float floatMessage;
    protected short shortMessage;

    /**
     * Gets the value of the floatMessage property.
     * 
     */
    public float getFloatMessage() {
        return floatMessage;
    }

    /**
     * Sets the value of the floatMessage property.
     * 
     */
    public void setFloatMessage(float value) {
        this.floatMessage = value;
    }

    /**
     * Gets the value of the shortMessage property.
     * 
     */
    public short getShortMessage() {
        return shortMessage;
    }

    /**
     * Sets the value of the shortMessage property.
     * 
     */
    public void setShortMessage(short value) {
        this.shortMessage = value;
    }

}
