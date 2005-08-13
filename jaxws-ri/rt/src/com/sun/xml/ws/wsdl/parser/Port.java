/**
 * $Id: Port.java,v 1.1 2005-08-13 19:30:36 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;

public class Port {
    private QName name;
    private String address;
    private QName binding;

    public Port(QName name, QName binding, String address) {
        this.name = name;
        this.binding = binding;
        this.address = address;
    }

    public QName getName() {
        return name;
    }

    public QName getBinding() {
        return binding;
    }

    public String getAddress() {
        return address;
    }
}
