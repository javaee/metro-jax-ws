/**
 * $Id: Service.java,v 1.2 2005-08-19 01:17:19 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

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
