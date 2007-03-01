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

package com.sun.xml.ws.util;

import com.sun.xml.ws.streaming.XMLReaderException;
import com.sun.xml.ws.streaming.XMLStreamReaderException;
import com.sun.xml.ws.streaming.XMLStreamWriterException;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.StringTokenizer;

public class FastInfosetUtil {
    
    public static boolean isFastInfosetAccepted(String[] accepts) {
        for (String accept : accepts) {
            if (isFastInfosetAccepted(accept)) {
                return true;
            }
        }        
        return false;
    }

    public static boolean isFastInfosetAccepted(String accept) {
        StringTokenizer st = new StringTokenizer(accept, ",");
        while (st.hasMoreTokens()) {
            final String token = st.nextToken().trim();
            if (token.equalsIgnoreCase("application/fastinfoset")) {
                return true;
            }
        }        
        return false;
    }
    
    public static String getFastInfosetFromAccept(List<String> accepts) {
        for (String accept : accepts) {
            StringTokenizer st = new StringTokenizer(accept, ",");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken().trim();
                if (token.equalsIgnoreCase("application/fastinfoset")) {
                    return "application/fastinfoset";
                }
                if (token.equalsIgnoreCase("application/soap+fastinfoset")) {
                    return "application/soap+fastinfoset";
                }
            }       
        }
        return null;        
    }
    
    public static void transcodeXMLStringToFI(String xml, OutputStream out) {
        try {
            XmlUtil.newTransformer().transform(
                new StreamSource(new java.io.StringReader(xml)),
                FastInfosetReflection.FastInfosetResult_new(out));
        }
        catch (Exception e) {
            // Ignore
        }
    }

    public static XMLStreamReader createFIStreamReader(InputSource source) {
        return createFIStreamReader(source.getByteStream());
    }

    /**
     * Returns the FI parser allocated for this thread.
     */
    public static XMLStreamReader createFIStreamReader(InputStream in) {
        // Check if compatible implementation of FI was found
        if (FastInfosetReflection.fiStAXDocumentParser_new == null) {
            throw new XMLReaderException("fastinfoset.noImplementation");
        }

        try {
            // Do not use StAX pluggable layer for FI
            Object sdp = FastInfosetReflection.fiStAXDocumentParser_new.newInstance();
            FastInfosetReflection.fiStAXDocumentParser_setStringInterning.invoke(sdp, Boolean.TRUE);
            FastInfosetReflection.fiStAXDocumentParser_setInputStream.invoke(sdp, in);
            return (XMLStreamReader) sdp;
        } catch (Exception e) {
            throw new XMLStreamReaderException(e);
        }
    }

    // -- Fast Infoset ---------------------------------------------------

    public static XMLStreamWriter createFIStreamWriter(OutputStream out) {
        return createFIStreamWriter(out, "UTF-8");
    }

    public static XMLStreamWriter createFIStreamWriter(OutputStream out, String encoding) {
        return createFIStreamWriter(out, encoding, true);
    }

    public static XMLStreamWriter createFIStreamWriter(OutputStream out,
        String encoding, boolean declare)
    {
        // Check if compatible implementation of FI was found
        if (FastInfosetReflection.fiStAXDocumentSerializer_new == null) {
            throw new XMLReaderException("fastinfoset.noImplementation");
        }

        try {
            Object sds = FastInfosetReflection.fiStAXDocumentSerializer_new.newInstance();
            FastInfosetReflection.fiStAXDocumentSerializer_setOutputStream.invoke(sds, out);
            FastInfosetReflection.fiStAXDocumentSerializer_setEncoding.invoke(sds, encoding);
            return (XMLStreamWriter) sds;
        }  catch (Exception e) {
            throw new XMLStreamWriterException(e);
        }
    }


}
