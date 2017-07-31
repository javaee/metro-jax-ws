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

package com.sun.xml.ws.commons.xmlutil;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.util.xml.XmlUtil;

import javax.xml.stream.*;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Utility class that provides conversion of different XML representations 
 * from/to various other formats
 * 
 * @author Marek Potociar
 */
public final class Converter {

    public static final String UTF_8 = "UTF-8";

    private Converter() {
        // prevents instantiation
    }
    private static final Logger LOGGER = Logger.getLogger(Converter.class);
    private static final ContextClassloaderLocal<XMLOutputFactory> xmlOutputFactory = new ContextClassloaderLocal<XMLOutputFactory>() {
        @Override
        protected XMLOutputFactory initialValue() throws Exception {
            return XMLOutputFactory.newInstance();
        }
    };
    private static final AtomicBoolean logMissingStaxUtilsWarning = new AtomicBoolean(false);

    /**
     * Converts a throwable to String
     * 
     * @param throwable
     * @return String representation of throwable
     */
    public static String toString(Throwable throwable) {
        if (throwable == null) {
            return "[ No exception ]";
        }

        StringWriter stringOut = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringOut));

        return stringOut.toString();
    }

    public static String toString(Packet packet) {
        if (packet == null) {
            return "[ Null packet ]";
        } else if (packet.getMessage() == null) {
                return "[ Empty packet ]";
        }
        
        return toString(packet.getMessage());
    }
    
    public static String toStringNoIndent(Packet packet) {
        if (packet == null) {
            return "[ Null packet ]";
        } else if (packet.getMessage() == null) {
                return "[ Empty packet ]";
        }
        
        return toStringNoIndent(packet.getMessage());
    }

    public static String toString(Message message) {
        return toString(message, true);
    }
    
    public static String toStringNoIndent(Message message) {
        return toString(message, false);
    }

    private static String toString(Message message, boolean createIndenter) {
        if (message == null) {
            return "[ Null message ]";
        }
        StringWriter stringOut = null;
        try {
            stringOut = new StringWriter();
            XMLStreamWriter writer = null;
            try {
                writer = xmlOutputFactory.get().createXMLStreamWriter(stringOut);
                if (createIndenter) {
                    writer = createIndenter(writer);
                }
                message.copy().writeTo(writer);
            } catch (Exception e) { // WSIT-1596 - Message Dumping should not affect other processing
                LOGGER.log(Level.WARNING, "Unexpected exception occured while dumping message", e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (XMLStreamException ignored) {
                        LOGGER.fine("Unexpected exception occured while closing XMLStreamWriter", ignored);
                    }
                }
            }
            return stringOut.toString();
        } finally {
            if (stringOut != null) {
                try {
                    stringOut.close();
                } catch (IOException ex) {
                    LOGGER.finest("An exception occured when trying to close StringWriter", ex);
                }
            }
        }
    }

    public static byte[] toBytes(Message message, String encoding) throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            if (message != null) {
                XMLStreamWriter xsw = xmlOutputFactory.get().createXMLStreamWriter(baos, encoding);
                try {
                    message.writeTo(xsw);
                } finally {
                    try {
                        xsw.close();
                    } catch (XMLStreamException ex) {
                        LOGGER.warning("Unexpected exception occured while closing XMLStreamWriter", ex);
                    }
                }
            }

            return baos.toByteArray();
        } finally {
            try {
                baos.close();
            } catch (IOException ex) {
                LOGGER.warning("Unexpected exception occured while closing ByteArrayOutputStream", ex);
            }
        }
    }

    /**
     * Converts JAX-WS RI message represented as input stream back to Message
     * object.
     * 
     * @param dataStream message data stream
     * @param encoding message data stream encoding
     * 
     * @return {@link com.sun.xml.ws.api.message.Message} object created from the data stream
     */
    public static Message toMessage(@NotNull InputStream dataStream, String encoding) throws XMLStreamException {
        XMLStreamReader xsr = XmlUtil.newXMLInputFactory(true).createXMLStreamReader(dataStream, encoding);
        return Messages.create(xsr);
    }

    public static String messageDataToString(final byte[] data, final String encoding) {
        try {
            return toString(toMessage(new ByteArrayInputStream(data), encoding));
            // closing ByteArrayInputStream has no effect, so ignoring the redundant call
        } catch (XMLStreamException ex) {
            LOGGER.warning("Unexpected exception occured while converting message data to string", ex);
            return "[ Message Data Conversion Failed ]";
        }
    }

    /**
     * Wraps {@link javax.xml.stream.XMLStreamWriter} by an indentation engine if possible.
     *
     * <p>
     * We can do this only when we have {@code stax-utils.jar} in the class path.
     */
    private static XMLStreamWriter createIndenter(XMLStreamWriter writer) {
        try {
            Class<?> clazz = Converter.class.getClassLoader().loadClass("javanet.staxutils.IndentingXMLStreamWriter");
            Constructor<?> c = clazz.getConstructor(XMLStreamWriter.class);
            writer = XMLStreamWriter.class.cast(c.newInstance(writer));
        } catch (Exception ex) {
            // if stax-utils.jar is not in the classpath, this will fail
            // so, we'll just have to do without indentation
            if (logMissingStaxUtilsWarning.compareAndSet(false, true)) {
                LOGGER.log(Level.WARNING, "Put stax-utils.jar to the classpath to indent the dump output", ex);
            }
        }
        return writer;
    }
}
