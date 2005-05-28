/*
 * $Id: AttachmentBlock.java,v 1.3 2005-05-28 01:04:38 vivekp Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap.internal;

import java.io.InputStream;

/**
 * @author JAX-RPC RI Development Team
 */
public class AttachmentBlock {
    private String id;
    private Object value;
    private String type;

    public AttachmentBlock(String id, Object value, String type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(InputStream value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
