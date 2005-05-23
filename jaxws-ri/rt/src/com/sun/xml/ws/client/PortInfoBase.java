/*
 * $Id: PortInfoBase.java,v 1.1 2005-05-23 22:17:54 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import javax.xml.namespace.QName;

/**
 * Created by IntelliJ IDEA.
 * User: kwalsh
 * Date: Jan 6, 2005
 * Time: 12:03:32 PM
 * To change this template use File | Settings | File Templates.
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

    void setName(QName nm) {
        name = nm;
    }

    public QName getName() {
        return name;
    }

    public void setTargetEndpoint(String endpoint) {
        targetEndpoint = endpoint;
    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }


    public void setBindingId(java.net.URI id) {
        bindingId = id;
    }

    public java.net.URI getBindingId() {
        return bindingId;
    }

    public void setPortTypeName(QName typeName) {
        portTypeName = typeName;
    }

    public QName getPortTypeName() {
        return portTypeName;
    }

    public void setDefaultNamespace(String namespace) {
        defaultNamespace = namespace;
    }

}
