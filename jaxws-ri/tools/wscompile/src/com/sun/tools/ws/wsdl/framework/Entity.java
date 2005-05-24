/*
 * $Id: Entity.java,v 1.1 2005-05-24 14:04:11 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * An entity, typically corresponding to an XML element.
 *
 * @author JAX-RPC Development Team
 */
public abstract class Entity implements Elemental {

    public Entity() {
    }

    public Object getProperty(String key) {
        if (_properties == null)
            return null;
        return _properties.get(key);
    }

    public void setProperty(String key, Object value) {
        if (value == null) {
            removeProperty(key);
            return;
        }

        if (_properties == null) {
            _properties = new HashMap();
        }
        _properties.put(key, value);
    }

    public void removeProperty(String key) {
        if (_properties != null) {
            _properties.remove(key);
        }
    }

    public void withAllSubEntitiesDo(EntityAction action) {
        // no-op by default
    }

    public void withAllQNamesDo(QNameAction action) {
        action.perform(getElementName());
    }

    public void withAllEntityReferencesDo(EntityReferenceAction action) {
        // no-op by default
    }

    public abstract void validateThis();

    protected void failValidation(String key) {
        throw new ValidationException(key, getElementName().getLocalPart());
    }

    protected void failValidation(String key, String arg) {
        throw new ValidationException(
            key,
            new Object[] { arg, getElementName().getLocalPart()});
    }

    private Map _properties;
}
