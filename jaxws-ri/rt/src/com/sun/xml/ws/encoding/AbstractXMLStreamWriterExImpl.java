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

import com.sun.istack.XMLStreamException2;
import com.sun.xml.ws.util.ByteArrayBuffer;
import org.jvnet.staxex.XMLStreamWriterEx;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Partial default implementation of {@link XMLStreamWriterEx}.
 *
 * TODO: find a good home for this class.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractXMLStreamWriterExImpl implements XMLStreamWriterEx {

    private StreamImpl stream;

    public void writeBinary(DataHandler data) throws XMLStreamException {
        try {
            StreamImpl stream = _writeBinary(data.getContentType());
            stream.write(data.getInputStream());
            stream.close();
        } catch (IOException e) {
            throw new XMLStreamException2(e);
        }
    }
    public OutputStream writeBinary(String contentType) throws XMLStreamException {
        return _writeBinary(contentType);
    }

    private StreamImpl _writeBinary(String contentType) {
        if(stream==null)
            stream = new StreamImpl();
        else
            stream.reset();
        stream.contentType = contentType;
        return stream;
    }

    private final class StreamImpl extends ByteArrayBuffer {
        private String contentType;
        public void close() throws IOException {
            super.close();
            try {
                writeBinary(buf,0,size(),contentType);
            } catch (XMLStreamException e) {
                IOException x = new IOException();
                x.initCause(e);
                throw x;
            }
        }
    }
}
