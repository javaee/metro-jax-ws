
package com.sun.xml.ws.wsdl.writer.document.mime;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.mime.Part;

@XmlElement("multipartRelated")
public interface MultipartRelated
    extends TypedXmlWriter
{


    @XmlElement
    public Part part();

}
