/*
 * $Id: XMLStreamReaderFactory.java,v 1.9 2005-08-24 19:01:38 spericas Exp $
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
import java.net.MalformedURLException;

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
     * Zephyr's <code>XMLReaderImpl.setInputSource()</code> method via reflection.
     */
    static Method XMLReaderImpl_setInputSource;
    
    /**
     * Zephyr's <code>XMLReaderImpl.reset()</code> method via reflection.
     */
    static Method XMLReaderImpl_reset;
    
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

        /*
        try {
            // Turn OFF internal factory caching in Zephyr -- not thread safe
            xmlInputFactory.setProperty("reuse-instance", Boolean.FALSE);
        }
        catch (IllegalArgumentException e) {
            // falls through
        }
        */
            
        // Use reflection to avoid static dependency with FI and Zephyr jar
        try {
            Class clazz;
            
            clazz = Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentParser");
            fiStAXDocumentParser_new = clazz.getConstructor();
            fiStAXDocumentParser_setInputStream =
                clazz.getMethod("setInputStream", java.io.InputStream.class);
            fiStAXDocumentParser_setStringInterning =
                clazz.getMethod("setStringInterning", boolean.class);
           
            /*
            clazz = Class.forName("com.sun.xml.stream.XMLReaderImpl");
            // Are we running on top of JAXP 1.4?
            if (clazz == null) {
                clazz = Class.forName("com.sun.xml.stream.XMLStreamReaderImpl");
            }
            XMLReaderImpl_setInputSource = 
                clazz.getMethod("setInputSource", org.xml.sax.InputSource.class);
            XMLReaderImpl_reset = clazz.getMethod("reset");
            */
        } 
        catch (Exception e) {
            // Falls through
        }
    }
    
    // -- XML ------------------------------------------------------------
    
    /**
     * Returns a StAX parser created from an InputSource.
     *
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createXMLStreamReader(InputSource source,
        boolean rejectDTDs) {
        if (source.getByteStream() != null) {
            return createXMLStreamReader(null, source.getByteStream(), rejectDTDs);
        }
        
        if (source.getCharacterStream() != null) {
            return createXMLStreamReader(source.getCharacterStream(), rejectDTDs);
        }

        try {
            return createXMLStreamReader(source.getSystemId(),
                new URL(source.getSystemId()).openStream(), rejectDTDs);
        }
        catch (Exception e) {
            throw new XMLReaderException("stax.cantCreate",
                new LocalizableExceptionAdapter(e));            
        }
    }
    
    /**
     * Returns a StAX parser from an InputStream.
     *
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createXMLStreamReader(InputStream in,
        boolean rejectDTDs) {
        return createXMLStreamReader(null, in, rejectDTDs);        
    }
    
    /**
     * Returns a StAX parser from an InputStream. Attemps to re-use parsers if
     * underlying representation is Zephyr.
     *
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createXMLStreamReader(String systemId, 
        InputStream in, boolean rejectDTDs) {
        try {
            // If using Zephyr, try re-using the last instance
            if (XMLReaderImpl_setInputSource != null) {
                Object xsr = xmlStreamReader.get();                
                if (xsr == null) {
                    synchronized (xmlInputFactory) {
                        xmlStreamReader.set(
                            xsr = xmlInputFactory.createXMLStreamReader(systemId, in));
                    }
                }              
                else {
                    XMLReaderImpl_reset.invoke(xsr);
                    InputSource inputSource = new InputSource(in);
                    inputSource.setSystemId(systemId);
                    XMLReaderImpl_setInputSource.invoke(xsr, inputSource);
                }                
                return (XMLStreamReader) xsr;
            }
            else {
                synchronized (xmlInputFactory) {
                    return xmlInputFactory.createXMLStreamReader(systemId, in);
                }                
            }
        } catch (Exception e) {
            throw new XMLReaderException("stax.cantCreate",
                new LocalizableExceptionAdapter(e));
        }
    }
    
    /**
     * Returns a StAX parser from a Reader. Attemps to re-use parsers if
     * underlying representation is Zephyr.
     *
     * TODO: Reject DTDs?
     */
    public static XMLStreamReader createXMLStreamReader(Reader reader,
        boolean rejectDTDs) {
        try {
            // If using Zephyr, try re-using the last instance
            if (XMLReaderImpl_setInputSource != null) {
                Object xsr = xmlStreamReader.get();                
                if (xsr == null) {
                    synchronized (xmlInputFactory) {
                        xmlStreamReader.set(
                            xsr = xmlInputFactory.createXMLStreamReader(reader));
                    }
                }              
                else {
                    XMLReaderImpl_reset.invoke(xsr);
                    XMLReaderImpl_setInputSource.invoke(xsr, new InputSource(reader));
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
