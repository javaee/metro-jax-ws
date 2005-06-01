
package com.sun.xml.ws.wsdl.writer.document;

import javax.xml.namespace.QName;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.OpenAtts;

@XmlElement("part")
public interface Part
    extends TypedXmlWriter, OpenAtts
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Part element(QName value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Part type(QName value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Part name(String value);

}
