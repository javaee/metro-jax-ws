/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.developer.SerializationFeature;
import com.sun.xml.ws.message.MimeAttachmentSet;
import com.sun.xml.ws.streaming.XMLStreamWriterUtil;
import com.sun.xml.ws.util.ByteArrayDataSource;
import com.sun.xml.ws.util.xml.XMLStreamReaderFilter;
import com.sun.xml.ws.util.xml.XMLStreamWriterFilter;
import com.sun.xml.ws.streaming.MtomStreamWriter;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.server.UnsupportedMediaException;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamReaderEx;
import org.jvnet.staxex.XMLStreamWriterEx;

import javax.activation.DataHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.bind.attachment.AttachmentMarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mtom messge Codec. It can be used even for non-soap message's mtom encoding.
 *
 * @author Vivek Pandey
 * @author Jitendra Kotamraju
 */
public class MtomCodec extends MimeCodec {

    public static final String XOP_XML_MIME_TYPE = "application/xop+xml";
    private static final String XOP_LOCALNAME = "Include";
    private static final String XOP_NAMESPACEURI = "http://www.w3.org/2004/08/xop/include";

    private final StreamSOAPCodec codec;

    // encoding related parameters
    private String boundary;
    private String rootId;
    private final MTOMFeature mtomFeature;
    private final SerializationFeature sf;
    private final static String DECODED_MESSAGE_CHARSET = "decodedMessageCharset";

    MtomCodec(SOAPVersion version, StreamSOAPCodec codec, WSBinding binding, WebServiceFeature mtomFeature){
        super(version, binding);
        this.codec = codec;
        createConteTypeHeader();
        sf = binding.getFeature(SerializationFeature.class);

        if(mtomFeature == null)
            this.mtomFeature = new MTOMFeature();
        else
            this.mtomFeature = (MTOMFeature) mtomFeature;
    }

    private void createConteTypeHeader(){
        String uuid = UUID.randomUUID().toString();
        boundary = "uuid:" + uuid;
        rootId = "<rootpart*"+uuid+"@example.jaxws.sun.com>";
    }

    private String createMessageContentType() {
        return createMessageContentType(null);
    }

    private String createMessageContentType(String soapActionParameter) {
        String boundaryParameter = "boundary=\"" + boundary +"\"";
        return MULTIPART_RELATED_MIME_TYPE +
                ";start=\""+rootId +"\"" +
                ";type=\"" + XOP_XML_MIME_TYPE + "\";" +
                boundaryParameter +
                ";start-info=\"" + version.contentType +
                (soapActionParameter == null? "" : soapActionParameter) +
                "\"";
    }

    
    /**
     * Return the soap 1.1 and soap 1.2 specific XOP packaged ContentType
     *
     * @return A non-null content type for soap11 or soap 1.2 content type
     */
    public ContentType getStaticContentType(Packet packet) {
        return getContentType(packet);
    }

    private ContentType getContentType(Packet packet){
        switch(version){
            case SOAP_11:
                return new ContentTypeImpl(createMessageContentType(), (packet.soapAction == null)?"":packet.soapAction, null);
            case SOAP_12:
                return new ContentTypeImpl(createMessageContentType(createActionParameter(packet)), null, null);
        }
        //never happens
        return null;
    }

    private String createActionParameter(Packet packet) {
        return packet.soapAction != null? ";action=\\\""+packet.soapAction+"\\\"" : "";
    }

    public ContentType encode(Packet packet, OutputStream out) throws IOException {
        //get the current boundary thaat will be reaturned from this method
        ContentType contentType = getContentType(packet);

        if(packet.getMessage() != null){
            try {
                String encoding = getPacketEncoding(packet);
                packet.invocationProperties.remove(DECODED_MESSAGE_CHARSET);

                String actionParameter = version == SOAPVersion.SOAP_11? "" : createActionParameter(packet);
                String soapXopContentType = XOP_XML_MIME_TYPE +";charset="+encoding+";type=\""+version.contentType+ actionParameter + "\"";

                writeln("--"+boundary, out);
                writeln("Content-Id: " + rootId, out);
                writeln("Content-Type: "+ soapXopContentType,  out);
                writeln("Content-Transfer-Encoding: binary", out);
                writeln(out);

                //mtom attachments that need to be written after the root part
                List<ByteArrayBuffer> mtomAttachments = new ArrayList<ByteArrayBuffer>();
                MtomStreamWriterImpl writer = new MtomStreamWriterImpl(
                        XMLStreamWriterFactory.create(out, encoding), mtomAttachments);

                packet.getMessage().writeTo(writer);
                XMLStreamWriterFactory.recycle(writer);
                writeln(out);

                for(ByteArrayBuffer bos : mtomAttachments){
                    bos.write(out);
                }

                //now write out the attachments in the message
                writeAttachments(packet.getMessage().getAttachments(),out);

                //write out the end boundary
                writeAsAscii("--"+boundary, out);
                writeAsAscii("--", out);

            } catch (XMLStreamException e) {
                throw new WebServiceException(e);
            }
        }
        //now create the boundary for next encode() call
        createConteTypeHeader();
        return contentType;
    }

