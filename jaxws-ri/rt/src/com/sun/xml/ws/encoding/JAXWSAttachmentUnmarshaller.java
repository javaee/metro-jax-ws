/**
 * $Id: JAXWSAttachmentUnmarshaller.java,v 1.3 2005-05-31 22:38:06 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding;

import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.util.ASCIIUtility;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
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
        AttachmentBlock block = attachments.get(decodeCid(cid));
        //TODO localize exception message
        if(block == null)
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        Object value = block.getValue();
        return new DataHandler(new ByteArrayDataSource((InputStream)block.getValue(),  block.getType()));
    }

    /**
     *
     * @param cid
     * @return
     */
    public byte[] getAttachmentAsByteArray(String cid) {
        AttachmentBlock block = attachments.get(decodeCid(cid));
        if(block == null)
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");

        try {
            return ASCIIUtility.getBytes((InputStream)block.getValue());
        } catch (IOException e) {
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
