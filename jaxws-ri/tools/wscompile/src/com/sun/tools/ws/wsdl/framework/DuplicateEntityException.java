/*
 * $Id: DuplicateEntityException.java,v 1.2 2005-07-18 18:14:18 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An exception signalling that an entity with the given name/id has already been defined.
 *
 * @author WS Development Team
 */
public class DuplicateEntityException extends ValidationException {

    public DuplicateEntityException(GloballyKnown entity) {
        super(
            "entity.duplicateWithType",
            new Object[] {
                entity.getElementName().getLocalPart(),
                entity.getName()});
    }

    public DuplicateEntityException(Identifiable entity) {
        super(
            "entity.duplicateWithType",
            new Object[] {
                entity.getElementName().getLocalPart(),
                entity.getID()});
    }

    public DuplicateEntityException(Entity entity, String name) {
        super(
            "entity.duplicateWithType",
            new Object[] { entity.getElementName().getLocalPart(), name });
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.wsdl";
    }
}
