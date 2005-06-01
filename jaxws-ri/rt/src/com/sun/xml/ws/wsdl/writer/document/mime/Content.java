
package com.sun.xml.ws.wsdl.writer.document.mime;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("content")
public interface Content
    extends TypedXmlWriter
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.mime.Content type(String value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.mime.Content part(String value);

}
