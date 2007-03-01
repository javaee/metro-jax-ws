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

package com.sun.tools.ws.wsdl.document;

import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.EntityReferenceAction;
import com.sun.tools.ws.wsdl.framework.Kind;
import com.sun.tools.ws.wsdl.framework.QNameAction;
import org.xml.sax.Locator;

import javax.jws.WebParam.Mode;
import javax.xml.namespace.QName;

/**
 * Entity corresponding to a WSDL message part.
 *
 * @author WS Development Team
 */
public class MessagePart extends Entity {

    public static final int SOAP_BODY_BINDING = 1;
    public static final int SOAP_HEADER_BINDING = 2;
    public static final int SOAP_HEADERFAULT_BINDING = 3;
    public static final int SOAP_FAULT_BINDING = 4;
    public static final int WSDL_MIME_BINDING = 5;
    public static final int PART_NOT_BOUNDED = -1;

    public MessagePart(Locator locator) {
        super(locator);
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public QName getDescriptor() {
        return _descriptor;
    }

    public void setDescriptor(QName n) {
        _descriptor = n;
    }

    public Kind getDescriptorKind() {
        return _descriptorKind;
    }

    public void setDescriptorKind(Kind k) {
        _descriptorKind = k;
    }

    public QName getElementName() {
        return WSDLConstants.QNAME_PART;
    }

    public int getBindingExtensibilityElementKind(){
        return _bindingKind;
    }

    public void setBindingExtensibilityElementKind(int kind) {
        _bindingKind = kind;
    }

    public void withAllQNamesDo(QNameAction action) {
        if (_descriptor != null) {
            action.perform(_descriptor);
        }
    }

    public void withAllEntityReferencesDo(EntityReferenceAction action) {
        super.withAllEntityReferencesDo(action);
        if (_descriptor != null && _descriptorKind != null) {
            action.perform(_descriptorKind, _descriptor);
        }
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    public void validateThis() {
        if(_descriptor != null && _descriptor.getLocalPart().equals("")){
            failValidation("validation.invalidElement", _descriptor.toString());
        }
    }

    public void setMode(Mode mode){
        this.mode = mode;
    }

    public Mode getMode(){
        return mode;
    }

    public boolean isINOUT(){
        if(mode!=null)
            return (mode == Mode.INOUT);
        return false;
    }

    public boolean isIN(){
        if(mode!=null)
            return (mode == Mode.IN);
        return false;
    }

    public boolean isOUT(){
        if(mode!=null)
            return (mode == Mode.OUT);
        return false;
    }

    public void setReturn(boolean ret){
        isRet=ret;
    }

    public boolean isReturn(){
        return isRet;
    }


    private boolean isRet;
    private String _name;
    private QName _descriptor;
    private Kind _descriptorKind;
    private int _bindingKind;

    private Mode mode;
}
