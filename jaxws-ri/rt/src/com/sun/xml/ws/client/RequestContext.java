/*
 * $Id: RequestContext.java,v 1.1 2005-05-23 22:26:37 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client;

import javax.xml.ws.BindingProvider;
import java.util.Iterator;

public class RequestContext extends ContextMap {

    public RequestContext(Object provider) {
        super(provider);
    }

    public RequestContext(PortInfo port, BindingProvider provider) {
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
