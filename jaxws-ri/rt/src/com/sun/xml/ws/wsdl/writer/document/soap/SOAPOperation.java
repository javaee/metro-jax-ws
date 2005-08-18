/**
 * $Id: SOAPOperation.java,v 1.2 2005-08-18 19:11:49 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer.document.soap;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

/**
 *
 * @author WS Development Team
 */
@XmlElement("operation")
public interface SOAPOperation
    extends TypedXmlWriter
{


    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.SOAPOperation soapAction(String value);

    @XmlAttribute
    public com.sun.xml.ws.wsdl.writer.document.soap.SOAPOperation style(String value);

}
