/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.encoding.fastinfoset;

import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.encoding.ContentTypeImpl;
import java.io.BufferedInputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.ReadableByteChannel;
import org.jvnet.fastinfoset.FastInfosetSource;

/**
 * A codec for encoding/decoding XML infosets to/from fast
 * infoset documents.
 *
 * @author Paul Sandoz
 */
public class FastInfosetCodec implements Codec {
    private static final int DEFAULT_INDEXED_STRING_SIZE_LIMIT = 32;
    private static final int DEFAULT_INDEXED_STRING_MEMORY_LIMIT = 4 * 1024 * 1024; //4M limit
    
    private StAXDocumentParser _parser;
    
    private StAXDocumentSerializer _serializer;
    
    private final boolean _retainState;
    
    private final ContentType _contentType;
    
    /* package */ FastInfosetCodec(boolean retainState) {
        _retainState = retainState;
        _contentType = (retainState) ? new ContentTypeImpl(FastInfosetMIMETypes.STATEFUL_INFOSET) :
            new ContentTypeImpl(FastInfosetMIMETypes.INFOSET);
    }
    
    public String getMimeType() {
        return _contentType.getContentType();
    }
    
    public Codec copy() {
        return new FastInfosetCodec(_retainState);
    }
    
    public ContentType getStaticContentType(Packet packet) {
        return _contentType;
    }
    
