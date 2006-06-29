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

import com.sun.xml.ws.util.FastInfosetReflection;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

/**
 * <p>A factory to create XML and FI serializers.</p>
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class XMLStreamWriterFactory {
    
    /**
     * StAX input factory shared by all threads.
     */
    static XMLOutputFactory xmlOutputFactory;
        
    /**
     * FI stream writer for each thread.
     */
    static ThreadLocal fiStreamWriter = new ThreadLocal();
    
    static {
        // Use StAX pluggability layer to get factory instance
        xmlOutputFactory = XMLOutputFactory.newInstance();        
    }
            
    // -- XML ------------------------------------------------------------
    
    public static XMLStreamWriter createXMLStreamWriter(OutputStream out) {
        return createXMLStreamWriter(out, "UTF-8");
    }

    public static XMLStreamWriter createXMLStreamWriter(OutputStream out, String encoding) {
        return createXMLStreamWriter(out, encoding, true);
    }
    
    /**
     * TODO: declare?
     * TODO: Use thread locals and reset() when using Zephyr
     */
    public static XMLStreamWriter createXMLStreamWriter(OutputStream out,
        String encoding, boolean declare) 
    {
        try {
            // Assume StAX factory implementation is not thread-safe 
            synchronized (xmlOutputFactory) {
                return xmlOutputFactory.createXMLStreamWriter(out, encoding);
            }
        }
        catch (Exception e) {
            throw new XMLReaderException("stax.cantCreate",e);
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
            Object sds = fiStreamWriter.get();
            if (sds == null) {
                // Do not use StAX pluggable layer for FI
                fiStreamWriter.set(sds = FastInfosetReflection.fiStAXDocumentSerializer_new.newInstance());
            }            
            FastInfosetReflection.fiStAXDocumentSerializer_setOutputStream.invoke(sds, out);
            FastInfosetReflection.fiStAXDocumentSerializer_setEncoding.invoke(sds, encoding);
            return (XMLStreamWriter) sds;
        }  catch (Exception e) {
            throw new XMLStreamWriterException(e);
        }        
    }
    
}
