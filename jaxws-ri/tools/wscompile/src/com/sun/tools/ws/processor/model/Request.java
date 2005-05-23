/*
 * $Id: Request.java,v 1.1 2005-05-23 23:18:57 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model;

/**
 *
 * @author JAX-RPC Development Team
 */
public class Request extends Message {

    public Request() {}

    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
