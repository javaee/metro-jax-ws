/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.encoding;

import com.sun.istack.Nullable;
import com.sun.istack.NotNull;

/**
 * @author Vivek Pandey
 */
public final class ContentTypeImpl implements com.sun.xml.ws.api.pipe.ContentType {
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
            tmpCharset = new ContentType(contentType).getParameter("charset");
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
