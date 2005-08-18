/**
 * $Id: Types.java,v 1.3 2005-08-18 19:11:45 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer.document;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.Documented;
import com.sun.xml.ws.wsdl.writer.document.xsd.Schema;

/**
 *
 * @author WS Development Team
 */
@XmlElement("types")
public interface Types
    extends TypedXmlWriter, Documented
{
    @XmlElement(value="schema",ns="http://www.w3.org/2001/XMLSchema")
    public Schema schema();
}
