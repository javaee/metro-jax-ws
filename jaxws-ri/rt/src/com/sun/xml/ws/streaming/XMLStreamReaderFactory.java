/*
 * $Id: XMLStreamReaderFactory.java,v 1.7 2005-08-17 22:29:47 kohsuke Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

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
     * FI <code>StAXDocumentParser.reset()</code> method via reflection.
     */
    static Method fiStAXDocumentParser_reset;
    
    /**
     * FI <code>StAXDocumentParser.setInputStream()</code> method via reflection.
     */
    static Method fiStAXDocumentParser_setInputStream;
    
    /**
     * FI <code>StAXDocumentParser.setStringInterning()</code> method via reflection.
     */
    static Method fiStAXDocumentParser_setStringInterning;
    
    /**
     * FI <code>XMLReaderImpl.setInputStream()</code> method via reflection.
     */
    static Method XMLReaderImpl_setInputStream;
    
    /**
     * FI <code>XMLReaderImpl.setReader()</code> method via reflection.
     */
    static Method XMLReaderImpl_setReader;
    
    /**
     * FI stream reader for each thread.
     */
    static ThreadLocal fiStreamReader = new ThreadLocal();
    
    /**
     * Zephyr's stream reader for each thread.
     */
    static ThreadLocal xmlStreamReader = new ThreadLocal();
    
    static {
        // Use StAX pluggability layer to get factory instance
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        
        // Use reflection to avoid static dependency with FI and Zephyr jar
        try {
            Class clazz;
            
            clazz = Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
            fiStAXDocumentParser_new = clazz.getConstructor();
            fiStAXDocumentParser_setInputStream =
                clazz.getMethod("setInputStream", java.io.InputStream.class);
            fiStAXDocumentParser_setStringInterning =
                clazz.getMethod("setStringInterning", boolean.class);
                        
            clazz = Class.forName("com.sun.xml.stream.XMLReaderImpl");
            XMLReaderImpl_setInputStream = 
                clazz.getMethod("setInputStream", java.io.InputStream.class);
            XMLReaderImpl_setReader = 
                clazz.getMethod("setReader", java.io.Reader.class);
        } 
        catch (Exception e) {
            // Falls through
        }
    }
    
    // -- XML ------------------------------------------------------------
    
    public static XMLStreamReader createXMLStreamReader(InputSource source,
        boolean rejectDTDs) {
        if(source.getByteStream()!=null)
            return createXMLStreamReader(source.getByteStream(), rejectDTDs);
        if(source.getCharacterStream()!=null)
            return createXMLStreamReader(source.getCharacterStream(), rejectDTDs);

        try {
            synchronized (xmlInputFactory) {
                return xmlInputFactory.createXMLStreamReader(source.getSystemId(),
                        new URL(source.getSystemId()).openStream());
            }
        } catch (XMLStreamException e) {
            throw new XMLReaderException("stax.cantCreate",
                new LocalizableExceptionAdapter(e));
        } catch (IOException e) {
            throw new XMLReaderException("stax.cantCreate",
                new LocalizableExceptionAdapter(e));
        }
    }
    
    /**
     * Returns a fresh StAX parser. StAX does not support a reset() method, but Zephyr does.
     *
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createXMLStreamReader(InputStream in,
        boolean rejectDTDs) {
        try {
            // If using Zephyr, try re-using the last instance
            if (XMLReaderImpl_setInputStream != null) {
                Object xsr = xmlStreamReader.get();                
                if (xsr == null) {
                    synchronized (xmlInputFactory) {
                        xmlStreamReader.set(xsr = xmlInputFactory.createXMLStreamReader(in));
                    }
                }              
                else {
                    // setInputStream() implies reset()
                    XMLReaderImpl_setInputStream.invoke(xsr, in);
                }                
                return (XMLStreamReader) xsr;
            }
            else {
                synchronized (xmlInputFactory) {
                    return xmlInputFactory.createXMLStreamReader(in);
                }                
            }
        } catch (Exception e) {
            throw new XMLReaderException("stax.cantCreate",
                new LocalizableExceptionAdapter(e));
        }
    }
    
    /**
     * Returns a fresh StAX parser. StAX does not support a reset() method, but Zephyr does.
     *
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createXMLStreamReader(Reader reader,
        boolean rejectDTDs) {
        try {
            // If using Zephyr, try re-using the last instance
            if (XMLReaderImpl_setReader != null) {
                Object xsr = xmlStreamReader.get();                
                if (xsr == null) {
                    synchronized (xmlInputFactory) {
                        xmlStreamReader.set(xsr = xmlInputFactory.createXMLStreamReader(reader));
                    }
                }              
                else {
                    // setReader() implies reset()
                    XMLReaderImpl_setReader.invoke(xsr, reader);
                }                
                return (XMLStreamReader) xsr;
            }
            else {
                synchronized (xmlInputFactory) {
                    return xmlInputFactory.createXMLStreamReader(reader);
                }                
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
                fiStreamReader.set(sdp = fiStAXDocumentParser_new.newInstance());
            } 
            fiStAXDocumentParser_setInputStream.invoke(sdp, in);
            fiStAXDocumentParser_setStringInterning.invoke(sdp, Boolean.TRUE);
            return (XMLStreamReader) sdp;
        } 
        catch (Exception e) {
            throw new XMLStreamReaderException(new LocalizableExceptionAdapter(e));
        }
    }
    
}
