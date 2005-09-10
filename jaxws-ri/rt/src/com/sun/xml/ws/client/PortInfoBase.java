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
