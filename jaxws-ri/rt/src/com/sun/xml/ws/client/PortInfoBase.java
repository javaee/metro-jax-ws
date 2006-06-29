/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.client;

import javax.xml.namespace.QName;

/**
 * JAXWS Development Team
 */
public class PortInfoBase {

    protected String targetEndpoint;
    protected String defaultNamespace;
    protected QName name;
    protected QName portTypeName;
    protected String bindingId;

    public PortInfoBase(QName name) {
        this.name = name;
        targetEndpoint = "";
        defaultNamespace = "";
    }

    public PortInfoBase(String targetEndpoint, QName name, String bindingId) {
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

    void setBindingId(String id) {
        bindingId = id;
    }

    public String getBindingId() {
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
