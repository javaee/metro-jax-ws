/**
 * $Id: PortType.java,v 1.2 2005-09-07 19:38:43 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;
import java.util.HashMap;

public class PortType extends HashMap<String, PortTypeOperation>{
    private QName name;

    public PortType(QName name) {
        super();
        this.name = name;
    }

    public QName getName() {
        return name;
    }
}
