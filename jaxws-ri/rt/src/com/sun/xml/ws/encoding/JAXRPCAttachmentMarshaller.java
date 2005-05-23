/**
 * $Id: JAXRPCAttachmentMarshaller.java,v 1.1 2005-05-23 22:36:14 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding;

import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;

/**
 * @author Vivek Pandey
 *
 * 
 */
public class JAXRPCAttachmentMarshaller extends AttachmentMarshaller {

    public JAXRPCAttachmentMarshaller(InternalMessage im, boolean isXOP){
        this.isXOP = isXOP;
        this.im = im;
    }

    public boolean isXOPPackage() {
        return isXOP;
    }

    /*
     * @see javax.xml.bind.attachment.AttachmentMarshaller#addMtomAttachment(javax.activation.DataHandler, java.lang.String, java.lang.String)
     */
    public String addMtomAttachment(DataHandler data, String elementNamespace, String elementName) {
        if(!isXOP)
            return null;
        String cid = encodeCid(elementNamespace, elementName);
        if(cid != null){
            im.addAttachment(cid, data);
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
        String cid = encodeCid(elementNamespace, elementName);
        if(cid != null){
            DataHandler dh = new DataHandler(data, "*/*");
            im.addAttachment(cid, dh);
            cid = "cid:"+cid;
        }
        return cid;
    }

    /* 
     * @see javax.xml.bind.attachment.AttachmentMarshaller#addSwaRefAttachment(javax.activation.DataHandler)
     */
    public String addSwaRefAttachment(DataHandler data) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private String encodeCid(String ns, String name){
        String cid=null;
        try {
            name = "<"+URLEncoder.encode(name, "UTF-8")+"="+UUID.randomUUID()+"@";
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        }
        try {
            URI uri = new URI(ns);
            URL host = uri.toURL();
            cid += host;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            try {
                cid += URLEncoder.encode(ns, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        cid += ">";
        return cid;
    }

    private boolean isXOP;
    private InternalMessage im;
}
