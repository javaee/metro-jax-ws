/*
 * $Id: Request.java,v 1.2 2005-07-18 18:14:00 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model;

/**
 *
 * @author WS Development Team
 */
public class Request extends Message {

    public Request() {}

    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }
}
