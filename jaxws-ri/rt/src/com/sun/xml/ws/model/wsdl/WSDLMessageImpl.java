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
package com.sun.xml.ws.model.wsdl;

import com.sun.xml.ws.api.model.wsdl.WSDLMessage;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;

/**
 * Provides abstraction for wsdl:message
 * @author Vivek Pandey
 */
public final class WSDLMessageImpl extends AbstractExtensibleImpl implements WSDLMessage {
    private final QName name;
    private final ArrayList<WSDLPartImpl> parts;

    /**
     * @param name wsdl:message name attribute value
     */
    public WSDLMessageImpl(XMLStreamReader xsr,QName name) {
        super(xsr);
        this.name = name;
        this.parts = new ArrayList<WSDLPartImpl>();
    }

    public QName getName() {
        return name;
    }

    public void add(WSDLPartImpl part){
        parts.add(part);
    }

    Iterable<WSDLPartImpl> parts(){
        return parts;
    }
}