    private class ByteArrayBuffer{
        final String contentId;

        private DataHandler dh;

        ByteArrayBuffer(@NotNull DataHandler dh) {
            this.dh = dh;
            this.contentId = encodeCid();
        }

        void write(OutputStream os) throws IOException {
            //build attachment frame
            writeln("--"+boundary, os);
            writeMimeHeaders(dh.getContentType(), contentId, os);
            dh.writeTo(os);
            writeln(os);
        }
    }

    private void writeMimeHeaders(String contentType, String contentId, OutputStream out) throws IOException {
        String cid = contentId;
        if(cid != null && cid.length() >0 && cid.charAt(0) != '<')
            cid = '<' + cid + '>';
        writeln("Content-Id: " + cid, out);
        writeln("Content-Type: " + contentType, out);
        writeln("Content-Transfer-Encoding: binary", out);
        writeln(out);
    }

    private void writeAttachments(AttachmentSet attachments, OutputStream out) throws IOException {
        for(Attachment att : attachments){
            //build attachment frame
            writeln("--"+boundary, out);
            writeMimeHeaders(att.getContentType(), att.getContentId(), out);
            att.writeTo(out);
            writeln(out);                    // write \r\n
        }
    }

    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        throw new UnsupportedOperationException();
    }

    public MtomCodec copy() {
        return new MtomCodec(version, (StreamSOAPCodec)codec.copy(), binding, mtomFeature);
    }

    private String encodeCid(){
        String cid="example.jaxws.sun.com";
        String name = UUID.randomUUID()+"@";
        return name + cid;
    }

    @Override
    protected void decode(MimeMultipartParser mpp, Packet packet) throws IOException {
        //TODO shouldn't we check for SOAP1.1/SOAP1.2 and throw
        //TODO UnsupportedMediaException like StreamSOAPCodec
        String charset = null;
        String ct = mpp.getRootPart().getContentType();
        if (ct != null) {
            charset = new ContentTypeImpl(ct).getCharSet();
        }
        if (charset != null && !Charset.isSupported(charset)) {
            throw new UnsupportedMediaException(charset);
        }

        if (charset != null) {
            packet.invocationProperties.put(DECODED_MESSAGE_CHARSET, charset);
        } else {
            packet.invocationProperties.remove(DECODED_MESSAGE_CHARSET);
        }

        // we'd like to reuse those reader objects but unfortunately decoder may be reused
        // before the decoded message is completely used.
        XMLStreamReader mtomReader = new MtomXMLStreamReaderEx( mpp,
            XMLStreamReaderFactory.create(null, mpp.getRootPart().asInputStream(), charset, true)
        );

        packet.setMessage(codec.decode(mtomReader, new MimeAttachmentSet(mpp)));

    }

    private String getPacketEncoding(Packet packet) {
        // If SerializationFeature is set, just use that encoding
        if (sf != null && sf.getEncoding() != null) {
            return sf.getEncoding().equals("") ? SOAPBindingCodec.DEFAULT_ENCODING : sf.getEncoding();
        }

        if (packet != null && packet.endpoint != null) {
            // Use request message's encoding for Server-side response messages
            String charset = (String)packet.invocationProperties.get(DECODED_MESSAGE_CHARSET);
            return charset == null
                    ? SOAPBindingCodec.DEFAULT_ENCODING : charset;
        } 
        
        // Use default encoding for client-side request messages
        return SOAPBindingCodec.DEFAULT_ENCODING;
    }

    private class MtomStreamWriterImpl extends XMLStreamWriterFilter implements XMLStreamWriterEx,
            MtomStreamWriter, HasEncoding {
        private final List<ByteArrayBuffer> mtomAttachments;

        public MtomStreamWriterImpl(XMLStreamWriter w, List<ByteArrayBuffer> mtomAttachments) {
            super(w);
            this.mtomAttachments = mtomAttachments;
        }

        public void writeBinary(byte[] data, int start, int len, String contentType) throws XMLStreamException {
            //check threshold and if less write as base64encoded value
            if(mtomFeature.getThreshold() > len){
                writeCharacters(DatatypeConverterImpl._printBase64Binary(data, start, len));
                return;
            }
            ByteArrayBuffer bab = new ByteArrayBuffer(new DataHandler(new ByteArrayDataSource(data, start, len, contentType)));
            writeBinary(bab);
        }

        public void writeBinary(DataHandler dataHandler) throws XMLStreamException {
            // TODO how do we check threshold and if less inline the data
            writeBinary(new ByteArrayBuffer(dataHandler));
        }

        public OutputStream writeBinary(String contentType) throws XMLStreamException {
            throw new UnsupportedOperationException();
        }

        public void writePCDATA(CharSequence data) throws XMLStreamException {
            if(data == null)
                return;
            if(data instanceof Base64Data){
                Base64Data binaryData = (Base64Data)data;
                writeBinary(binaryData.getDataHandler());
                return;
            }
            writeCharacters(data.toString());
        }

        private void writeBinary(ByteArrayBuffer bab) {
            try {
                mtomAttachments.add(bab);
                writer.setPrefix("xop", XOP_NAMESPACEURI);
                writer.writeNamespace("xop", XOP_NAMESPACEURI);
                writer.writeStartElement(XOP_NAMESPACEURI, XOP_LOCALNAME);
                writer.writeAttribute("href", "cid:"+bab.contentId);
                writer.writeEndElement();
                writer.flush();
            } catch (XMLStreamException e) {
                throw new WebServiceException(e);
            }
        }

        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            // Hack for JDK6's SJSXP
            if (name.equals("sjsxp-outputstream") && writer instanceof Map) {
                Object obj = ((Map) writer).get("sjsxp-outputstream");
                if (obj != null) {
                    return obj;
                }
            }
            return super.getProperty(name);
        }

        /**
         * JAXBMessage writes envelope directly to the OutputStream(for SJSXP, woodstox).
         * While writing, it calls the AttachmentMarshaller methods for adding attachments.
         * JAXB writes xop:Include in this case.
         */
        public AttachmentMarshaller getAttachmentMarshaller() {
            return new AttachmentMarshaller() {

                public String addMtomAttachment(DataHandler data, String elementNamespace, String elementLocalName) {
                    // Should we do the threshold processing on DataHandler ? But that would be
                    // expensive as DataHolder need to read the data again from its source
                    ByteArrayBuffer bab = new ByteArrayBuffer(data);
                    mtomAttachments.add(bab);
                    return "cid:"+bab.contentId;
                }

                public String addMtomAttachment(byte[] data, int offset, int length, String mimeType, String elementNamespace, String elementLocalName) {
                    // inline the data based on the threshold
                    if (mtomFeature.getThreshold() > length) {
                        return null;                // JAXB inlines the attachment data
                    }
                    ByteArrayBuffer bab = new ByteArrayBuffer(new DataHandler(new ByteArrayDataSource(data, offset, length, mimeType)));
                    mtomAttachments.add(bab);
                    return "cid:"+bab.contentId;
                }

                public String addSwaRefAttachment(DataHandler data) {
                    ByteArrayBuffer bab = new ByteArrayBuffer(data);
                    mtomAttachments.add(bab);
                    return "cid:"+bab.contentId;
                }

                @Override
                public boolean isXOPPackage() {
                    return true;
                }
            };
        }

        public String getEncoding() {
            return XMLStreamWriterUtil.getEncoding(writer);
        }

        private class MtomNamespaceContextEx implements NamespaceContextEx {
            private NamespaceContext nsContext;

            public MtomNamespaceContextEx(NamespaceContext nsContext) {
                this.nsContext = nsContext;
            }

            public Iterator<Binding> iterator() {
                throw new UnsupportedOperationException();
            }

            public String getNamespaceURI(String prefix) {
                return nsContext.getNamespaceURI(prefix);
            }

            public String getPrefix(String namespaceURI) {
                return nsContext.getPrefix(namespaceURI);
            }

            public Iterator getPrefixes(String namespaceURI) {
                return nsContext.getPrefixes(namespaceURI);
            }
        }

        @Override
        public NamespaceContextEx getNamespaceContext() {
            NamespaceContext nsContext = writer.getNamespaceContext();
            return new MtomNamespaceContextEx(nsContext);
        }
    }

    private static class MtomXMLStreamReaderEx extends XMLStreamReaderFilter implements XMLStreamReaderEx {
        /**
         * The parser for the outer MIME 'shell'.
         */
        private final MimeMultipartParser mimeMP;

        private boolean xopReferencePresent = false;
        private Base64Data base64AttData;

        //To be used with #getTextCharacters
        private char[] base64EncodedText;

        public MtomXMLStreamReaderEx(MimeMultipartParser mimeMP, XMLStreamReader reader) {
            super(reader);
            this.mimeMP = mimeMP;
        }

        public CharSequence getPCDATA() throws XMLStreamException {
            if(xopReferencePresent){
                return base64AttData;
            }
            return reader.getText();
        }

        public NamespaceContextEx getNamespaceContext() {
            NamespaceContext nsContext = reader.getNamespaceContext();
            return new MtomNamespaceContextEx(nsContext);
        }

        public String getElementTextTrim() throws XMLStreamException {
            throw new UnsupportedOperationException();
        }

        private static class MtomNamespaceContextEx implements NamespaceContextEx {
            private NamespaceContext nsContext;

            public MtomNamespaceContextEx(NamespaceContext nsContext) {
                this.nsContext = nsContext;
            }

            public Iterator<Binding> iterator() {
                throw new UnsupportedOperationException();
            }

            public String getNamespaceURI(String prefix) {
                return nsContext.getNamespaceURI(prefix);
            }

            public String getPrefix(String namespaceURI) {
                return nsContext.getPrefix(namespaceURI);
            }

            public Iterator getPrefixes(String namespaceURI) {
                return nsContext.getPrefixes(namespaceURI);
            }

        }

        public int getTextLength() {
            if (xopReferencePresent) {
                return base64AttData.length();
            }
            return reader.getTextLength();
        }

        public int getTextStart() {
            if (xopReferencePresent) {
                return 0;
            }
            return reader.getTextStart();
        }

        public int getEventType() {
            if(xopReferencePresent)
                return XMLStreamConstants.CHARACTERS;
            return super.getEventType();
        }

        public int next() throws XMLStreamException {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals(XOP_LOCALNAME) && reader.getNamespaceURI().equals(XOP_NAMESPACEURI)) {
                //its xop reference, take the URI reference
                String href = reader.getAttributeValue(null, "href");
                try {
                    Attachment att = getAttachment(href);
                    if(att != null){
                        base64AttData = new Base64Data();
                        base64AttData.set(att.asDataHandler());
                    }
                    xopReferencePresent = true;
                } catch (IOException e) {
                    throw new WebServiceException(e);
                }
                //move to the </xop:Include>
                XMLStreamReaderUtil.nextElementContent(reader);
                return XMLStreamConstants.CHARACTERS;
            }
            if(xopReferencePresent){
                xopReferencePresent = false;
                base64EncodedText = null;
            }
            return event;
        }

        private String decodeCid(String cid) {
            try {
                cid = URLDecoder.decode(cid, "utf-8");
            } catch (UnsupportedEncodingException e) {
                //on recceiving side lets not fail now, try to look for it
            }
            return cid;
        }

        private Attachment getAttachment(String cid) throws IOException {
            if (cid.startsWith("cid:"))
                cid = cid.substring(4, cid.length());
            if (cid.indexOf('%') != -1) {
                cid = decodeCid(cid);
                return mimeMP.getAttachmentPart(cid);
            }
            return mimeMP.getAttachmentPart(cid);
        }

        public char[] getTextCharacters() {
            if (xopReferencePresent) {
                char[] chars = new char[base64AttData.length()];
                base64AttData.writeTo(chars, 0);
                return chars;
            }
            return reader.getTextCharacters();
        }

        public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
            if(xopReferencePresent){
                if(target == null){
                    throw new NullPointerException("target char array can't be null") ;
                }

                if(targetStart < 0 || length < 0 || sourceStart < 0 || targetStart >= target.length ||
                        (targetStart + length ) > target.length) {
                    throw new IndexOutOfBoundsException();
                }

                int textLength = base64AttData.length();
                if(sourceStart > textLength)
                    throw new IndexOutOfBoundsException();

                if(base64EncodedText == null){
                    base64EncodedText = new char[base64AttData.length()];
                    base64AttData.writeTo(base64EncodedText, 0);
                }

                int copiedLength = Math.min(textLength - sourceStart, length);
                System.arraycopy(base64EncodedText, sourceStart , target, targetStart, copiedLength);
                return copiedLength;
            }
            return reader.getTextCharacters(sourceStart, target, targetStart, length);
        }

        public String getText() {
            if (xopReferencePresent) {
                return base64AttData.toString();
            }
            return reader.getText();
        }
    }

}
