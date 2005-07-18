/*
 * $Id: XMLStreamReaderFactory.java,v 1.4 2005-07-18 19:06:11 spericas Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import java.io.Reader;
import java.io.InputStream;
import org.xml.sax.InputSource;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;

import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

/**
 * <p>A factory to create XML and FI parsers.</p>
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class XMLStreamReaderFactory {
    
    /**
     * StAX input factory shared by all threads.
     */
    static XMLInputFactory xmlInputFactory;
    
    /**
     * FI StAXDocumentParser constructor using reflection.
     */
    static Constructor fiStAXDocumentParser_new;
    
    /**
     * FI <code>StAXDocumentParser.setInputStream()</code> method via reflection.
     */
    static Method fiStAXDocumentParser_setInputStream;
    
    /**
     * FI stream reader for each thread.
     */
    static ThreadLocal fiStreamReader = new ThreadLocal();
    
    static {
        // Use StAX pluggability layer to get factory instance
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        
        // Use reflection to avoid static dependency with FI jar
        try {
            Class clazz =
                Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
            fiStAXDocumentParser_new = clazz.getConstructor((Class) null);
            fiStAXDocumentParser_setInputStream =
                clazz.getMethod("setInputStream", java.io.InputStream.class);
        } 
        catch (Exception e) {
            fiStAXDocumentParser_new = null;
        }
    }
    
    // -- XML ------------------------------------------------------------
    
    public static XMLStreamReader createXMLStreamReader(InputSource source,
        boolean rejectDTDs) {
        return createXMLStreamReader(source.getByteStream(), rejectDTDs);
    }
    
    /**
     * Returns a fresh StAX parser. StAX does not support a reset() method, but Zephyr does.
     *
     * TODO: Use reflection to call reset() method on Zephyr?
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createXMLStreamReader(InputStream in,
        boolean rejectDTDs) {
        try {
            // Assume StAX factory implementation is not thread-safe
            synchronized (xmlInputFactory) {
                return xmlInputFactory.createXMLStreamReader(in);
            }
        } catch (Exception e) {
            throw new XMLReaderException("stax.cantCreate",
                new LocalizableExceptionAdapter(e));
        }
    }
    
    /**
     * Returns a fresh StAX parser. StAX does not support a reset() method, but Zephyr does.
     *
     * TODO: Use reflection to call reset() method on Zephyr?
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createXMLStreamReader(Reader reader,
        boolean rejectDTDs) {
        try {
            // Assume StAX factory implementation is not thread-safe
            synchronized (xmlInputFactory) {
                return xmlInputFactory.createXMLStreamReader(reader);
            }
        } 
        catch (Exception e) {
            throw new XMLReaderException("stax.cantCreate", 
                new LocalizableExceptionAdapter(e));
        }
    }
    
    // -- Fast Infoset ---------------------------------------------------
    
    public static XMLStreamReader createFIStreamReader(InputSource source) {
        return createFIStreamReader(source.getByteStream());
    }
    
    //
    // Returns the FI parser allocated for this thread.
    //
    public static XMLStreamReader createFIStreamReader(InputStream in) {
        // Check if compatible implementation of FI was found
        if (fiStAXDocumentParser_new == null) {
            throw new XMLReaderException("fastinfoset.noImplementation");
        }
        
        try {
            Object sdp = fiStreamReader.get();
            if (sdp == null) {
                // Do not use StAX pluggable layer for FI
                fiStreamReader.set(sdp = fiStAXDocumentParser_new.newInstance((Class) null));
            } 
            fiStAXDocumentParser_setInputStream.invoke(sdp, in);
            return (XMLStreamReader) sdp;
        } 
        catch (Exception e) {
            throw new XMLStreamReaderException(new LocalizableExceptionAdapter(e));
        }
    }
    
}
