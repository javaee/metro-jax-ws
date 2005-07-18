/*
 * $Id: MessageBlock.java,v 1.2 2005-07-18 16:52:16 kohlert Exp $
 */


/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap.internal;

import javax.xml.namespace.QName;

/**
 * @author WS Development Team
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
