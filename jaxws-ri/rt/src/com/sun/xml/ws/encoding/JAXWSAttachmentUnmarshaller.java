/**
 * $Id: JAXWSAttachmentUnmarshaller.java,v 1.8 2005-09-21 22:20:49 vivekp Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
        //TODO localize exception message
        if((ab == null) || ((ab != null) && (ab.getAttachmentPart() == null)))
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
        if((ab == null) || ((ab != null) && (ab.getAttachmentPart() == null)))
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
        try {
            return "<"+URLDecoder.decode(cid, "UTF-8")+">";
        } catch (UnsupportedEncodingException e) {
            throw new WebServiceException(e);
        }
    }

    private Map<String, AttachmentBlock> attachments;
    private boolean isXOP;
}
