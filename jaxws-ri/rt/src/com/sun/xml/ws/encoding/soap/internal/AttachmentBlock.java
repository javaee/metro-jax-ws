/*
 * $Id: AttachmentBlock.java,v 1.7 2005-10-04 23:04:55 kohsuke Exp $
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
package com.sun.xml.ws.encoding.soap.internal;

import javax.xml.soap.AttachmentPart;

/**
 * Attachment of {@link InternalMessage}.
 *
 * @author WS Development Team
 */
public final class AttachmentBlock {
    private final String id;
    private Object value;
    private String type;
    private AttachmentPart ap;

    public AttachmentBlock(AttachmentPart ap){
        this.ap = ap;
        this.id = ap.getContentId();
    }

    public AttachmentBlock(String id, Object value, String type) {
        this.id = id;
        this.value = value;
        this.type = type;
    }

    /**
     * Content ID of the attachment. Uniquely identifies an attachment.
     */
    public String getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public AttachmentPart getAttachmentPart(){
        return ap;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
