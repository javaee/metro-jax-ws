/*
 * $Id: MIMEXml.java,v 1.2 2005-07-18 18:14:15 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.mime;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A MIME mimeXml extension.
 *
 * @author WS Development Team
 */
public class MIMEXml extends Extension {

    public MIMEXml() {
    }

    public QName getElementName() {
        return MIMEConstants.QNAME_MIME_XML;
    }

    public String getPart() {
        return _part;
    }

    public void setPart(String s) {
        _part = s;
    }

    public void validateThis() {
    }

    private String _part;
}
