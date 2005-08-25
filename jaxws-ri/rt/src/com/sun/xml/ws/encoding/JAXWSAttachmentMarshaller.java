/**
 * $Id: JAXWSAttachmentMarshaller.java,v 1.11 2005-08-25 19:03:30 vivekp Exp $
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
 * @author WS Development Team
 *
 *
 */
public class JAXWSAttachmentMarshaller extends AttachmentMarshaller {

    public JAXWSAttachmentMarshaller(boolean isXOP){
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
     * @see AttachmentMarshaller#addMtomAttachment(DataHandler, String, String)
     */
    public String addMtomAttachment(DataHandler data, String elementNamespace, String elementName) {
        if(!isXOP)
            return null;
        String cid = encodeCid(elementNamespace);
        if(cid != null){
            attachments.put("<"+cid+">", new AttachmentBlock("<"+cid+">", data, data.getContentType()));
            isXopped = true;
            cid = "cid:"+cid;
        }
        return cid;
    }

    /**
     * <p>Consider binary <code>data</code> for optimized binary storage as an attachment.
     * <p/>
     * <p>Since content type is not known, the attachment's MIME content type must be set to "application/octet-stream".</p>
     * <p/>
     * <p/>
     * The <code>elementNamespace</code> and <code>elementLocalName</code>
     * parameters provide the
     * context that contains the binary data. This information could
     * be used by the MIME-based package processor to determine if the
     * binary data should be inlined or optimized as an attachment.
     *
     * @param data             represents the data to be attached. Must be non-null. The actual data region is
     *                         specified by <tt>(data,offset,len)</tt> tuple.
     * @param mimeType         If the data has an associated MIME type known to JAXB, that is passed
     *                         as this parameter. If none is known, "application/octet-stream".
     *                         This parameter may never be null.
     * @param elementNamespace the namespace URI of the element that encloses the base64Binary data.
     *                         Can be empty but never null.
     * @param elementLocalName The local name of the element. Always a non-null valid string.
     * @return content-id URI, cid, to the attachment containing
     *         <code>data</code> or null if data should be inlined.
     * @see #addMtomAttachment(javax.activation.DataHandler, String, String)
     */
    public String addMtomAttachment(byte[] data, int offset, int len, String mimeType, String elementNamespace, String elementLocalName) {
        if(!isXOP)
            return null;

        //TODO: With performance results we need to find out what length would need optimization
        if(len < 1000)
            return null;

        //this will not be needed if saaj exposes api that takes
        //byte[] actualData = getActualData(data, offset, len);

        String cid = encodeCid(elementNamespace);
        if(cid != null){
            attachments.put("<"+cid+">", new AttachmentBlock("<"+cid+">", new ByteArray(data, offset, len), "application/octet-stream"));
            isXopped = true;
            cid = "cid:"+cid;
        }
        return cid;

    }

    private byte[] getActualData(byte[] data, int offset, int len) {
        if((offset == 0) && (len == data.length))
            return data;
        byte[] actualData = new byte[len];
        for(int i = 0; i < len; i++){
            actualData[i] = data[offset + i];
        }
        return actualData;
    }

    /*
     * @see AttachmentMarshaller#addSwaRefAttachment(DataHandler)
     */
    public String addSwaRefAttachment(DataHandler data) {
        String cid = encodeCid(null);
        if(cid != null){
            attachments.put("<"+cid+">", new AttachmentBlock("<"+cid+">", data, data.getContentType()));
            isXopped = false;
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
     * @return true if Xopped, false otherwise
     */
    public boolean isXopped() {
        return isXopped;
    }

    private boolean isXOP;
    private boolean isXopped;
    private Map<String, AttachmentBlock> attachments;

}
