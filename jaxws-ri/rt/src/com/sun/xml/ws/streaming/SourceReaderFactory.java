/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.streaming;

import com.sun.xml.ws.util.FastInfosetReflection;
import com.sun.xml.ws.util.xml.XmlUtil;

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Santiago.PericasGeertsen@sun.com
 */
public class SourceReaderFactory {
       
    /**
     * Thread variable used to store DOMStreamReader for current thread.
     */
    static ThreadLocal<DOMStreamReader> domStreamReader = 
        new ThreadLocal<DOMStreamReader>();

    public static XMLStreamReader createSourceReader(Source source,
        boolean rejectDTDs) 
    {
        return createSourceReader(source, rejectDTDs, null);
    }
    
    public static XMLStreamReader createSourceReader(Source source,
        boolean rejectDTDs, String charsetName) 
    {
        try {
            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource) source;
                InputStream is = streamSource.getInputStream();

                if (is != null) {
                    // Wrap input stream in Reader if charset is specified
                    if (charsetName != null) {
                        return XMLStreamReaderFactory.createXMLStreamReader(
                            new InputStreamReader(is, charsetName), rejectDTDs);                    
                    }
                    else {
                        return XMLStreamReaderFactory.createXMLStreamReader(is, 
                            rejectDTDs);
                    }
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
            else if (FastInfosetReflection.isFastInfosetSource(source)) {
                return XMLStreamReaderFactory.createFIStreamReader((InputStream) 
                    FastInfosetReflection.FastInfosetSource_getInputStream(source));
            }
            else if (source instanceof DOMSource) {
                DOMStreamReader dsr = domStreamReader.get();
                if (dsr == null) {
                    domStreamReader.set(dsr = new DOMStreamReader());
                } 
                dsr.setCurrentNode(((DOMSource) source).getNode());
                return dsr;
            }
            else if (source instanceof SAXSource) {
                // TODO: need SAX to StAX adapter here -- Use transformer for now
                Transformer tx =  XmlUtil.newTransformer();
                DOMResult domResult = new DOMResult();
                tx.transform(source, domResult);
                return createSourceReader(
                    new DOMSource(domResult.getNode()),
                    rejectDTDs);
            }
            else {
                throw new XMLReaderException("sourceReader.invalidSource", 
                    new Object[] { source.getClass().getName() });
            }        
        }
        catch (Exception e) {
            throw new XMLReaderException(e);
        }
    }

}
