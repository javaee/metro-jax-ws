/**
 * $Id: JAXRPCAttachmentMarshaller.java,v 1.2 2005-05-24 17:48:14 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding;

import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

/**
 * @author Vivek Pandey
 *
 *
 */
public class JAXRPCAttachmentMarshaller extends AttachmentMarshaller {

    public JAXRPCAttachmentMarshaller(boolean isXOP){
        this.isXOP = isXOP;
    }

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

    /*
     * @see javax.xml.bind.attachment.AttachmentMarshaller#addMtomAttachment(javax.activation.DataHandler, java.lang.String, java.lang.String)
     */
    public String addMtomAttachment(DataHandler data, String elementNamespace, String elementName) {
        if(!isXOP)
            return null;
        String cid = encodeCid(elementNamespace);
        if(cid != null){
            try {
                cid = "<"+cid+">";
                attachments.put(cid, new AttachmentBlock(cid, data.getInputStream(), data.getContentType()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            isXopped = true;
            cid = "cid:"+cid;
        }
        return cid;
    }

    /*
     * @see javax.xml.bind.attachment.AttachmentMarshaller#addMtomAttachment(byte[], java.lang.String, java.lang.String)
     */
    public String addMtomAttachment(byte[] data, String elementNamespace, String elementName) {
        if(!isXOP)
            return null;
        String cid = encodeCid(elementNamespace);
        if(cid != null){
            cid = "<"+cid+">";
            attachments.put(cid, new AttachmentBlock(cid, new ByteArrayInputStream(data), "application/octet-stream"));
            isXopped = true;
            cid = "cid:"+cid;
        }
        return cid;
    }

    /*
     * @see javax.xml.bind.attachment.AttachmentMarshaller#addSwaRefAttachment(javax.activation.DataHandler)
     */
    public String addSwaRefAttachment(DataHandler data) {
        String cid = encodeCid(null);
        if(cid != null){
            try {
                cid = "<"+cid+">";
                attachments.put(cid, new AttachmentBlock(cid, data.getInputStream(), data.getContentType()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            isXopped = true;
            cid = "cid:"+cid;
        }
        return cid;
    }

    /**
     *
     * @param ns
     * @return
     */
    private String encodeCid(String ns){
        String cid="example.jaxws.sun.com";
        String name = UUID.randomUUID()+"@";
        if(ns != null && (ns.length() > 0)){
            try {
                URI uri = new URI(ns);
                String host = uri.toURL().getHost();
                cid = host;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            } catch (MalformedURLException e) {
                try {
                    cid = URLEncoder.encode(ns, "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        return name + cid;
    }

    /**
     * Must be called before marshalling any data.
     * @param attachments Reference to Map from InternalMessage
     */
    public void setAttachments(Map<String, AttachmentBlock> attachments){
        this.attachments = attachments;
        isXopped = false;
    }

    /**
     *
     * @return
     */
    public boolean isXopped() {
        return isXopped;
    }

    private boolean isXOP;
    private boolean isXopped;
    private Map<String, AttachmentBlock> attachments;

}
