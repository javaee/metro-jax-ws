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

package com.sun.xml.ws.streaming;

import com.sun.istack.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.util.Map;
import java.io.OutputStream;

/**
 * <p>XMLStreamWriterUtil provides some utility methods intended to be used
 * in conjunction with a StAX XMLStreamWriter. </p>
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class XMLStreamWriterUtil {

    private XMLStreamWriterUtil() {
    }

    /**
     * Gives the underlying stream for XMLStreamWriter. It closes any start elements, and returns the stream so
     * that JAXB can write infoset directly to the stream.
     *
     * @param writer XMLStreamWriter for which stream is required
     * @return  underlying OutputStream, null if writer doesn't provide a way to get it
     * @throws XMLStreamException if any of writer operations throw the exception
     */
    public static @Nullable OutputStream getOutputStream(XMLStreamWriter writer) throws XMLStreamException {
        // SJSXP
        if (writer instanceof Map) {
            Object obj = ((Map) writer).get("sjsxp-outputstream");
            if (obj != null) {
                writer.writeCharacters("");  // Force completion of open elems
                return (OutputStream)obj;
            }
        }
        // woodstox
        try {
            Object obj = writer.getProperty("com.ctc.wstx.outputUnderlyingStream");
            if (obj != null) {
                writer.writeCharacters("");  // Force completion of open elems
                writer.flush();
                return (OutputStream)obj;
            }
        } catch(IllegalArgumentException ie) {
            // nothing to do here
        }
        return null;
    }


    public static String encodeQName(XMLStreamWriter writer, QName qname,
        PrefixFactory prefixFactory) 
    {
        // NOTE: Here it is assumed that we do not serialize using default
        // namespace declarations and therefore that writer.getPrefix will
        // never return ""

        try {
            String namespaceURI = qname.getNamespaceURI();
            String localPart = qname.getLocalPart();

            if (namespaceURI == null || namespaceURI.equals("")) {
                return localPart;
            } 
            else {
                String prefix = writer.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = prefixFactory.getPrefix(namespaceURI);
                    writer.writeNamespace(prefix, namespaceURI);
                }
                return prefix + ":" + localPart;
            }
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }
}
