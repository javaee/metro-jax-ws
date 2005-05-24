/*
 * $Id: Message.java,v 1.1 2005-05-24 14:00:47 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Defining;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.EntityAction;
import com.sun.tools.ws.wsdl.framework.GlobalEntity;
import com.sun.tools.ws.wsdl.framework.Kind;
import com.sun.tools.ws.wsdl.framework.ValidationException;

/**
 * Entity corresponding to the "message" WSDL element.
 *
 * @author JAX-RPC Development Team
 */
public class Message extends GlobalEntity {

    public Message(Defining defining) {
        super(defining);
        _parts = new ArrayList<MessagePart>();
        _partsByName = new HashMap<String, MessagePart>();
    }

    public void add(MessagePart part) {
        if (_partsByName.get(part.getName()) != null)
            throw new ValidationException(
                "validation.duplicateName",
                part.getName());
        _partsByName.put(part.getName(), part);
        _parts.add(part);
    }

    public Iterator<MessagePart> parts() {
        return _parts.iterator();
    }

    public List<MessagePart> getParts(){
        return _parts;
    }

    public MessagePart getPart(String name) {
        return _partsByName.get(name);
    }

    public int numParts() {
        return _parts.size();
    }

    public Kind getKind() {
        return Kinds.MESSAGE;
    }

    public QName getElementName() {
        return WSDLConstants.QNAME_MESSAGE;
    }

    public Documentation getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(Documentation d) {
        _documentation = d;
    }

    public void withAllSubEntitiesDo(EntityAction action) {
        super.withAllSubEntitiesDo(action);

        for (Iterator iter = _parts.iterator(); iter.hasNext();) {
            action.perform((Entity) iter.next());
        }
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.preVisit(this);
        for (Iterator<MessagePart> iter = _parts.iterator(); iter.hasNext();) {
            iter.next().accept(visitor);
        }
        visitor.postVisit(this);
    }

    public void validateThis() {
        if (getName() == null) {
            failValidation("validation.missingRequiredAttribute", "name");
        }
    }

    private Documentation _documentation;
    private List<MessagePart> _parts;
    private Map<String, MessagePart> _partsByName;
}
