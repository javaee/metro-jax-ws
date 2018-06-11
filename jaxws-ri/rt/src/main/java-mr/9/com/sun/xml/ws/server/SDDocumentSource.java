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

package com.sun.xml.ws.api.server;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.streaming.TidyXMLStreamReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * SPI that provides the source of {@link SDDocument}.
 *
 * <p>
 * This abstract class could be implemented by applications, or one of the
 * {@link #create} methods can be used.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class SDDocumentSource {
    /**
     * Returns the {@link XMLStreamReader} that reads the document.
     *
     * <p>
     * This method maybe invoked multiple times concurrently.
     *
     * @param xif
     *      The implementation may choose to use this object when it wants to
     *      create a new parser (or it can just ignore this parameter completely.)
     * @return
     *      The caller is responsible for closing the reader to avoid resource leak.
     *
     * @throws XMLStreamException
     *      if something goes wrong while creating a parser.
     * @throws IOException
     *      if something goes wrong trying to read the document.
     */
    public abstract XMLStreamReader read(XMLInputFactory xif) throws IOException, XMLStreamException;

    /**
     * Returns the {@link XMLStreamReader} that reads the document.
     *
     * <p>
     * This method maybe invoked multiple times concurrently.
     *
     * @return
     *      The caller is responsible for closing the reader to avoid resource leak.
     *
     * @throws XMLStreamException
     *      if something goes wrong while creating a parser.
     * @throws IOException
     *      if something goes wrong trying to read the document.
     */
    public abstract XMLStreamReader read() throws IOException, XMLStreamException;

    /**
     * System ID of this document.
     * @return
     */
    public abstract URL getSystemId();

    public static SDDocumentSource create(final Class<?> implClass, final String wsdlLocation) {
        ClassLoader cl = implClass.getClassLoader();
        URL url = cl.getResource(wsdlLocation);
        if (url != null) {
            return create(url);
        } else {
            return create(wsdlLocation, implClass);
        }
    }

    /**
     * Creates {@link SDDocumentSource} from an URL.
     * @param url
     * @return
     */
    public static SDDocumentSource create(final URL url) {
        return new SDDocumentSource() {
            private final URL systemId = url;

            @Override
            public XMLStreamReader read(XMLInputFactory xif) throws IOException, XMLStreamException {
                InputStream is = url.openStream();
                return new TidyXMLStreamReader(
                    xif.createXMLStreamReader(systemId.toExternalForm(),is), is);
            }

            @Override
            public XMLStreamReader read() throws IOException, XMLStreamException {
                InputStream is = url.openStream();
                return new TidyXMLStreamReader(
                   XMLStreamReaderFactory.create(systemId.toExternalForm(),is,false), is);
            }

            @Override
            public URL getSystemId() {
                return systemId;
            }
        };
    }

    /**
     * Creates {@link SDDocumentSource} from resource path using resolvingClass to read the resource.
     * Required for Jigsaw runtime.
     *
     * @param resolvingClass class used to read resource
     * @param path resource path
     */
    private static SDDocumentSource create(final String path, final Class<?> resolvingClass) {
        return new SDDocumentSource() {

            @Override
            public XMLStreamReader read(XMLInputFactory xif) throws IOException, XMLStreamException {
                InputStream is = inputStream();
                return new TidyXMLStreamReader(xif.createXMLStreamReader(path,is), is);
            }

            @Override
            public XMLStreamReader read() throws IOException, XMLStreamException {
                InputStream is = inputStream();
                return new TidyXMLStreamReader(XMLStreamReaderFactory.create(path,is,false), is);
            }

            @Override
            public URL getSystemId() {
                try {
                    return new URL("file://" + path);
                } catch (MalformedURLException e) {
                    return null;
                }
            }

            private InputStream inputStream() throws IOException {
                java.lang.Module module = resolvingClass.getModule();
                InputStream stream = module.getResourceAsStream(path);
                if (stream != null) {
                    return stream;
                }
                throw new ServerRtException("cannot.load.wsdl", path);
            }

        };
    }

    /**
     * Creates a {@link SDDocumentSource} from {@link XMLStreamBuffer}.
     * @param systemId
     * @param xsb
     * @return
     */
    public static SDDocumentSource create(final URL systemId, final XMLStreamBuffer xsb) {
        return new SDDocumentSource() {
            @Override
            public XMLStreamReader read(XMLInputFactory xif) throws XMLStreamException {
                return xsb.readAsXMLStreamReader();
            }

            @Override
            public XMLStreamReader read() throws XMLStreamException {
                return xsb.readAsXMLStreamReader();
            }

            @Override
            public URL getSystemId() {
                return systemId;
            }
        };
    }
}
