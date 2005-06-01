
package com.sun.xml.ws.wsdl.writer.document.soap;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.soap.BodyType;

@XmlElement("fault")
public interface SOAPFault
    extends TypedXmlWriter, BodyType
{
   @XmlAttribute
   public SOAPFault name(String value);

}
