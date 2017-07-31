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

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.client.SelectOptimalEncodingFeature;
import com.sun.xml.ws.api.fastinfoset.FastInfosetFeature;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.ExceptionHasMessage;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.client.ContentNegotiation;
import com.sun.xml.ws.protocol.soap.MessageCreationException;
import com.sun.xml.ws.resources.StreamingMessages;
import com.sun.xml.ws.server.UnsupportedMediaException;
import static com.sun.xml.ws.binding.WebServiceFeatureList.getSoapVersion;   

import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
//import java.util.StringTokenizer;

/**
 * SOAP binding {@link Codec} that can handle MTOM, SwA, and SOAP messages
 * encoded using XML or Fast Infoset.
 *
 * <p>
 * This is used when we need to determine the encoding from what we received (for decoding)
 * and from configuration and {@link Message} contents (for encoding)
 *
 * <p>
 * TODO: Split this Codec into two, one that supports FI and one that does not.
 * Then further split the FI Codec into two, one for client and one for
 * server. This will simplify the logic and make it easier to understand/maintain.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class SOAPBindingCodec extends MimeCodec implements com.sun.xml.ws.api.pipe.SOAPBindingCodec {

    public static final String UTF8_ENCODING = "utf-8";
    public static final String DEFAULT_ENCODING = UTF8_ENCODING;


    /**
     * True if Fast Infoset functionality has been
     * configured to be disabled, or the Fast Infoset
     * runtime is not available.
     */
    private boolean isFastInfosetDisabled;
    
    /**
     * True if the Fast Infoset codec should be used for encoding.
     */
    private boolean useFastInfosetForEncoding;
    
    /**
     * True if the content negotiation property should
     * be ignored by the client. This will be used in
     * the case of Fast Infoset being configured to be
     * disabled or automatically selected.
     */
    private boolean ignoreContentNegotiationProperty;
    
    // The XML SOAP codec
    private final StreamSOAPCodec xmlSoapCodec;
    
    // The Fast Infoset SOAP codec
    private final Codec fiSoapCodec;
    
    // The XML MTOM codec
    private final MimeCodec xmlMtomCodec;
    
    // The XML SWA codec
    private final MimeCodec xmlSwaCodec;
    
    // The Fast Infoset SWA codec
    private final MimeCodec fiSwaCodec;
        
    /**
     * The XML SOAP MIME type
     */
    private final String xmlMimeType;
    
    /**
     * The Fast Infoset SOAP MIME type
     */
    private final String fiMimeType;
    
    /**
     * The Accept header for XML encodings
     */
    private final String xmlAccept;
    
    /**
     * The Accept header for Fast Infoset and XML encodings
     */
    private final String connegXmlAccept;
    
    public StreamSOAPCodec getXMLCodec() {
        return xmlSoapCodec;
    }
  
    private ContentTypeImpl setAcceptHeader(Packet p, ContentTypeImpl c) {
        String _accept;
        if (!ignoreContentNegotiationProperty && p.contentNegotiation != ContentNegotiation.none) {
            _accept = connegXmlAccept;
        } else {
            _accept = xmlAccept;
        }
        c.setAcceptHeader(_accept);
        return c;
    }
    
    public SOAPBindingCodec(WSFeatureList features) {
        this(features, Codecs.createSOAPEnvelopeXmlCodec(features));
    }
    
    public SOAPBindingCodec(WSFeatureList features, StreamSOAPCodec xmlSoapCodec) {
        super(getSoapVersion(features), features);
        
        this.xmlSoapCodec = xmlSoapCodec;
        xmlMimeType = xmlSoapCodec.getMimeType();
        
        xmlMtomCodec = new MtomCodec(version, xmlSoapCodec, features);
        
        xmlSwaCodec = new SwACodec(version, features, xmlSoapCodec);
        
        String clientAcceptedContentTypes = xmlSoapCodec.getMimeType() + ", " +
                xmlMtomCodec.getMimeType();
        
        WebServiceFeature fi = features.get(FastInfosetFeature.class);
        isFastInfosetDisabled = (fi != null && !fi.isEnabled());
        if (!isFastInfosetDisabled) {
            fiSoapCodec = getFICodec(xmlSoapCodec, version);
            if (fiSoapCodec != null) {
                fiMimeType = fiSoapCodec.getMimeType();
                fiSwaCodec = new SwACodec(version, features, fiSoapCodec);
                connegXmlAccept = fiMimeType + ", " + clientAcceptedContentTypes;
                
                /**
                 * This feature will only be present on the client side.
                 *
                 * Fast Infoset is enabled on the client if the service
                 * explicitly supports Fast Infoset.
                 */
                WebServiceFeature select = features.get(SelectOptimalEncodingFeature.class);
                if (select != null) { // if the client FI feature is set - ignore negotiation property
                    ignoreContentNegotiationProperty = true;
                    if (select.isEnabled()) {
                        // If the client's FI encoding feature is enabled, and server's is not disabled
                        if (fi != null) {  // if server's FI feature also enabled
                            useFastInfosetForEncoding = true;
                        }
                        
                        clientAcceptedContentTypes = connegXmlAccept;
                    } else {  // If client FI feature is disabled
                        isFastInfosetDisabled = true;
                    }
                }
            } else {
                // Fast Infoset could not be loaded by the runtime
                isFastInfosetDisabled = true;
                fiSwaCodec = null;
                fiMimeType = "";
                connegXmlAccept = clientAcceptedContentTypes;
                ignoreContentNegotiationProperty = true;
            }
        } else {
            // Fast Infoset is explicitly not supported by the service
            fiSoapCodec = fiSwaCodec = null;
            fiMimeType = "";
            connegXmlAccept = clientAcceptedContentTypes;
            ignoreContentNegotiationProperty = true;
        }
        
        xmlAccept = clientAcceptedContentTypes;

        if(getSoapVersion(features) == null)
            throw new WebServiceException("Expecting a SOAP binding but found ");
    }
    
    public String getMimeType() {
        return null;
    }
    
    public ContentType getStaticContentType(Packet packet) {
        ContentType toAdapt = getEncoder(packet).getStaticContentType(packet);
        return setAcceptHeader(packet, (ContentTypeImpl)toAdapt);
    }
    
    public ContentType encode(Packet packet, OutputStream out) throws IOException {
       preEncode(packet);
       ContentType ct = getEncoder(packet).encode(packet, out);
       ct = setAcceptHeader(packet, (ContentTypeImpl)ct);
       postEncode();
       return ct;
    }
    
    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        preEncode(packet);
        ContentType ct = getEncoder(packet).encode(packet, buffer);
        ct = setAcceptHeader(packet, (ContentTypeImpl)ct);
        postEncode();
        return ct;
    }

    /**
     * Should be called before encode().
     * Set the state so that such state is used by encode process.
     */
    private void preEncode(Packet p) {
    }

    /**
     * Should be called after encode()
     * Reset the encoding state.
     */
    private void postEncode() {
    }

    /**
     * Should be called before decode().
     * Set the state so that such state is used by decode().
     */
    private void preDecode(Packet p) {
        if (p.contentNegotiation == null)
            useFastInfosetForEncoding = false;
    }

    /**
     * Should be called after decode().
     * Set the state so that such state is used by encode().
     */
    private void postDecode(Packet p) {
        p.setFastInfosetDisabled(isFastInfosetDisabled);
        if(features.isEnabled(MTOMFeature.class)) p.checkMtomAcceptable();
//            p.setMtomAcceptable( isMtomAcceptable(p.acceptableMimeTypes) );
        MTOMFeature mtomFeature = features.get(MTOMFeature.class);
        if (mtomFeature != null) {
            p.setMtomFeature(mtomFeature);
        }
        if (!useFastInfosetForEncoding) {
            useFastInfosetForEncoding = p.getFastInfosetAcceptable(fiMimeType);
//          useFastInfosetForEncoding = isFastInfosetAcceptable(p.acceptableMimeTypes);
        }
    }

    public void decode(InputStream in, String contentType, Packet packet) throws IOException {
        if (contentType == null) {
            contentType = xmlMimeType;
        }
        packet.setContentType(new ContentTypeImpl(contentType));
        preDecode(packet);
        try {
            if(isMultipartRelated(contentType))
                // parse the multipart portion and then decide whether it's MTOM or SwA
                super.decode(in, contentType, packet);
            else if(isFastInfoset(contentType)) {
                if (!ignoreContentNegotiationProperty && packet.contentNegotiation == ContentNegotiation.none)
                    throw noFastInfosetForDecoding();

                useFastInfosetForEncoding = true;
                fiSoapCodec.decode(in, contentType, packet);
            } else
                xmlSoapCodec.decode(in, contentType, packet);
        } catch(RuntimeException we) {
            if (we instanceof ExceptionHasMessage || we instanceof UnsupportedMediaException) {
                throw we;
            } else {
                throw new MessageCreationException(version, we);
            }
        }
        postDecode(packet);
    }

    public void decode(ReadableByteChannel in, String contentType, Packet packet) {
        if (contentType == null) {
            throw new UnsupportedMediaException();
        }

        preDecode(packet);
        try {
            if(isMultipartRelated(contentType))
                super.decode(in, contentType, packet);
            else if(isFastInfoset(contentType)) {
                if (packet.contentNegotiation == ContentNegotiation.none)
                    throw noFastInfosetForDecoding();

                useFastInfosetForEncoding = true;
                fiSoapCodec.decode(in, contentType, packet);
            } else
                xmlSoapCodec.decode(in, contentType, packet);
        } catch(RuntimeException we) {
            if (we instanceof ExceptionHasMessage || we instanceof UnsupportedMediaException) {
                throw we;
            } else {
                throw new MessageCreationException(version, we);
            }
        }
        postDecode(packet);
    }

    public SOAPBindingCodec copy() {
        return new SOAPBindingCodec(features, (StreamSOAPCodec)xmlSoapCodec.copy());
    }
    
    @Override
    protected void decode(MimeMultipartParser mpp, Packet packet) throws IOException {
        // is this SwA or XOP?
        final String rootContentType = mpp.getRootPart().getContentType();
        boolean isMTOM = isApplicationXopXml(rootContentType);
        packet.setMtomRequest(isMTOM);
        if(isMTOM) {
            xmlMtomCodec.decode(mpp,packet);
        } else if (isFastInfoset(rootContentType)) {
            if (packet.contentNegotiation == ContentNegotiation.none)
                throw noFastInfosetForDecoding();
            
            useFastInfosetForEncoding = true;
            fiSwaCodec.decode(mpp,packet);
        } else if (isXml(rootContentType))
            xmlSwaCodec.decode(mpp,packet);
        else {
            // TODO localize exception
            throw new IOException("");
        }
//        checkDuplicateKnownHeaders(packet);
    }
    
    private boolean isMultipartRelated(String contentType) {
        return compareStrings(contentType, MimeCodec.MULTIPART_RELATED_MIME_TYPE);
    }
    
    private boolean isApplicationXopXml(String contentType) {
        return compareStrings(contentType, MtomCodec.XOP_XML_MIME_TYPE);
    }
    
    private boolean isXml(String contentType) {
        return compareStrings(contentType, xmlMimeType);
    }
    
    private boolean isFastInfoset(String contentType) {
        if (isFastInfosetDisabled) return false;
        
        return compareStrings(contentType, fiMimeType);
    }
    
    private boolean compareStrings(String a, String b) {
        return a.length() >= b.length() &&
                b.equalsIgnoreCase(
                a.substring(0,
                b.length()));
    }
    
