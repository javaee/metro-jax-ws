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

import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPortType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.Hashtable;
import java.util.Map;

/**
 * Provides implementation of {@link WSDLPortType}
 *
 * @author Vivek Pandey
 */
public final class WSDLPortTypeImpl  extends AbstractExtensibleImpl implements WSDLPortType {
    private QName name;
    private final Map<String, WSDLOperationImpl> portTypeOperations;
    private WSDLModelImpl owner;

    public WSDLPortTypeImpl(XMLStreamReader xsr,WSDLModelImpl owner, QName name) {
        super(xsr);
        this.name = name;
        this.owner = owner;
        portTypeOperations = new Hashtable<String, WSDLOperationImpl>();
    }

    public QName getName() {
        return name;
    }

    public WSDLOperationImpl get(String operationName) {
        return portTypeOperations.get(operationName);
    }

    public Iterable<WSDLOperationImpl> getOperations() {
        return portTypeOperations.values();
    }

    /**
     * Populates the Map that holds operation name as key and {@link WSDLOperation} as the value.
     * @param opName Must be non-null
     * @param ptOp  Must be non-null
     * @throws NullPointerException if either opName or ptOp is null
     */
    public void put(String opName, WSDLOperationImpl ptOp){
        portTypeOperations.put(opName, ptOp);
    }

    WSDLModelImpl getOwner(){
        return owner;
    }

    void freeze() {
        for(WSDLOperationImpl op : portTypeOperations.values()){
            op.freez(owner);
        }
    }
}
