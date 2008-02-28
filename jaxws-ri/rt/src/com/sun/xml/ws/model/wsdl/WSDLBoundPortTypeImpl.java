/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.util.QNameMap;
import com.sun.xml.ws.util.exception.LocatableWebServiceException;

import javax.jws.WebParam.Mode;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.MTOMFeature;

/**
 * Implementation of {@link WSDLBoundPortType}
 *
 * @author Vivek Pandey
 */
public final class WSDLBoundPortTypeImpl extends AbstractFeaturedObjectImpl implements WSDLBoundPortType {
    private final QName name;
    private final QName portTypeName;
    private WSDLPortTypeImpl portType;
    private BindingID bindingId;
    private final @NotNull WSDLModelImpl owner;
    private final QNameMap<WSDLBoundOperationImpl> bindingOperations = new QNameMap<WSDLBoundOperationImpl>();

    /**
     * Operations keyed by the payload tag name.
     */
    private QNameMap<WSDLBoundOperationImpl> payloadMap;
    /**
     * {@link #payloadMap} doesn't allow null key, so we store the value for it here.
     */
    private WSDLBoundOperationImpl emptyPayloadOperation;



    public WSDLBoundPortTypeImpl(XMLStreamReader xsr,@NotNull WSDLModelImpl owner, QName name, QName portTypeName) {
        super(xsr);
        this.owner = owner;
        this.name = name;
        this.portTypeName = portTypeName;
        owner.addBinding(this);
    }

    public QName getName() {
        return name;
    }

    public @NotNull WSDLModelImpl getOwner() {
        return owner;
    }

    public WSDLBoundOperationImpl get(QName operationName) {
        return bindingOperations.get(operationName);
    }

    /**
     * Populates the Map that holds operation name as key and {@link WSDLBoundOperation} as the value.
     *
     * @param opName Must be non-null
     * @param ptOp   Must be non-null
     * @throws NullPointerException if either opName or ptOp is null
     */
    public void put(QName opName, WSDLBoundOperationImpl ptOp) {
        bindingOperations.put(opName,ptOp);
    }

    public QName getPortTypeName() {
        return portTypeName;
    }

    public WSDLPortTypeImpl getPortType() {
        return portType;
    }

    public Iterable<WSDLBoundOperationImpl> getBindingOperations() {
        return bindingOperations.values();
    }

    public BindingID getBindingId() {
        //Should the default be SOAP1.1/HTTP binding? For now lets keep it for
        //JBI bug 6509800 
        return (bindingId==null)?BindingID.SOAP11_HTTP:bindingId;
    }

    public void setBindingId(BindingID bindingId) {
        this.bindingId = bindingId;
    }

    /**
     * sets whether the {@link WSDLBoundPortType} is rpc or lit
     */
    private Style style = Style.DOCUMENT;
    public void setStyle(Style style){
        this.style = style;
    }

    public SOAPBinding.Style getStyle() {
        return style;
    }

    public boolean isRpcLit(){
        return Style.RPC==style;
    }

    public boolean isDoclit(){
        return Style.DOCUMENT==style;
    }


    /**
     * Gets the {@link ParameterBinding} for a given operation, part name and the direction - IN/OUT
     *
     * @param operation wsdl:operation@name value. Must be non-null.
     * @param part      wsdl:part@name such as value of soap:header@part. Must be non-null.
     * @param mode      {@link Mode#IN} or {@link Mode@OUT}. Must be non-null.
     * @return null if the binding could not be resolved for the part.
     */
    public ParameterBinding getBinding(QName operation, String part, Mode mode) {
        WSDLBoundOperationImpl op = get(operation);
        if (op == null) {
            //TODO throw exception
            return null;
        }
        if ((Mode.IN == mode) || (Mode.INOUT == mode))
            return op.getInputBinding(part);
        else
            return op.getOutputBinding(part);
    }

    /**
     * Gets mime:content@part value which is the MIME type for a given operation, part and {@link Mode}.
     *
     * @param operation wsdl:operation@name value. Must be non-null.
     * @param part      wsdl:part@name such as value of soap:header@part. Must be non-null.
     * @param mode      {@link Mode#IN} or {@link Mode@OUT}. Must be non-null.
     * @return null if the binding could not be resolved for the part.
     */
    public String getMimeType(QName operation, String part, Mode mode) {
        WSDLBoundOperationImpl op = get(operation);
        if (Mode.IN == mode)
            return op.getMimeTypeForInputPart(part);
        else
            return op.getMimeTypeForOutputPart(part);
    }

    public WSDLBoundOperationImpl getOperation(String namespaceUri, String localName) {
        if(namespaceUri==null && localName == null)
            return emptyPayloadOperation;
        else{
            return payloadMap.get((namespaceUri==null)?"":namespaceUri,localName);
        }
    }

    public void enableMTOM() {
        features.add(new MTOMFeature());
    }

    public boolean isMTOMEnabled() {
        return features.isEnabled(MTOMFeature.class);
    }

    public SOAPVersion getSOAPVersion(){
        return getBindingId().getSOAPVersion();
    }

    void freeze() {
        portType = owner.getPortType(portTypeName);
        if(portType == null){
            throw new LocatableWebServiceException(
                    ClientMessages.UNDEFINED_PORT_TYPE(portTypeName), getLocation());
        }
        portType.freeze();

        for (WSDLBoundOperationImpl op : bindingOperations.values()) {
            op.freeze(owner);
        }

        freezePayloadMap();
        owner.finalizeRpcLitBinding(this);
    }

    private void freezePayloadMap() {
        if(style== Style.RPC) {
            // If the style is rpc then the tag name should be
            // same as operation name so return the operation that matches the tag name.
            payloadMap = bindingOperations;
        } else {
            payloadMap = new QNameMap<WSDLBoundOperationImpl>();
            // For doclit The tag will be the operation that has the same input part descriptor value
            for(WSDLBoundOperationImpl op : bindingOperations.values()){
                QName name = op.getReqPayloadName();
                if(name == null){
                    //empty payload
                    emptyPayloadOperation = op;
                    continue;
                }

                payloadMap.put(name, op);
            }
        }
    }
}
