/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
