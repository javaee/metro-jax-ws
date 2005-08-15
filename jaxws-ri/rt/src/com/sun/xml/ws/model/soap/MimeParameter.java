/**
 * $Id: MimeParameter.java,v 1.1 2005-08-15 22:43:33 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model.soap;

import com.sun.xml.ws.model.Parameter;
import com.sun.xml.ws.model.Mode;
import com.sun.xml.bind.api.TypeReference;

public class MimeParameter extends Parameter{
    private String mimeType;

    /**
     *
     */
    public MimeParameter(TypeReference type, Mode mode, int index, String mimeType) {
        super(type, mode, index);
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}
