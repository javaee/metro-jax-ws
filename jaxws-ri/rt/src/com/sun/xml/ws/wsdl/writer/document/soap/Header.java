
package com.sun.xml.ws.wsdl.writer.document.soap;

import javax.xml.namespace.QName;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.soap.BodyType;
import com.sun.xml.ws.wsdl.writer.document.soap.HeaderFault;

@XmlElement("header")
public interface Header
    extends TypedXmlWriter, BodyType
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.Header message(QName value);

    @XmlElement
    public HeaderFault headerFault();

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.BodyType part(String value);
    
}
