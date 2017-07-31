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

package com.sun.xml.ws.encoding.xml;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.encoding.ContentTypeImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public final class XMLCodec implements Codec {
    public static final String XML_APPLICATION_MIME_TYPE = "application/xml";

    public static final String XML_TEXT_MIME_TYPE = "text/xml";

    private static final ContentType contentType = new ContentTypeImpl(XML_TEXT_MIME_TYPE);

//  private final WSBinding binding;
    private WSFeatureList features;
    
    public XMLCodec(WSFeatureList f) {
//        this.binding = binding;
        features = f;
    }

    public String getMimeType() {
        return XML_APPLICATION_MIME_TYPE;
    }

    public ContentType getStaticContentType(Packet packet) {
        return contentType;
    }

    public ContentType encode(Packet packet, OutputStream out) {
		String encoding = (String) packet.invocationProperties
                .get(XMLConstants.OUTPUT_XML_CHARACTER_ENCODING);
				
        XMLStreamWriter writer = null;
		
		if (encoding != null && encoding.length() > 0) {
            writer = XMLStreamWriterFactory.create(out, encoding);
        } else {
            writer = XMLStreamWriterFactory.create(out);
        }
		
        try {
            if (packet.getMessage().hasPayload()){
            	writer.writeStartDocument();
                packet.getMessage().writePayloadTo(writer);              
                writer.flush();
            }
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
        return contentType;
    }

    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        //TODO: not yet implemented
        throw new UnsupportedOperationException();
    }

    public Codec copy() {
        return this;
    }

    public void decode(InputStream in, String contentType, Packet packet) throws IOException {
        Message message = XMLMessage.create(contentType, in, features);
        packet.setMessage(message);
    }

    public void decode(ReadableByteChannel in, String contentType, Packet packet) {
        // TODO
        throw new UnsupportedOperationException();
    }    
}
