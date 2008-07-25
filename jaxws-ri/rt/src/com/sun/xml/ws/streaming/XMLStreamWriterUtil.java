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
        Object obj = null;

        // Hack for JDK6's SJSXP
        if (writer instanceof Map) {
            obj = ((Map) writer).get("sjsxp-outputstream");
        }

        // SJSXP
        if (obj == null) {
            try {
                obj = writer.getProperty("http://java.sun.com/xml/stream/properties/outputstream");
            } catch(Exception ie) {
                // Catch all exceptions. SJSXP in JDK throws NPE
                // nothing to do here
            }
        }

        // woodstox
        if (obj == null) {
            try {
                obj = writer.getProperty("com.ctc.wstx.outputUnderlyingStream");
            } catch(Exception ie) {
                // Catch all exceptions. SJSXP in JDK throws NPE
                // nothing to do here
            }
        }
        if (obj != null) {
            writer.writeCharacters("");  // Force completion of open elems
            writer.flush();
            return (OutputStream)obj;
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
