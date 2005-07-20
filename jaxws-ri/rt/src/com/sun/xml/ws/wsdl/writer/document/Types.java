
package com.sun.xml.ws.wsdl.writer.document;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.Documented;
import com.sun.xml.ws.wsdl.writer.document.xsd.Schema;

@XmlElement("types")
public interface Types
    extends TypedXmlWriter, Documented
{
    @XmlElement(value="schema",ns="http://www.w3.org/2001/XMLSchema")
    public Schema schema();
}
