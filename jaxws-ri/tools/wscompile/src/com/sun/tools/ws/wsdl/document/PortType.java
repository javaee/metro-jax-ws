/*
 * $Id: PortType.java,v 1.2 2005-07-18 18:14:13 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Defining;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.EntityAction;
import com.sun.tools.ws.wsdl.framework.ExtensibilityHelper;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.GlobalEntity;
import com.sun.tools.ws.wsdl.framework.Kind;
import com.sun.tools.ws.wsdl.framework.ValidationException;

/**
 * Entity corresponding to the "portType" WSDL element.
 *
 * @author WS Development Team
 */
public class PortType extends GlobalEntity implements Extensible{

    public PortType(Defining defining) {
        super(defining);
        _operations = new ArrayList();
        _operationKeys = new HashSet();
        _helper = new ExtensibilityHelper();
    }

    public void add(Operation operation) {
        String key = operation.getUniqueKey();
        if (_operationKeys.contains(key))
            throw new ValidationException(
                "validation.ambiguousName",
                operation.getName());
        _operationKeys.add(key);
        _operations.add(operation);
    }

    public Iterator operations() {
        return _operations.iterator();
    }

    public Set getOperationsNamed(String s) {
        Set result = new HashSet();
        for (Iterator iter = _operations.iterator(); iter.hasNext();) {
            Operation operation = (Operation) iter.next();
            if (operation.getName().equals(s)) {
                result.add(operation);
            }
        }
        return result;
    }

    public Kind getKind() {
        return Kinds.PORT_TYPE;
    }

    public QName getElementName() {
        return WSDLConstants.QNAME_PORT_TYPE;
    }

    public Documentation getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(Documentation d) {
        _documentation = d;
    }

    public void withAllSubEntitiesDo(EntityAction action) {
        super.withAllSubEntitiesDo(action);

        for (Iterator iter = _operations.iterator(); iter.hasNext();) {
            action.perform((Entity) iter.next());
        }
        _helper.withAllSubEntitiesDo(action);
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.preVisit(this);
        _helper.accept(visitor);
        for (Iterator iter = _operations.iterator(); iter.hasNext();) {
            ((Operation) iter.next()).accept(visitor);
        }
        visitor.postVisit(this);
    }

    public void validateThis() {
        if (getName() == null) {
            failValidation("validation.missingRequiredAttribute", "name");
        }
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.wsdl.framework.Extensible#addExtension(com.sun.xml.rpc.wsdl.framework.Extension)
     */
    public void addExtension(Extension e) {
        _helper.addExtension(e);

    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.wsdl.framework.Extensible#extensions()
     */
    public Iterator extensions() {
        return _helper.extensions();
    }

    private Documentation _documentation;
    private List _operations;
    private Set _operationKeys;
    private ExtensibilityHelper _helper;
}
