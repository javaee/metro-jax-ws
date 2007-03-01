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
package com.sun.xml.ws.message;

import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Attachment;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Default dumb {@link AttachmentSet} implementation backed by {@link ArrayList}.
 *
 * <p>
 * The assumption here is that the number of attachments are small enough to
 * justify linear search in {@link #get(String)}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class AttachmentSetImpl implements AttachmentSet {

    private final ArrayList<Attachment> attList = new ArrayList<Attachment>();
    
    /**
     * Creates an empty {@link AttachmentSet}.
     */
    public AttachmentSetImpl() {
    }

    /**
     * Creates an {@link AttachmentSet} by copying contents from another.
     */
    public AttachmentSetImpl(Iterable<Attachment> base) {
        for (Attachment a : base)
            add(a);
    }

    public Attachment get(String contentId) {
        for( int i=attList.size()-1; i>=0; i-- ) {
            Attachment a = attList.get(i);
            if(a.getContentId().equals(contentId))
                return a;
        }
        return null;
    }

    public boolean isEmpty() {
        return attList.isEmpty();
    }

    public void add(Attachment att) {
        attList.add(att);
    }

    public Iterator<Attachment> iterator() {
        return attList.iterator();
    }

}
