/*
 * $Id: RequestContext.java,v 1.2 2005-07-20 20:28:22 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import java.util.Iterator;

import javax.xml.ws.BindingProvider;


public class RequestContext extends ContextMap {

    public RequestContext(Object provider) {
        super(provider);
    }

    public RequestContext(PortInfoBase port, BindingProvider provider) {
        super(port, provider);
    }

    public RequestContext copy() {
        RequestContext _copy = new RequestContext(_owner);
        Iterator i = getPropertyNames();

        while (i.hasNext()) {
            String name = (String) i.next();
            Object value = this.get(name);
            _copy.put(name, value);
        }

        return _copy;
    }
}
