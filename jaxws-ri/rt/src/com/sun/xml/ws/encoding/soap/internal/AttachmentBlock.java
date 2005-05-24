/*
 * $Id: AttachmentBlock.java,v 1.2 2005-05-24 17:48:12 vivekp Exp $
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
    private InputStream value;
    private String type;

    public AttachmentBlock(String id, InputStream value, String type) {
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

    public InputStream getValue() {
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
