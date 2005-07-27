/**
 * $Id: SourceReaderFactory.java,v 1.5 2005-07-27 13:15:51 spericas Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.streaming;

import java.io.Reader;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

/**
 * @author Santiago.PericasGeertsen@sun.com
 */
public class SourceReaderFactory {
       
    /**
     * Thread variable used to store DOMStreamReader for current thread.
     */
    static ThreadLocal<DOMStreamReader> domStreamReader = 
        new ThreadLocal<DOMStreamReader>();
    
    /**
     * FI FastInfosetSource class.
     */
    static Class fastInfosetSourceClass;
    
    /**
     * FI <code>StAXDocumentSerializer.setEncoding()</code> method via reflection.
     */
    static Method fastInfosetSource_getInputStream;

    static {
        // Use reflection to avoid static dependency with FI jar
        try {
            fastInfosetSourceClass =
                Class.forName("org.jvnet.fastinfoset.FastInfosetSource");
            fastInfosetSource_getInputStream = 
                fastInfosetSourceClass.getMethod("getInputStream");
        } 
        catch (Exception e) {
            fastInfosetSourceClass = null;
        }
    }

    public static XMLStreamReader createSourceReader(Source source,
        boolean rejectDTDs) 
    {
        if (source instanceof StreamSource) {
            StreamSource streamSource = (StreamSource) source;
            InputStream is = streamSource.getInputStream();
            
            if (is != null) {
                return XMLStreamReaderFactory.createXMLStreamReader(is, 
                    rejectDTDs);
            }
            else {
                Reader reader = streamSource.getReader();
                if (reader != null) {
                    return XMLStreamReaderFactory.createXMLStreamReader(reader, 
                        rejectDTDs);
                }
                else {
                    throw new XMLReaderException("sourceReader.invalidSource", 
                        new Object[] { source.getClass().getName() });
                }
            }
        }
        else if (source.getClass() == fastInfosetSourceClass) {
            try {
                return XMLStreamReaderFactory.createFIStreamReader((InputStream) 
                    fastInfosetSource_getInputStream.invoke(source));
            }
            catch (Exception e) {
                throw new XMLReaderException(new LocalizableExceptionAdapter(e));
            }
        }
        else if (source instanceof DOMSource) {
            try {
                DOMStreamReader dsr = domStreamReader.get();
                if (dsr == null) {
                    domStreamReader.set(dsr = new DOMStreamReader());
                } 
                dsr.setCurrentNode(((DOMSource) source).getNode());
                return dsr;
            } 
            catch (Exception e) {
                throw new XMLReaderException(new LocalizableExceptionAdapter(e));
            }
        }
        else if (source instanceof SAXSource) {
            try { 
                // TODO: need SAX to StAX adapter here -- Use transformer for now
                javax.xml.transform.Transformer tx = 
                    javax.xml.transform.TransformerFactory.newInstance().newTransformer();
                javax.xml.transform.dom.DOMResult domResult = 
                    new javax.xml.transform.dom.DOMResult();
                tx.transform(source, domResult);
                return createSourceReader(
                    new javax.xml.transform.dom.DOMSource(domResult.getNode()),
                    rejectDTDs);
            }
            catch (Exception e) {
                throw new XMLReaderException(new LocalizableExceptionAdapter(e));
            }        
        }
        else {
            throw new XMLReaderException("sourceReader.invalidSource", 
                new Object[] { source.getClass().getName() });
        }        
    }

}
