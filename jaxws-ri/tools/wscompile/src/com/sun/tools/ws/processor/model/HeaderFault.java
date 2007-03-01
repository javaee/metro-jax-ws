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
package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.wsdl.framework.Entity;

import javax.xml.namespace.QName;

public class HeaderFault extends Fault {

    public HeaderFault(Entity entity) {
        super(entity);
    }

    public HeaderFault(String name, Entity entity) {
        super(name, entity);
    }

    public QName getMessage() {
        return _message;
    }

    public void setMessage(QName message) {
        _message = message;
    }

    public String getPart() {
        return _part;
    }

    public void setPart(String part) {
        _part = part;
    }

    private QName _message;
    private String _part;
}

