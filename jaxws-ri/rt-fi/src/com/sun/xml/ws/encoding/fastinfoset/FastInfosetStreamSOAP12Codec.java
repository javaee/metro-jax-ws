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
package com.sun.xml.ws.encoding.fastinfoset;

import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.encoding.ContentTypeImpl;
import com.sun.xml.ws.message.stream.StreamHeader;
import com.sun.xml.ws.message.stream.StreamHeader12;
import com.sun.xml.stream.buffer.XMLStreamBuffer;

import javax.xml.stream.XMLStreamReader;

/**
 * A codec that converts SOAP 1.2 messages infosets to fast infoset
 * documents.
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class FastInfosetStreamSOAP12Codec extends FastInfosetStreamSOAPCodec {
    /*package*/ FastInfosetStreamSOAP12Codec(StreamSOAPCodec soapCodec, boolean retainState) {
        super(soapCodec, SOAPVersion.SOAP_12, retainState, 
                (retainState) ? FastInfosetMIMETypes.STATEFUL_SOAP_12 : FastInfosetMIMETypes.SOAP_12);
    }

    private FastInfosetStreamSOAP12Codec(FastInfosetStreamSOAPCodec that) {
        super(that);
    }
    
    public Codec copy() {
        return new FastInfosetStreamSOAP12Codec(this);
    }

    protected final StreamHeader createHeader(XMLStreamReader reader, XMLStreamBuffer mark) {
        return new StreamHeader12(reader, mark);
    }
    
    protected ContentType getContentType(String soapAction) {
        if (soapAction == null) {
            return _defaultContentType;
        } else {
            return new ContentTypeImpl(
                    _defaultContentType.getContentType() + ";action=\""+soapAction+"\"");
        }
    }
}
