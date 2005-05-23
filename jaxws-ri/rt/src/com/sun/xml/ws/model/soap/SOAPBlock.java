/**
 * $Id: SOAPBlock.java,v 1.1 2005-05-23 22:42:08 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model.soap;

/**
 * @author Vivek Pandey
 *
 * Denotes the soap block
 */
public enum SOAPBlock {
    BODY(0), HEADER(1), ATTACHMENT(2);
    
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
        return type == ATTACHMENT.value();
    }
    
    private final int type;
}
