/**
 * $Id: ParameterBinding.java,v 1.1 2005-08-19 01:16:11 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model;

import com.sun.xml.ws.model.soap.SOAPBlock;

public class ParameterBinding {
    private SOAPBlock binding;
    private String mimeType;

    public ParameterBinding(SOAPBlock binding) {
        this.binding = binding;
    }

    public SOAPBlock getBinding() {
        return binding;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isBody(){
        return binding.isBody();
    }

    public boolean isHeader(){
        return binding.isHeader();
    }

    public boolean isUnbound(){
        return binding.isUnbound();
    }

    public boolean isAttachment(){
        return binding.isAttachment();
    }
}
