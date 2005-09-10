/**
 * $Id: ParameterBinding.java,v 1.2 2005-09-10 19:47:49 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
