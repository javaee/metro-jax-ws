/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.model.wsdl;

import com.sun.xml.ws.api.model.wsdl.WSDLMessage;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.istack.NotNull;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Vivek Pandey
 */
public final class WSDLOutputImpl extends AbstractExtensibleImpl implements WSDLOutput {
    private String name;
    private QName messageName;
    private WSDLOperationImpl operation;
    private WSDLMessageImpl message;
    private String action;
    private boolean defaultAction = true;
    public WSDLOutputImpl(XMLStreamReader xsr,String name, QName messageName, WSDLOperationImpl operation) {
        super(xsr);
        this.name = name;
        this.messageName = messageName;
        this.operation = operation;
    }

    public String getName() {
        return (name == null)?operation.getName().getLocalPart()+"Response":name;
    }

    public WSDLMessage getMessage() {
        return message;
    }

    public String getAction() {
        return action;
    }

    public boolean isDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(boolean defaultAction) {
        this.defaultAction = defaultAction;
    }

    @NotNull
    public WSDLOperation getOperation() {
        return operation;
    }

    @NotNull
    public QName getQName() {
        return new QName(operation.getName().getNamespaceURI(), getName());
    }

    public void setAction(String action) {
        this.action = action;
    }

    void freeze(WSDLModelImpl root) {
        message = root.getMessage(messageName);
    }
}
