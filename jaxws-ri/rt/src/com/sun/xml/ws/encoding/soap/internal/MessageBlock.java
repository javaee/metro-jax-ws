/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
     * @return the value of this block
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
     * @return the <code>QName</code> of this block
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
