/*
 * $Id: RpcLitParameter.java,v 1.1 2005-05-23 22:28:41 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.encoding.jaxb;

import javax.xml.namespace.QName;

public class RpcLitParameter {
    private QName name;
    private Class type;
    private Object value;
    private JAXBBridgeInfo bridgeInfo;

    public RpcLitParameter(QName name, Class type) {
        this.name = name;
        this.type = type;
    }

    public RpcLitParameter(QName name, Class type, Object value) {
        this(name, type);
        this.value = value;
    }

    public RpcLitParameter(QName name, JAXBBridgeInfo bridgeInfo) {
        this.name = name;
        this.bridgeInfo = bridgeInfo;
    }

    public QName getName() {
        return name;
    }

    public JAXBBridgeInfo getJAXBBridgeInfo() {
        return bridgeInfo;
    }

    public Class getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
