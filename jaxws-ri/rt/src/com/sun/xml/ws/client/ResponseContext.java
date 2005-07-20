/*
 * $Id: ResponseContext.java,v 1.2 2005-07-20 20:28:22 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import java.util.Iterator;


public class ResponseContext extends ContextMap {
    public ResponseContext(Object provider) {
        super(provider);
    }

    public ResponseContext copy() {
        ResponseContext _copy = new ResponseContext(_owner);
        Iterator i = getPropertyNames();

        while (i.hasNext()) {
            String name = (String) i.next();
            Object value = get(name);
            _copy.put(name, value);
        }

        return _copy;
    }
}
