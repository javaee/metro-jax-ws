
package com.sun.xml.ws.wsdl.writer.document;

import javax.xml.namespace.QName;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.ws.wsdl.writer.document.Documented;

public interface FaultType
    extends TypedXmlWriter, Documented
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.FaultType message(QName value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.FaultType name(String value);

}