    public ContentType encode(Packet packet, OutputStream out) {
        Message message = packet.getMessage();
        if (message != null && message.hasPayload()) {
            final XMLStreamWriter writer = getXMLStreamWriter(out);
            try {
                writer.writeStartDocument();
                packet.getMessage().writePayloadTo(writer);
                writer.writeEndDocument();
                writer.flush();
            } catch (XMLStreamException e) {
                throw new WebServiceException(e);
            }
        }
        
        return _contentType;
    }
    
    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        //TODO: not yet implemented
        throw new UnsupportedOperationException();
    }
    
    public void decode(InputStream in, int contentLength, String contentType, Packet packet) throws IOException {
        decode(in, contentType, packet);
    }
    
    public void decode(InputStream in, String contentType, Packet packet) throws IOException {
        /* Implements similar logic as the XMLMessage.create(String, InputStream).
         * But it's faster, as we know the InputStream has FastInfoset content*/
        Message message;
        in = hasSomeData(in);
        if (in != null) {
            message = Messages.createUsingPayload(new FastInfosetSource(in),
                    SOAPVersion.SOAP_11);
        } else {
            message = Messages.createEmpty(SOAPVersion.SOAP_11);
        }
        
        packet.setMessage(message);
    }
    
    public void decode(ReadableByteChannel in, String contentType, Packet response) {
        throw new UnsupportedOperationException();
    }
    
    private XMLStreamWriter getXMLStreamWriter(OutputStream out) {
        if (_serializer != null) {
            _serializer.setOutputStream(out);
            return _serializer;
        } else {
            return _serializer = createNewStreamWriter(out, _retainState);
        }
    }
        
    /**
     * Creates a new {@link FastInfosetCodec} instance.
     *
     * @return a new {@link FastInfosetCodec} instance.
     */
    public static FastInfosetCodec create() {
        return create(false);
    }
    
    /**
     * Creates a new {@link FastInfosetCodec} instance.
     *
     * @param retainState if true the Codec should retain the state of
     *        vocabulary tables for multiple encode/decode invocations.
     * @return a new {@link FastInfosetCodec} instance.
     */
    public static FastInfosetCodec create(boolean retainState) {
        return new FastInfosetCodec(retainState);
    }
    
    /**
     * Create a new (@link StAXDocumentSerializer} instance.
     *
     * @param in the OutputStream to serialize to.
     * @param retainState if true the serializer should retain the state of
     *        vocabulary tables for multiple serializations.
     * @return a new {@link StAXDocumentSerializer} instance.
     */
    /* package */ static StAXDocumentSerializer createNewStreamWriter(OutputStream out, boolean retainState) {
        return createNewStreamWriter(out, retainState, DEFAULT_INDEXED_STRING_SIZE_LIMIT, DEFAULT_INDEXED_STRING_MEMORY_LIMIT);
    }
    
    /**
     * Create a new (@link StAXDocumentSerializer} instance.
     *
     * @param in the OutputStream to serialize to.
     * @param retainState if true the serializer should retain the state of
     *        vocabulary tables for multiple serializations.
     * @return a new {@link StAXDocumentSerializer} instance.
     */
    /* package */ static StAXDocumentSerializer createNewStreamWriter(OutputStream out,
            boolean retainState, int indexedStringSizeLimit, int stringsMemoryLimit) {
        StAXDocumentSerializer serializer = new StAXDocumentSerializer(out);
        if (retainState) {
            /**
             * Create a serializer vocabulary external to the serializer.
             * This will ensure that the vocabulary will never be cleared
             * for each serialization and will be retained (and will grow)
             * for each serialization
             */
            SerializerVocabulary vocabulary = new SerializerVocabulary();
            serializer.setVocabulary(vocabulary);
            serializer.setMinAttributeValueSize(0);
            serializer.setMaxAttributeValueSize(indexedStringSizeLimit);
            serializer.setMinCharacterContentChunkSize(0);
            serializer.setMaxCharacterContentChunkSize(indexedStringSizeLimit);
            serializer.setAttributeValueMapMemoryLimit(stringsMemoryLimit);
            serializer.setCharacterContentChunkMapMemoryLimit(stringsMemoryLimit);
        }
        return serializer;
    }
    
    /**
     * Create a new (@link StAXDocumentParser} instance.
     *
     * @param in the InputStream to parse from.
     * @param retainState if true the parser should retain the state of
     *        vocabulary tables for multiple parses.
     * @return a new {@link StAXDocumentParser} instance.
     */
    /* package */ static StAXDocumentParser createNewStreamReader(InputStream in, boolean retainState) {
        StAXDocumentParser parser = new StAXDocumentParser(in);
        parser.setStringInterning(true);
        if (retainState) {
            /**
             * Create a parser vocabulary external to the parser.
             * This will ensure that the vocabulary will never be cleared
             * for each parse and will be retained (and will grow)
             * for each parse.
             */
            ParserVocabulary vocabulary = new ParserVocabulary();
            parser.setVocabulary(vocabulary);
        }
        return parser;
    }
    
    /**
     * Create a new (@link StAXDocumentParser} recyclable instance.
     *
     * @param in the InputStream to parse from.
     * @param retainState if true the parser should retain the state of
     *        vocabulary tables for multiple parses.
     * @return a new recyclable {@link StAXDocumentParser} instance.
     */
    /* package */ static StAXDocumentParser createNewStreamReaderRecyclable(InputStream in, boolean retainState) {
        StAXDocumentParser parser = new FastInfosetStreamReaderRecyclable(in);
        parser.setStringInterning(true);
        parser.setForceStreamClose(true);
        if (retainState) {
            /**
             * Create a parser vocabulary external to the parser.
             * This will ensure that the vocabulary will never be cleared
             * for each parse and will be retained (and will grow)
             * for each parse.
             */
            ParserVocabulary vocabulary = new ParserVocabulary();
            parser.setVocabulary(vocabulary);
        }
        return parser;
    }

    /**
     * Method is copied from com.sun.xml.ws.encoding.xml.XMLMessage
     * @TODO method should be public in some util package?
     *
     * Finds if the stream has some content or not
     *
     * @return null if there is no data
     *         else stream to be used
     */
    private static InputStream hasSomeData(InputStream in) throws IOException {
        if (in != null) {
            if (in.available() < 1) {
                if (!in.markSupported()) {
                    in = new BufferedInputStream(in);
                }
                in.mark(1);
                if (in.read() != -1) {
                    in.reset();
                } else {
                    in = null;          // No data
                }
            }
        }
        return in;
    }
}
