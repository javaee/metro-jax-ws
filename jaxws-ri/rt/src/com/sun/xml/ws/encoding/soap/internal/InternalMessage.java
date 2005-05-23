/*
 * $Id: InternalMessage.java,v 1.1 2005-05-23 22:30:16 bbissett Exp $
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
 * @author JAX-RPC RI Development Team
 */
public class InternalMessage {
    private List<HeaderBlock> headers;
    private Set<QName> headerSet;
    private BodyBlock body;
    private Map<String, DataHandler> attachments = new HashMap<String, DataHandler>();

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

    public void addAttachment(String contentId, DataHandler attachment){
        attachments.put(contentId, attachment);
    }

    public DataHandler getAttachment(String contentId){
        return attachments.get(contentId);
    }

    /**
     * @return
     */
    public Map<String, DataHandler> getAttachments() {
        return attachments;
    }

}
