
package com.sun.xml.ws.wsdl.writer.document;

import javax.xml.namespace.QName;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.Documented;

@XmlElement("port")
public interface Port
    extends TypedXmlWriter, Documented
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Port name(String value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Port arrayType(String value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Port binding(QName value);

}
