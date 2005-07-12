/*
 * $Id: AttachmentBlock.java,v 1.4 2005-07-12 15:54:09 vivekp Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap.internal;

import javax.xml.soap.AttachmentPart;

/**
 * @author JAX-RPC RI Development Team
 */
public class AttachmentBlock {
    private String id;
    private Object value;
    private String type;
    private AttachmentPart ap;

    public AttachmentBlock(AttachmentPart ap){
        this.ap = ap;
    }

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

    public void setValue(Object value) {
        this.value = value;
    }

    public AttachmentPart getAttachmentPart(){
        return ap;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
