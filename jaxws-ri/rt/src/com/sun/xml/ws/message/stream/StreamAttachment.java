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

package com.sun.xml.ws.message.stream;

import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.util.ByteArrayDataSource;
import com.sun.xml.ws.util.ByteArrayBuffer;

import javax.activation.DataHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import org.jvnet.staxex.Base64Data;

/**
 * Attachment created from raw bytes.
 *
 * @author Vivek Pandey
 */
public class StreamAttachment implements Attachment {
    private final String contentId;
    private final String contentType;
    private final ByteArrayBuffer byteArrayBuffer;
    private final byte[] data;
    private final int len;

    public StreamAttachment(ByteArrayBuffer buffer, String contentId, String contentType) {
        this.contentId = contentId;
        this.contentType = contentType;
        this.byteArrayBuffer = buffer;
        this.data = byteArrayBuffer.getRawData();
        this.len = byteArrayBuffer.size();
    }

    public String getContentId() {
        return contentId;
    }

    public String getContentType() {
        return contentType;
    }


    public byte[] asByteArray() {
        //we got to reallocate and give the exact byte[]
        return byteArrayBuffer.toByteArray();
    }

    public DataHandler asDataHandler() {
        return new DataHandler(new ByteArrayDataSource(data,0,len,getContentType()));
    }

    public Source asSource() {
        return new StreamSource(new ByteArrayInputStream(data,0,len));
    }

    public InputStream asInputStream() {
        return byteArrayBuffer.newInputStream();
    }

    public Base64Data asBase64Data(){
        Base64Data base64Data = new Base64Data();
        base64Data.set(data, len, contentType);
        return base64Data;
    }

    public void writeTo(OutputStream os) throws IOException {
        byteArrayBuffer.writeTo(os);
    }

    public void writeTo(SOAPMessage saaj) throws SOAPException {
        AttachmentPart part = saaj.createAttachmentPart();
        part.setRawContentBytes(data,0,len,getContentType());
        part.setContentId(contentId);
        saaj.addAttachmentPart(part);
    }
}
