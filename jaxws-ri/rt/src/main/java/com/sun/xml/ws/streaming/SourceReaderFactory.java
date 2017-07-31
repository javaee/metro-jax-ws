/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.util.FastInfosetUtil;
import com.sun.xml.ws.util.xml.XmlUtil;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;

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
                fastInfosetSourceClass.getMethod("getInputStream");
        } 
        catch (Exception e) {
            fastInfosetSourceClass = null;
        }
    }

    public static XMLStreamReader createSourceReader(Source source, boolean rejectDTDs) {
        return createSourceReader(source, rejectDTDs, null);
    }
    
    public static XMLStreamReader createSourceReader(Source source, boolean rejectDTDs, String charsetName) {
        try {
            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource) source;
                InputStream is = streamSource.getInputStream();

                if (is != null) {
                    // Wrap input stream in Reader if charset is specified
                    if (charsetName != null) {
                        return XMLStreamReaderFactory.create(
                            source.getSystemId(), new InputStreamReader(is, charsetName), rejectDTDs);                    
                    }
                    else {
                        return XMLStreamReaderFactory.create(
                            source.getSystemId(), is, rejectDTDs);
                    }
                }
                else {
                    Reader reader = streamSource.getReader();
                    if (reader != null) {
                        return XMLStreamReaderFactory.create(
                            source.getSystemId(), reader, rejectDTDs);
                    }
                    else {
                        return XMLStreamReaderFactory.create(
                            source.getSystemId(), new URL(source.getSystemId()).openStream(), rejectDTDs );
                    }
                }
            }
            else if (source.getClass() == fastInfosetSourceClass) {
                return FastInfosetUtil.createFIStreamReader((InputStream)
                    fastInfosetSource_getInputStream.invoke(source));
            }
            else if (source instanceof DOMSource) {
                DOMStreamReader dsr =  new DOMStreamReader();
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
                        source.getClass().getName());
            }        
        }
        catch (Exception e) {
            throw new XMLReaderException(e);
        }
    }

}
