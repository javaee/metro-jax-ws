
package com.sun.xml.ws.wsdl.writer.document.soap12;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("fault")
public interface SOAPFault
    extends TypedXmlWriter, BodyType
{
   @XmlAttribute
   public SOAPFault name(String value);

}
