
package com.sun.xml.ws.wsdl.writer.document.soap;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("operation")
public interface Operation
    extends TypedXmlWriter
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.Operation soapAction(String value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.Operation style(String value);

}
