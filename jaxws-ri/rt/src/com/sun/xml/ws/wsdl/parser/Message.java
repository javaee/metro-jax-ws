/**
 * $Id: Message.java,v 1.2 2005-09-07 19:38:44 vivekp Exp $
 */
/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashSet;

public class Message extends LinkedHashSet<String>{
    private QName name;

    /**
     * @param name wsdl:message name attribute qualified name
     */
    public Message(QName name) {
        this.name = name;
    }

    public QName getName() {
        return name;
    }
}
