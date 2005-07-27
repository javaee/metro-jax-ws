/*
 * $Id: XMLStreamWriterFactory.java,v 1.7 2005-07-27 13:15:51 spericas Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

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
     * FI StAXDocumentSerializer constructor using reflection.
     */
    static Constructor fiStAXDocumentSerializer_new;
    
    /**
     * FI <code>StAXDocumentSerializer.reset()</code> method via reflection.
     */
    static Method fiStAXDocumentSerializer_reset;
    
    /**
     * FI <code>StAXDocumentSerializer.setOutputStream()</code> method via reflection.
     */
    static Method fiStAXDocumentSerializer_setOutputStream;
    
    /**
     * FI <code>StAXDocumentSerializer.setEncoding()</code> method via reflection.
     */
    static Method fiStAXDocumentSerializer_setEncoding;
    
    /**
     * FI stream writer for each thread.
     */
    static ThreadLocal fiStreamWriter = new ThreadLocal();
    
    static {
        // Use StAX pluggability layer to get factory instance
        xmlOutputFactory = XMLOutputFactory.newInstance();
        
        // Use reflection to avoid static dependency with FI jar
        try {
            Class clazz =
                Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentSerializer");
            fiStAXDocumentSerializer_new = clazz.getConstructor();
            fiStAXDocumentSerializer_setOutputStream =
                clazz.getMethod("setOutputStream", java.io.OutputStream.class);
            fiStAXDocumentSerializer_setEncoding =
                clazz.getMethod("setEncoding", String.class);
        } 
        catch (Exception e) {
            fiStAXDocumentSerializer_new = null;
        }
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
            throw new XMLReaderException("stax.cantCreate",
                new LocalizableExceptionAdapter(e));
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
        if (fiStAXDocumentSerializer_new == null) {
            throw new XMLReaderException("fastinfoset.noImplementation");
        }
        
        try {
            Object sds = fiStreamWriter.get();
            if (sds == null) {
                // Do not use StAX pluggable layer for FI
                fiStreamWriter.set(sds = fiStAXDocumentSerializer_new.newInstance());
            }            
            fiStAXDocumentSerializer_setOutputStream.invoke(sds, out);
            fiStAXDocumentSerializer_setEncoding.invoke(sds, encoding);
            return (XMLStreamWriter) sds;
        } 
        catch (Exception e) {
            throw new XMLStreamWriterException(new LocalizableExceptionAdapter(e));
        }        
    }
    
}
