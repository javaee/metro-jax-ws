
package com.sun.xml.ws.test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for moreExtendedStruct complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="moreExtendedStruct">
 *   &lt;complexContent>
 *     &lt;extension base="{http://performance.bea.com}extendedStruct">
 *       &lt;sequence>
 *         &lt;element name="booleanMessage" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "moreExtendedStruct", propOrder = {
    "booleanMessage"
})
public class MoreExtendedStruct
    extends ExtendedStruct
{

    protected boolean booleanMessage;

    /**
     * Gets the value of the booleanMessage property.
     * 
     */
    public boolean isBooleanMessage() {
        return booleanMessage;
    }

    /**
     * Sets the value of the booleanMessage property.
     * 
     */
    public void setBooleanMessage(boolean value) {
        this.booleanMessage = value;
    }

}
