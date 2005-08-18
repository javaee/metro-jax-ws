/**
 * $Id: MultipartRelated.java,v 1.2 2005-08-18 19:11:47 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.writer.document.mime;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;
import com.sun.xml.ws.wsdl.writer.document.mime.Part;

/**
 *
 * @author WS Development Team
 */
@XmlElement("multipartRelated")
public interface MultipartRelated
    extends TypedXmlWriter
{


    @XmlElement
    public Part part();

}
