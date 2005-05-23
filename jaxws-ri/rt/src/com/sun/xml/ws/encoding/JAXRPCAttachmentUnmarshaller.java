/**
 * $Id: JAXRPCAttachmentUnmarshaller.java,v 1.1 2005-05-23 22:36:14 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding;

import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

/**
 * @author Vivek Pandey
 *
 * AttachmentUnmarshaller, will be called by jaxb unmarshaller to process xop package.
 */
public class JAXRPCAttachmentUnmarshaller extends AttachmentUnmarshaller {
    
    /**
     *
     * @param im
     * @param isXOP
     */
    public JAXRPCAttachmentUnmarshaller(InternalMessage im, boolean isXOP){
        this.im = im;
        this.isXOP = isXOP;
    }

    /**
     *
     * @param cid
     * @return
     */
    public DataHandler getAttachmentAsDataHandler(String cid) {
        DataHandler dh = im.getAttachment(decodeCid(cid));
        //TODO localize exception message
        if(dh == null)
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        return dh;
    }

    /**
     *
     * @param cid
     * @return
     */
    public byte[] getAttachmentAsByteArray(String cid) {
        DataHandler dh = im.getAttachment(decodeCid(cid));
        //TODO localize exception message
        if(dh == null)
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        if(dh != null){
            try {
                InputStream is = dh.getInputStream();
                if(is != null){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while(is.read() != -1){
                        baos.write(is.read());
                    }
                    baos.toByteArray();                                
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
     *
     * @param isXOP
     */
    public void setXOPPackage(boolean isXOP){
        this.isXOP = isXOP;
    }

    /**
     *
     * @param cid
     * @return
     */
    private String decodeCid(String cid){
        if(cid.startsWith("cid:"))
            cid = cid.substring(4, cid.length()-1);   
        return cid;
    }
    
    private InternalMessage im;
    private boolean isXOP;
}