//    private boolean isFastInfosetAcceptable(String accept) {
//        if (accept == null || isFastInfosetDisabled) return false;
//        
//        StringTokenizer st = new StringTokenizer(accept, ",");
//        while (st.hasMoreTokens()) {
//            final String token = st.nextToken().trim();
//            if (token.equalsIgnoreCase(fiMimeType)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /*
     * Just check if the Accept header contains application/xop+xml,
     * no need to worry about q values.
     */
//    private boolean isMtomAcceptable(String accept) {
//        if (accept == null || isFastInfosetDisabled) return false;
//        StringTokenizer st = new StringTokenizer(accept, ",");
//        while (st.hasMoreTokens()) {
//            final String token = st.nextToken().trim();
//            if (token.toLowerCase().contains(MtomCodec.XOP_XML_MIME_TYPE)) {
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * Determines the encoding codec.
     */
    private Codec getEncoder(Packet p) {
        /**
         * The following logic is only for outbound packets
         * to be encoded by a client.
         * For a server the p.contentNegotiation == null.
         */
        if (!ignoreContentNegotiationProperty) {
            if (p.contentNegotiation == ContentNegotiation.none) {
                // The client may have changed the negotiation property from
                // pessismistic to none between invocations
                useFastInfosetForEncoding = false;
            } else if (p.contentNegotiation == ContentNegotiation.optimistic) {
                // Always encode using Fast Infoset if in optimisitic mode
                useFastInfosetForEncoding = true;
            }
        }
        
        // Override the MTOM binding for now
        // Note: Using FI with MTOM does not make sense
        if (useFastInfosetForEncoding) {
            final Message m = p.getMessage();
            if(m==null || m.getAttachments().isEmpty() || features.isEnabled(MTOMFeature.class))
                return fiSoapCodec;
            else
                return fiSwaCodec;
        }

        //If the packet does not have a binding, explicitly set the MTOMFeature
        //on the packet so that it has a way to determine whether to use MTOM
        if (p.getBinding() == null) {
            if (features != null) {
                p.setMtomFeature(features.get(MTOMFeature.class));
            }
        }
        
        if (p.shouldUseMtom()) {
            return xmlMtomCodec; 
        }

        Message m = p.getMessage();
        if(m==null || m.getAttachments().isEmpty())
            return xmlSoapCodec;
        else
            return xmlSwaCodec;
    }
    
    private RuntimeException noFastInfosetForDecoding() {
        return new RuntimeException(StreamingMessages.FASTINFOSET_DECODING_NOT_ACCEPTED());
    }
    
    /**
     * Obtain an FI SOAP codec instance using reflection.
     */
    private static Codec getFICodec(StreamSOAPCodec soapCodec, SOAPVersion version) {
        try {
            Class c = Class.forName("com.sun.xml.ws.encoding.fastinfoset.FastInfosetStreamSOAPCodec");
            Method m = c.getMethod("create", StreamSOAPCodec.class, SOAPVersion.class);
            return (Codec)m.invoke(null, soapCodec, version);
        } catch (Exception e) {
            // TODO Log that FI cannot be loaded
            return null;
        }
    }
}
