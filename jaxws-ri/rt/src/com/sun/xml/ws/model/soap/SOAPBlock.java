/**
 * $Id: SOAPBlock.java,v 1.4 2005-08-15 22:56:16 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model.soap;

/**
 * Denotes the soap block
 *
 * @author Vivek Pandey
 */
public enum SOAPBlock {
    BODY(0), HEADER(1), MIME(2), UNBOUND(3);
    
    SOAPBlock(int type){
        this.type = type;
    }

    public int value() {
        return type;
    }

    public boolean isHeader(){
        return type == HEADER.value();
    }

    public boolean isBody(){
        return type == BODY.value();
    }

    public boolean isAttachment(){
        return type == MIME.value();
    }

    public boolean isUnbound(){
        return type == UNBOUND.value();
    }

    private final int type;
}
