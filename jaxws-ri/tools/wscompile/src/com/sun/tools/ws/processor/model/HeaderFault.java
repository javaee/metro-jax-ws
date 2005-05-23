/**
 * $Id: HeaderFault.java,v 1.1 2005-05-23 23:18:55 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model;

import javax.xml.namespace.QName;

public class HeaderFault extends Fault {

    public HeaderFault() {}

    public HeaderFault(String name) {
        super(name);
    }

    public QName getMessage() {
        return _message;
    }

    public void setMessage(QName message) {
        _message = message;
    }

    public String getPart() {
        return _part;
    }

    public void setPart(String part) {
        _part = part;
    }

    private QName _message;
    private String _part;
}

