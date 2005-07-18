/*
 * $Id: Extension.java,v 1.2 2005-07-18 18:14:20 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An entity extending another entity.
 *
 * @author WS Development Team
 */
public abstract class Extension extends Entity {

    public Extension() {
    }

    public Extensible getParent() {
        return _parent;
    }

    public void setParent(Extensible parent) {
        _parent = parent;
    }

    public void accept(ExtensionVisitor visitor) throws Exception {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    private Extensible _parent;
}
