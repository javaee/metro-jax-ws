
package com.sun.xml.ws.wsdl.writer.document;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("definitions")
public interface Definitions
    extends TypedXmlWriter, Documented
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Definitions name(String value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.Definitions targetNamespace(String value);

    @XmlElement
    public Service service();

    @XmlElement
    public Binding binding();

    @XmlElement
    public PortType portType();

    @XmlElement
    public Message message();

    @XmlElement
    public Types types();

    @XmlElement("import")
    public Import _import();

}
