/*
 * $Id: MessagePart.java,v 1.2 2005-07-18 18:14:13 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.EntityReferenceAction;
import com.sun.tools.ws.wsdl.framework.Kind;
import com.sun.tools.ws.wsdl.framework.QNameAction;

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

    public MessagePart() {
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
        if (_descriptorKind == null || _descriptor == null) {
            failValidation("validation.missingRequiredProperty", "descriptor");
        }
    }

    private String _name;
    private QName _descriptor;
    private Kind _descriptorKind;
    private int _bindingKind;
}
