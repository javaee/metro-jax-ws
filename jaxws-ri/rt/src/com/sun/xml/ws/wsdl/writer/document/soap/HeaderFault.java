/**
 * $Id: HeaderFault.java,v 1.2 2005-08-18 19:11:48 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer.document.soap;

import javax.xml.namespace.QName;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.soap.BodyType;

/**
 *
 * @author WS Development Team
 */
@XmlElement("headerFault")
public interface HeaderFault
    extends TypedXmlWriter, BodyType
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.HeaderFault message(QName value);

}
