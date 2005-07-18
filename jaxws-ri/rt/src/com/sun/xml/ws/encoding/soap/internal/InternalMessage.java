/*
 * $Id: InternalMessage.java,v 1.3 2005-07-18 16:52:16 kohlert Exp $
 */


/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap.internal;

import java.util.*;

import javax.xml.namespace.QName;
import javax.activation.DataHandler;


/**
 * @author WS Development Team
 */
public class InternalMessage {
    private List<HeaderBlock> headers;
    private Set<QName> headerSet;
    private BodyBlock body;
    private Map<String, AttachmentBlock> attachments = new HashMap<String, AttachmentBlock>();

    /**
     * @return
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
     * @return
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

    public void addAttachment(String contentId, AttachmentBlock attachment){
        attachments.put(contentId, attachment);
    }

    public AttachmentBlock getAttachment(String contentId){
        return attachments.get(contentId);
    }

    /**
     * @return
     */
    public Map<String, AttachmentBlock> getAttachments() {
        return attachments;
    }

}
