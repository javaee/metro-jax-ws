/*
 * $Id: PortInfoBase.java,v 1.3 2005-07-20 20:28:22 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import javax.xml.namespace.QName;
import java.net.URI;


/**
 * JAXWS Development Team
 */
public class PortInfoBase {

    protected String targetEndpoint;
    protected String defaultNamespace;
    protected QName name;
    protected QName portTypeName;
    protected java.net.URI bindingId;

    public PortInfoBase(QName name) {
        this.name = name;
        targetEndpoint = "";
        defaultNamespace = "";
    }

    public PortInfoBase(String targetEndpoint, QName name, URI bindingId) {
        this.targetEndpoint = targetEndpoint;
        this.name = name;
        this.bindingId = bindingId;
    }

    void setName(QName nm) {
        name = nm;
    }

    public QName getName() {
        return name;
    }

    void setTargetEndpoint(String endpoint) {
        targetEndpoint = endpoint;
    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    void setBindingId(java.net.URI id) {
        bindingId = id;
    }

    public java.net.URI getBindingId() {
        return bindingId;
    }

    void setPortTypeName(QName typeName) {
        portTypeName = typeName;
    }

    public QName getPortTypeName() {
        return portTypeName;
    }

    void setDefaultNamespace(String namespace) {
        defaultNamespace = namespace;
    }
}
