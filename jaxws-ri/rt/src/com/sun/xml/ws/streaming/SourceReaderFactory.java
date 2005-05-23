/**
 * $Id: SourceReaderFactory.java,v 1.1 2005-05-23 22:59:35 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
                fastInfosetSourceClass.getMethod("getInputStream", (Class) null);
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
            // TODO: use a DOMStreamReader
            throw new RuntimeException("DOMSource not yet supported");
        }
        else if (source instanceof SAXSource) {
            // TODO: need SAX to StAX adapter here 
            throw new RuntimeException("SAXSource not yet supported");
        }
        else {
            throw new XMLReaderException("sourceReader.invalidSource", 
                new Object[] { source.getClass().getName() });
        }        
    }

}
