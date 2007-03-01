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

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.message.stream.StreamHeader;
import com.sun.xml.ws.message.stream.StreamHeader11;

import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.List;

/**
 * {@link StreamSOAPCodec} for SOAP 1.1.
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class StreamSOAP11Codec extends StreamSOAPCodec {
    public static final String SOAP11_MIME_TYPE = "text/xml";
    public static final String SOAP11_CONTENT_TYPE = SOAP11_MIME_TYPE+"; charset=utf-8";

    private static final List<String> expectedContentTypes = Collections.singletonList(SOAP11_MIME_TYPE);

    /*package*/  StreamSOAP11Codec() {
        super(SOAPVersion.SOAP_11);
    }

    public String getMimeType() {
        return SOAP11_MIME_TYPE;
    }
    
    @Override
    protected final StreamHeader createHeader(XMLStreamReader reader, XMLStreamBuffer mark) {
        return new StreamHeader11(reader, mark);
    }

    /** BP 1.1 R1109 requires SOAPAction too be a quoted value **/
    public static final ContentTypeImpl defaultContentType =
            new ContentTypeImpl(SOAP11_CONTENT_TYPE, "\"\"");

    @Override
    protected ContentType getContentType(String soapAction) {
        // TODO: set Accept header

        if (soapAction == null || soapAction.length() == 0) {
            return defaultContentType;
        } else {
            //surround soapAction by double quotes for BP R1109
            if(soapAction.charAt(0) != '"' && soapAction.charAt(soapAction.length() -1) != '"')
                soapAction = "\"" + soapAction + "\"";
            return new ContentTypeImpl(SOAP11_CONTENT_TYPE, soapAction);
        }
    }

    protected List<String> getExpectedContentTypes() {
        return expectedContentTypes;
    }
}