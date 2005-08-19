/**
 * $Id: Port.java,v 1.2 2005-08-19 01:17:18 vivekp Exp $
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
    private QName bindingName;
    private Binding binding;

    public Port(QName name, QName binding, String address) {
        this.name = name;
        this.bindingName = binding;
        this.address = address;
    }

    public QName getName() {
        return name;
    }

    public QName getBindingName() {
        return bindingName;
    }

    public String getAddress() {
        return address;
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }


}
