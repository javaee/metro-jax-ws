/**
 * $Id: Message.java,v 1.1 2005-08-13 19:30:36 vivekp Exp $
 */
/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;

public class Message extends LinkedHashMap<String, QName>{
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
