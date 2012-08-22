/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.message;

import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.encoding.MimeMultipartParser;
import com.sun.xml.ws.resources.EncodingMessages;
import com.sun.istack.Nullable;

import javax.xml.ws.WebServiceException;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

/**
 * {@link AttachmentSet} backed by {@link com.sun.xml.ws.encoding.MimeMultipartParser}
 *
 * @author Vivek Pandey
 */
public final class MimeAttachmentSet implements AttachmentSet {
    private final MimeMultipartParser mpp;
    private Map<String, Attachment> atts = new HashMap<String, Attachment>();


    public MimeAttachmentSet(MimeMultipartParser mpp) {
        this.mpp = mpp;
    }

    @Nullable
    public Attachment get(String contentId) {
        Attachment att;
        /**
         * First try to get the Attachment from internal map, maybe this attachment
         * is added by the user.
         */
        att = atts.get(contentId);
        if(att != null)
            return att;
        try {
            /**
             * Attachment is not found in the internal map, now do look in
             * the mpp, if found add to the internal Attachment map.
             */
            att = mpp.getAttachmentPart(contentId);
            if(att != null){
                atts.put(contentId, att);
            }
        } catch (IOException e) {
            throw new WebServiceException(EncodingMessages.NO_SUCH_CONTENT_ID(contentId), e);
        }
        return att;
    }

    /**
     * This is expensive operation, its going to to read all the underlying
     * attachments in {@link MimeMultipartParser}.
     */
    public boolean isEmpty() {
        return atts.size() <= 0 && mpp.getAttachmentParts().isEmpty();
    }

    public void add(Attachment att) {
        atts.put(att.getContentId(), att);
    }

    /**
     * Expensive operation.
     */
    public Iterator<Attachment> iterator() {
        /**
         * Browse thru all the attachments in the mpp, add them to #atts,
         * then return whether its empty.
         */
        Map<String, Attachment> attachments = mpp.getAttachmentParts();
        for(Map.Entry<String, Attachment> att : attachments.entrySet()) {
            if(atts.get(att.getKey()) == null){
                atts.put(att.getKey(), att.getValue());
            }
        }

        return atts.values().iterator();
    }
}
