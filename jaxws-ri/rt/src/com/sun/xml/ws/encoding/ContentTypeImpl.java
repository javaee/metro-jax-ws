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

import com.sun.istack.Nullable;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.pipe.ContentType;

/**
 * @author Vivek Pandey
 */
public final class ContentTypeImpl implements ContentType {
    private final @NotNull String contentType;
    private final @NotNull String soapAction;
    private final @Nullable String accept;
    private final @Nullable String charset;

    public ContentTypeImpl(String contentType) {
        this(contentType, null, null);
    }
    
    public ContentTypeImpl(String contentType, @Nullable String soapAction) {
        this(contentType, soapAction, null);
    }
    
    public ContentTypeImpl(String contentType, @Nullable String soapAction, @Nullable String accept) {
        this.contentType = contentType;
        this.accept = accept;
        this.soapAction = getQuotedSOAPAction(soapAction);
        String tmpCharset = null;
        try {
            tmpCharset = new com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType(contentType).getParameter("charset");
        } catch(Exception e) {
            //Ignore the parsing exception.
        }
        charset = tmpCharset;
    }

    /**
     * Returns the character set encoding.
     *
     * @return returns the character set encoding.
     */
    public @Nullable String getCharSet() {
        return charset;
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
