/*
 * $Id: NoSuchEntityException.java,v 1.2 2005-07-18 18:14:21 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import javax.xml.namespace.QName;

/**
 * An exception signalling that an entity with the given name/id does not exist.
 *
 * @author WS Development Team
 */
public class NoSuchEntityException extends ValidationException {

    public NoSuchEntityException(QName name) {
        super(
            "entity.notFoundByQName",
            new Object[] { name.getLocalPart(), name.getNamespaceURI()});
    }

    public NoSuchEntityException(String id) {
        super("entity.notFoundByID", id);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.wsdl";
    }
}
