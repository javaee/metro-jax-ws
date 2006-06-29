/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;

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
        if(ab == null)
            //TODO localize exception message
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        return ab.asDataHandler();
    }

    /**
     *
     * @param cid
     * @return the attachment as a <code>byte[]</code>
     */
    public byte[] getAttachmentAsByteArray(String cid) {
        AttachmentBlock ab = attachments.get(decodeCid(cid));
        if(ab == null)
            throw new IllegalArgumentException("Attachment corresponding to "+cid+ " not found!");
        return ab.asByteArray();
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
        try {
            return "<"+URLDecoder.decode(cid, "UTF-8")+">";
        } catch (UnsupportedEncodingException e) {
            throw new WebServiceException(e);
        }
    }

    private Map<String, AttachmentBlock> attachments;
    private boolean isXOP;
}
