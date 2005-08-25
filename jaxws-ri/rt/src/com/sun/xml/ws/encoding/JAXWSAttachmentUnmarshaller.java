/**
 * $Id: JAXWSAttachmentUnmarshaller.java,v 1.6 2005-08-25 19:03:30 vivekp Exp $
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
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;
import java.util.Map;
import java.io.IOException;

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
     * @return a <code>DataHandler</code> for the attachment
     */
    public DataHandler getAttachmentAsDataHandler(String cid) {
        AttachmentBlock ab = attachments.get(decodeCid(cid));
        //TODO localize exception message
        if((ab == null)&& ((ab != null) && (ab.getAttachmentPart() == null)))
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        try {
            AttachmentPart ap = ab.getAttachmentPart();
            if(ap != null){
                byte[] data = ASCIIUtility.getBytes(ap.getRawContent());
                return new DataHandler(new com.sun.xml.bind.v2.ByteArrayDataSource(data,  ap.getContentType()));
            }
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        } catch (IOException e) {
            throw new WebServiceException(e);
        }
        return null;
    }

    /**
     *
     * @param cid
     * @return the attachment as a <code>byte[]</code>
     */
    public byte[] getAttachmentAsByteArray(String cid) {
        AttachmentBlock ab = attachments.get(decodeCid(cid));
        if((ab == null) && ((ab != null) && (ab.getAttachmentPart() == null)))
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        try {
            return ASCIIUtility.getBytes(ab.getAttachmentPart().getRawContent());
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        } catch (IOException e) {
            throw new WebServiceException(e);
        }
    }

    /**
     *
     * @return true if XOPPackage
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
