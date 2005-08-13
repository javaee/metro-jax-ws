/**
 * $Id: PortType.java,v 1.1 2005-08-13 19:30:36 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;
import java.util.HashMap;

public class PortType extends HashMap<QName, PortTypeOperation>{
    private QName name;

    public PortType(QName name) {
        super();
        this.name = name;
    }

    public QName getName() {
        return name;
    }
}
