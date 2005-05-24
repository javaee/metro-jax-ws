/*
 * $Id: MIMEContent.java,v 1.1 2005-05-24 13:53:24 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.mime;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A MIME content extension.
 *
 * @author JAX-RPC Development Team
 */
public class MIMEContent extends Extension {

    public MIMEContent() {
    }

    public QName getElementName() {
        return MIMEConstants.QNAME_CONTENT;
    }

    public String getPart() {
        return _part;
    }

    public void setPart(String s) {
        _part = s;
    }

    public String getType() {
        return _type;
    }

    public void setType(String s) {
        _type = s;
    }

    public void validateThis() {
    }

    private String _part;
    private String _type;
}
