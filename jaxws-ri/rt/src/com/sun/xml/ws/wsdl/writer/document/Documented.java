/**
 * $Id: Documented.java,v 1.2 2005-08-18 19:11:43 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer.document;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

/**
 *
 * @author WS Development Team
 */
public interface Documented
    extends TypedXmlWriter
{


    @XmlElement
    public com.sun.xml.ws.wsdl.writer.document.Documented documentation(String value);

}
