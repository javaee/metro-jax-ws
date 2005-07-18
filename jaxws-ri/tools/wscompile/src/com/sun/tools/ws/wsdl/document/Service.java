/*
 * $Id: Service.java,v 1.2 2005-07-18 18:14:13 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Defining;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.EntityAction;
import com.sun.tools.ws.wsdl.framework.ExtensibilityHelper;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.GlobalEntity;
import com.sun.tools.ws.wsdl.framework.Kind;

/**
 * Entity corresponding to the "service" WSDL element.
 *
 * @author WS Development Team
 */
public class Service extends GlobalEntity implements Extensible {

    public Service(Defining defining) {
        super(defining);
        _ports = new ArrayList();
        _helper = new ExtensibilityHelper();
    }

    public void add(Port port) {
        port.setService(this);
        _ports.add(port);
    }

    public Iterator <Port> ports() {
        return _ports.iterator();
    }

    public Kind getKind() {
        return Kinds.SERVICE;
    }

    public QName getElementName() {
        return WSDLConstants.QNAME_SERVICE;
    }

    public Documentation getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(Documentation d) {
        _documentation = d;
    }

    public void withAllSubEntitiesDo(EntityAction action) {
        for (Iterator iter = _ports.iterator(); iter.hasNext();) {
            action.perform((Entity) iter.next());
        }
        _helper.withAllSubEntitiesDo(action);
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.preVisit(this);
        for (Iterator iter = _ports.iterator(); iter.hasNext();) {
            ((Port) iter.next()).accept(visitor);
        }
        _helper.accept(visitor);
        visitor.postVisit(this);
    }

    public void validateThis() {
        if (getName() == null) {
            failValidation("validation.missingRequiredAttribute", "name");
        }
    }

    public void addExtension(Extension e) {
        _helper.addExtension(e);
    }

    public Iterator extensions() {
        return _helper.extensions();
    }

    private ExtensibilityHelper _helper;
    private Documentation _documentation;
    private List <Port> _ports;
}
