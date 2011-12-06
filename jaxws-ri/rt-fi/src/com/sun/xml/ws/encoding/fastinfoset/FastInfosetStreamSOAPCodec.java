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

package com.sun.xml.ws.encoding.fastinfoset;

import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.message.stream.StreamHeader;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.encoding.ContentTypeImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * A stream SOAP codec for handling SOAP message infosets to fast
 * infoset documents.
 *
 * <p>
 * This implementation currently defers to {@link StreamSOAPCodec} for the decoding
 * using {@link XMLStreamReader}.
 *
 * @author Paul Sandoz
 */
public abstract class FastInfosetStreamSOAPCodec implements Codec {
    private static final FastInfosetStreamReaderFactory READER_FACTORY = FastInfosetStreamReaderFactory.getInstance();

    private StAXDocumentParser _statefulParser;
    private StAXDocumentSerializer _serializer;
    
    private final StreamSOAPCodec _soapCodec;
    
    private final boolean _retainState;
    
    protected final ContentType _defaultContentType;
    
    /* package */ FastInfosetStreamSOAPCodec(StreamSOAPCodec soapCodec, SOAPVersion soapVersion, boolean retainState, String mimeType) {
//        _soapCodec = StreamSOAPCodec.create(soapVersion);
        _soapCodec = soapCodec;
        _retainState = retainState;
        _defaultContentType = new ContentTypeImpl(mimeType);
    }
    
    /* package */ FastInfosetStreamSOAPCodec(FastInfosetStreamSOAPCodec that) {
        this._soapCodec = (StreamSOAPCodec) that._soapCodec.copy();
        this._retainState = that._retainState;
        this._defaultContentType = that._defaultContentType;
    }
    
    public String getMimeType() {
        return _defaultContentType.getContentType();
    }
    
    public ContentType getStaticContentType(Packet packet) {
        return getContentType(packet.soapAction);
    }
    
    public ContentType encode(Packet packet, OutputStream out) {
        if (packet.getMessage() != null) {
            final XMLStreamWriter writer = getXMLStreamWriter(out);
            try {
                packet.getMessage().writeTo(writer);
                writer.flush();
            } catch (XMLStreamException e) {
                throw new WebServiceException(e);
            }
        }
        return getContentType(packet.soapAction);
    }
    
    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        //TODO: not yet implemented
        throw new UnsupportedOperationException();
    }
    
    public void decode(InputStream in, String contentType, Packet response) throws IOException {
        response.setMessage(
                _soapCodec.decode(getXMLStreamReader(in)));
    }
    
    public void decode(ReadableByteChannel in, String contentType, Packet response) {
        throw new UnsupportedOperationException();
    }
    
    protected abstract StreamHeader createHeader(XMLStreamReader reader, XMLStreamBuffer mark);
    
    protected abstract ContentType getContentType(String soapAction);
    
    private XMLStreamWriter getXMLStreamWriter(OutputStream out) {
        if (_serializer != null) {
            _serializer.setOutputStream(out);
            return _serializer;
        } else {
            return _serializer = FastInfosetCodec.createNewStreamWriter(out, _retainState);
        }
    }
    
    private XMLStreamReader getXMLStreamReader(InputStream in) {
        // If the _retainState is true (FI stateful) then pick up Codec assiciated XMLStreamReader
        if (_retainState) {
            if (_statefulParser != null) {
                _statefulParser.setInputStream(in);
                return _statefulParser;
            } else {
                return _statefulParser = FastInfosetCodec.createNewStreamReader(in, _retainState);
            }
        }
        
        // Otherwise thread assiciated XMLStreamReader
        return READER_FACTORY.doCreate(null, in, false);
    }
    
    /**
     * Creates a new {@link FastInfosetStreamSOAPCodec} instance.
     *
     * @param version the SOAP version of the codec.
     * @return a new {@link FastInfosetStreamSOAPCodec} instance.
     */
    public static FastInfosetStreamSOAPCodec create(StreamSOAPCodec soapCodec, SOAPVersion version) {
        return create(soapCodec, version, false);
    }
    
    /**
     * Creates a new {@link FastInfosetStreamSOAPCodec} instance.
     *
     * @param version the SOAP version of the codec.
     * @param retainState if true the Codec should retain the state of
     *        vocabulary tables for multiple encode/decode invocations.
     * @return a new {@link FastInfosetStreamSOAPCodec} instance.
     */
    public static FastInfosetStreamSOAPCodec create(StreamSOAPCodec soapCodec,
            SOAPVersion version, boolean retainState) {
        if(version==null)
            // this decoder is for SOAP, not for XML/HTTP
            throw new IllegalArgumentException();
        switch(version) {
            case SOAP_11:
                return new FastInfosetStreamSOAP11Codec(soapCodec, retainState);
            case SOAP_12:
                return new FastInfosetStreamSOAP12Codec(soapCodec, retainState);
            default:
                throw new AssertionError();
        }
    }
}
