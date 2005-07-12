/**
 * $Id: SOAPBlock.java,v 1.2 2005-07-12 23:32:52 kohlert Exp $
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
