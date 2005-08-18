/**
 * $Id: SOAPFault.java,v 1.2 2005-08-18 19:11:51 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer.document.soap12;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

/**
 *
 * @author WS Development Team
 */
@XmlElement("fault")
public interface SOAPFault
    extends TypedXmlWriter, BodyType
{
   @XmlAttribute
   public SOAPFault name(String value);

}
