/*
 * $Id: InternalMessage.java,v 1.6 2005-10-04 23:04:55 kohsuke Exp $
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

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Represents a SOAP message with headers, a body, and attachments.
 *
 * @author WS Development Team
 */
public class InternalMessage {
    private List<HeaderBlock> headers;
    private Set<QName> headerSet;
    private BodyBlock body;
    private final Map<String,AttachmentBlock> attachments = new HashMap<String, AttachmentBlock>();

    /**
     * @return the <code>BodyBlock</code> for this message
     */
    public BodyBlock getBody() {
        return body;
    }

    public void addHeader(HeaderBlock headerBlock) {
        if (headers == null) {
            headers = new ArrayList<HeaderBlock>();
            headerSet = new HashSet<QName>();
        }
        headers.add(headerBlock);
        headerSet.add(headerBlock.getName());
    }

    /*
     * Checks if a header is already present
     */
    public boolean isHeaderPresent(QName name) {
        if (headerSet == null) {
            return false;
        }
        return headerSet.contains(name);
    }

    /**
     * @return a <code>List</code> of <code>HeaderBlocks</code associated
     * with this message
     */
    public List<HeaderBlock> getHeaders() {
        return headers;
    }

    /**
     * @param body
     */
    public void setBody(BodyBlock body) {
        this.body = body;
    }

    public void addAttachment(AttachmentBlock attachment){
        attachments.put(attachment.getId(),attachment);
    }

    public AttachmentBlock getAttachment(String contentId){
        return attachments.get(contentId);
    }

    /**
     * @return a <code>Map</code> of contentIds to attachments
     */
    public Map<String, AttachmentBlock> getAttachments() {
        return attachments;
    }

}
