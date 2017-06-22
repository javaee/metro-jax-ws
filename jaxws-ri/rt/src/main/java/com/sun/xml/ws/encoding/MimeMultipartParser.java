/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.encoding;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentEx;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.ws.developer.StreamingDataHandler;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.ByteArrayDataSource;

import org.jvnet.mimepull.Header;
import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEPart;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses Mime multipart message into primary part and attachment parts. It
 * parses the stream lazily as and when required.
 *
 * @author Vivek Pandey
 * @author Jitendra Kotamraju
 */
public final class MimeMultipartParser {

    private final String start;
    private final MIMEMessage message;
    private Attachment root;
    private ContentTypeImpl contentType;
    
    // Attachments without root part
    private final Map<String, Attachment> attachments = new HashMap<String, Attachment>();

    private boolean gotAll;

    public MimeMultipartParser(InputStream in, String cType, StreamingAttachmentFeature feature) {
        this.contentType = new ContentTypeImpl(cType);
//        ContentType ct = new ContentType(cType);
//        String boundary = ct.getParameter("boundary");
        String boundary = contentType.getBoundary();
        if (boundary == null || boundary.equals("")) {
            throw new WebServiceException("MIME boundary parameter not found" + contentType);
        }
        message = (feature != null)
                ? new MIMEMessage(in, boundary, feature.getConfig())
                : new MIMEMessage(in, boundary);
        // Strip <...> from root part's Content-ID
//        String st = ct.getParameter("start");
        String st = contentType.getRootId();
        if (st != null && st.length() > 2 && st.charAt(0) == '<' && st.charAt(st.length()-1) == '>') {
            st = st.substring(1, st.length()-1);
        }
        start = st;
    }

    /**
     * Parses the stream and returns the root part. If start parameter is
     * present in Content-Type, it is used to determine the root part, otherwise
     * root part is the first part.
     *
     * @return StreamAttachment for root part
     *         null if root part cannot be found
     *
     */
    public @Nullable Attachment getRootPart() {
        if (root == null) {
            root = new PartAttachment((start != null) ? message.getPart(start) : message.getPart(0));
        }
        return root;
    }

    /**
     * Parses the entire stream and returns all MIME parts except root MIME part.
     *
     * @return Map<String, StreamAttachment> for all attachment parts
     */
    public @NotNull Map<String, Attachment> getAttachmentParts() {
        if (!gotAll) {
            MIMEPart rootPart = (start != null) ? message.getPart(start) : message.getPart(0);
            List<MIMEPart> parts = message.getAttachments();
            for(MIMEPart part : parts) {
                if (part != rootPart) {
                    String cid = part.getContentId();
                    if (!attachments.containsKey(cid)) {
                        PartAttachment attach = new PartAttachment(part);
                        attachments.put(attach.getContentId(), attach);
                    }
                }
            }
            gotAll = true;
        }
        return attachments;
    }

    /**
     * This method can be called to get a matching MIME attachment part for the
     * given contentId. It parses the stream until it finds a matching part.
     *
     * @return StreamAttachment attachment for contentId
     *         null if there is no attachment for contentId
     */
    public @Nullable Attachment getAttachmentPart(String contentId) throws IOException {
        //first see if this attachment is already parsed, if so return it
        Attachment attach = attachments.get(contentId);
        if (attach == null) {
            MIMEPart part = message.getPart(contentId);
            attach = new PartAttachment(part);
            attachments.put(contentId, attach);
        }
        return attach;
    }

    static class PartAttachment implements AttachmentEx {

        final MIMEPart part;
        byte[] buf;
        private StreamingDataHandler streamingDataHandler;

        PartAttachment(MIMEPart part) {
            this.part = part;
        }

        public @NotNull @Override String getContentId() {
            return part.getContentId();
        }

        public @NotNull @Override String getContentType() {
            return part.getContentType();
        }

        @Override
        public byte[] asByteArray() {
            if (buf == null) {
                ByteArrayBuffer baf = new ByteArrayBuffer();
                try {
                    baf.write(part.readOnce());
                } catch(IOException ioe) {
                    throw new WebServiceException(ioe);
                } finally {
                    if (baf != null) {
                        try {
                            baf.close();
                        } catch (IOException ex) {
                            Logger.getLogger(MimeMultipartParser.class.getName()).log(Level.FINE, null, ex);
                        }
                    }
                }
                buf = baf.toByteArray();
            }
            return buf;
        }
        
        @Override
        public DataHandler asDataHandler() {
            if (streamingDataHandler == null) {
                streamingDataHandler = (buf != null)
                    ? new DataSourceStreamingDataHandler(new ByteArrayDataSource(buf,getContentType()))
                    : new MIMEPartStreamingDataHandler(part);
            }
            return streamingDataHandler;                
        }

        @Override
        public Source asSource() {
            return (buf != null)
                ? new StreamSource(new ByteArrayInputStream(buf))
                : new StreamSource(part.read());
        }

        @Override
        public InputStream asInputStream() {
            return (buf != null)
                ? new ByteArrayInputStream(buf) : part.read();
        }

        @Override
        public void writeTo(OutputStream os) throws IOException {
            if (buf != null) {
                os.write(buf);
            } else {
                InputStream in = part.read();
                byte[] temp = new byte[8192];
                int len;
                while((len=in.read(temp)) != -1) {
                    os.write(temp, 0, len);
                }
                in.close();
            }
        }

        @Override
        public void writeTo(SOAPMessage saaj) throws SOAPException {
            saaj.createAttachmentPart().setDataHandler(asDataHandler());
        }

        // AttachmentEx methods begin here
        @Override
        public Iterator<MimeHeader> getMimeHeaders() {
            final Iterator<? extends Header> ih = part.getAllHeaders()
                    .iterator();
            return new Iterator<MimeHeader>() {
                @Override
                public boolean hasNext() {
                    return ih.hasNext();
                }

                @Override
                public MimeHeader next() {
                    final Header hdr = ih.next();
                    return new AttachmentEx.MimeHeader() {
                        @Override
                        public String getValue() {
                            return hdr.getValue();
                        }
                        @Override
                        public String getName() {
                            return hdr.getName();
                        }
                    };
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public ContentTypeImpl getContentType() {
        return contentType;
    }

}
