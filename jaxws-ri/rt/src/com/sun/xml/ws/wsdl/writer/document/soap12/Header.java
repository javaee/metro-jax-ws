/**
 * $Id: Header.java,v 1.3 2005-08-18 19:11:50 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer.document.soap12;

import javax.xml.namespace.QName;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

/**
 *
 * @author WS Development Team
 */
@XmlElement("header")
public interface Header
    extends TypedXmlWriter, BodyType
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap12.Header message(QName value);

    @XmlElement
    public HeaderFault headerFault();

    @XmlAttribute
    public BodyType part(String value);

}
