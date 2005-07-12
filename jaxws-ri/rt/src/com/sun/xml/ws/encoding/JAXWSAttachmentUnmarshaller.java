/**
 * $Id: JAXWSAttachmentUnmarshaller.java,v 1.4 2005-07-12 15:54:09 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding;

import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import java.util.Map;

/**
 * @author Vivek Pandey
 *
 * AttachmentUnmarshaller, will be called by jaxb unmarshaller to process xop package.
 */
public class JAXWSAttachmentUnmarshaller extends AttachmentUnmarshaller {

    /**
     *
     */
    public JAXWSAttachmentUnmarshaller(){
    }

    /**
     *
     * @param cid
     * @return
     */
    public DataHandler getAttachmentAsDataHandler(String cid) {
        AttachmentBlock ab = attachments.get(decodeCid(cid));
        //TODO localize exception message
        if((ab == null)&& ((ab != null) && (ab.getAttachmentPart() == null)))
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        try {
            AttachmentPart ap = ab.getAttachmentPart();
            if(ap != null)
                return new DataHandler(new com.sun.xml.bind.v2.ByteArrayDataSource(ap.getRawContentBytes(),  ap.getContentType()));
        } catch (SOAPException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    /**
     *
     * @param cid
     * @return
     */
    public byte[] getAttachmentAsByteArray(String cid) {
        AttachmentBlock ab = attachments.get(decodeCid(cid));
        if((ab == null) && ((ab != null) && (ab.getAttachmentPart() == null)))
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        try {
            return ab.getAttachmentPart().getRawContentBytes();
        } catch (SOAPException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    /**
     *
     * @return
     */
    public boolean isXOPPackage() {
        return isXOP;
    }

    /**
     * set the XOP package if the incoming SOAP envelope is a XOP package
     * @param isXOP
     */
    public void setXOPPackage(boolean isXOP){
        this.isXOP = isXOP;
    }

    /**
     * Must be called before marshalling any data.
     * @param attachments Reference to Map from InternalMessage
     */
    public void setAttachments(Map<String, AttachmentBlock> attachments){
        this.attachments = attachments;
    }

    /**
     *
     * @param cid
     * @return
     */
    private String decodeCid(String cid){
        if(cid.startsWith("cid:"))
            cid = cid.substring(4, cid.length());
        return "<"+cid+">";
    }

    private Map<String, AttachmentBlock> attachments;
    private boolean isXOP;
}
