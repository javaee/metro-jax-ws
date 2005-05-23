/*
 * $Id: MessageBlock.java,v 1.1 2005-05-23 22:30:17 bbissett Exp $
 */


/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap.internal;

import javax.xml.namespace.QName;

/**
 * @author JAX-RPC RI Development Team
 */
public class MessageBlock {
    protected QName _name;
    protected Object _value;

    public MessageBlock() {
    }

    public MessageBlock(QName name, Object value) {
        _name = name;
        _value = value;
    }

    public MessageBlock(QName name) {
        _name = name;
    }

    /**
     * @return
     */
    public Object getValue() {
        return _value;
    }

    /**
     * @param element
     */
    public void setValue(Object element) {
        _value = element;
    }

    /**
     * @return
     */
    public QName getName() {
        return _name;
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        _name = name;
    }
}
