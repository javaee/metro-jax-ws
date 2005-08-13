/**
 * $Id: Service.java,v 1.1 2005-08-13 19:30:37 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Service extends LinkedHashMap<QName, Port> {
    private QName name;

    public Service(QName name) {
        super();
        this.name = name;
    }

    public QName getName() {
        return name;
    }
}
