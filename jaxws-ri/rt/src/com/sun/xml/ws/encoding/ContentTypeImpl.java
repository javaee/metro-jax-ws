/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.encoding;

import com.sun.xml.ws.api.pipe.ContentType;

/**
 * @author Vivek Pandey
 */
public final class ContentTypeImpl implements ContentType {
    private final String contentType;
    private final String soapAction;
    private final String accept;

    public ContentTypeImpl(String contentType) {
        this.contentType = contentType;
        this.soapAction = null;
        this.accept = null;
    }
    
    public ContentTypeImpl(String contentType, String soapAction) {
        this.contentType = contentType;
        this.soapAction = getQuotedSOAPAction(soapAction);
        this.accept = null;
    }
    
    public ContentTypeImpl(String contentType, String soapAction, String accept) {
        this.contentType = contentType;
        this.accept = accept;
        this.soapAction = getQuotedSOAPAction(soapAction);
    }

    /** BP 1.1 R1109 requires SOAPAction too be a quoted value **/
    private String getQuotedSOAPAction(String soapAction){
        if(soapAction == null || soapAction.length() == 0){
            return "\"\"";
        }else if(soapAction.charAt(0) != '"' && soapAction.charAt(soapAction.length() -1) != '"'){
            //surround soapAction by double quotes for BP R1109
            return "\"" + soapAction + "\"";
        }else{
            return soapAction;
        }
    }

    public String getContentType() {
        return contentType;
    }

    public String getSOAPActionHeader() {
        return soapAction;
    }

    public String getAcceptHeader() {
        return accept;
    }
}
