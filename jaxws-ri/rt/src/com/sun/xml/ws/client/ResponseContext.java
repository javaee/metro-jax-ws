/*
 * $Id: ResponseContext.java,v 1.1 2005-05-23 22:26:37 bbissett Exp $
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
