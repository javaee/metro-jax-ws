/*
 * $Id: ExtensibilityHelper.java,v 1.2 2005-07-18 18:14:19 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A helper class for extensible entities.
 *
 * @author WS Development Team
 */
public class ExtensibilityHelper {

    public ExtensibilityHelper() {
    }

    public void addExtension(Extension e) {
        if (_extensions == null) {
            _extensions = new ArrayList();
        }
        _extensions.add(e);
    }

    public Iterator extensions() {
        if (_extensions == null) {
            return new Iterator() {
                public boolean hasNext() {
                    return false;
                }

                public Object next() {
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            return _extensions.iterator();
        }
    }

    public void withAllSubEntitiesDo(EntityAction action) {
        if (_extensions != null) {
            for (Iterator iter = _extensions.iterator(); iter.hasNext();) {
                action.perform((Entity) iter.next());
            }
        }
    }

    public void accept(ExtensionVisitor visitor) throws Exception {
        if (_extensions != null) {
            for (Iterator iter = _extensions.iterator(); iter.hasNext();) {
                ((Extension) iter.next()).accept(visitor);
            }
        }
    }

    private List _extensions;
}
