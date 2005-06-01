
package com.sun.xml.ws.wsdl.writer.document.soap;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("binding")
public interface SOAPBinding
    extends TypedXmlWriter
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.SOAPBinding transport(String value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.SOAPBinding style(String value);

}
