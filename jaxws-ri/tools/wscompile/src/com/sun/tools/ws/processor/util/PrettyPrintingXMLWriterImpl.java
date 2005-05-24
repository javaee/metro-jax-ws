/*
 * $Id: PrettyPrintingXMLWriterImpl.java,v 1.1 2005-05-24 13:43:47 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

import com.sun.xml.ws.util.NamespaceSupport;
import com.sun.xml.ws.streaming.PrefixFactory;
import com.sun.xml.ws.streaming.XMLWriterBase;
import com.sun.xml.ws.streaming.XMLWriterException;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.xml.CDATA;
import com.sun.tools.ws.util.xml.PrettyPrintingXmlWriter;

/**
 * <p> A concrete XMLWriter implementation class. </p>
 *
 * @author JAX-RPC Development Team
 */
public class PrettyPrintingXMLWriterImpl extends XMLWriterBase {

    public PrettyPrintingXMLWriterImpl(OutputStream out, String enc,
        boolean declare) {

        try {
            _writer = new PrettyPrintingXmlWriter(out, enc, declare);
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void startElement(String localName, String uri) {
        try {
            _nsSupport.pushContext();

            if (!uri.equals("")) {
                String aPrefix = null;
                boolean mustDeclarePrefix = false;

                String defaultNamespaceURI = _nsSupport.getPrefix("");
                if (defaultNamespaceURI != null) {
                    if (uri.equals(defaultNamespaceURI)) {
                        aPrefix = "";
                    }
                }

                aPrefix = _nsSupport.getPrefix(uri);

                if (aPrefix == null) {
                    mustDeclarePrefix = true;

                    if (_prefixFactory != null) {
                        aPrefix = _prefixFactory.getPrefix(uri);
                    }

                    if (aPrefix == null) {
                        throw new XMLWriterException(
                            "xmlwriter.noPrefixForURI", uri);
                    }
                }

                String rawName = aPrefix.equals("") ?
                    localName : (aPrefix + ":" + localName);

                _writer.start(rawName);
                _elemStack.push(rawName);

                if (mustDeclarePrefix) {
                    writeNamespaceDeclaration(aPrefix, uri);
                }
            } else {
                _writer.start(localName);
                _elemStack.push(localName);
            }
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void startElement(String localName, String uri, String prefix) {
        try {
            _nsSupport.pushContext();

            if (!uri.equals("")) {
                String aPrefix = null;
                boolean mustDeclarePrefix = false;

                String defaultNamespaceURI = _nsSupport.getPrefix("");
                if (defaultNamespaceURI != null) {
                    if (uri.equals(defaultNamespaceURI)) {
                        aPrefix = "";
                    }
                }

                aPrefix = _nsSupport.getPrefix(uri);

                if (aPrefix == null) {
                    mustDeclarePrefix = true;

                    aPrefix = prefix;

                    if (aPrefix == null) {
                        throw new XMLWriterException(
                            "xmlwriter.noPrefixForURI", uri);
                    }
                }

                String rawName = aPrefix.equals("") ?
                    localName : (aPrefix + ":" + localName);

                _writer.start(rawName);
                _elemStack.push(rawName);

                if (mustDeclarePrefix) {
                    writeNamespaceDeclaration(aPrefix, uri);
                }
            } else {
                _writer.start(localName);
                _elemStack.push(localName);
            }
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void writeNamespaceDeclaration(String prefix, String uri) {
        try {
            _nsSupport.declarePrefix(prefix, uri);

            String rawName = "xmlns";
            if ((prefix != null) && !prefix.equals("")) {

                // it's not a default namespace declaration
                rawName += ":" + prefix;
            }

            _writer.attribute(rawName, uri);
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void writeNamespaceDeclaration(String uri) {

        if (_prefixFactory == null) {
            throw new XMLWriterException("xmlwriter.noPrefixForURI", uri);
        }

        String aPrefix = _prefixFactory.getPrefix(uri);
        writeNamespaceDeclaration(aPrefix, uri);
    }

    public void writeAttribute(String localName, String uri, String value) {
        try {
            if (!uri.equals("")) {

                String aPrefix = null;
                boolean mustDeclarePrefix = false;

                String defaultNamespaceURI = _nsSupport.getPrefix("");
                if (defaultNamespaceURI != null) {
                    if (uri.equals(defaultNamespaceURI)) {
                        aPrefix = "";
                    }
                }

                aPrefix = _nsSupport.getPrefix(uri);

                if (aPrefix == null) {
                    mustDeclarePrefix = true;

                    if (_prefixFactory != null) {
                        aPrefix = _prefixFactory.getPrefix(uri);
                    }

                    if (aPrefix == null) {
                        throw new XMLWriterException("xmlwriter.noPrefixForURI", uri);
                    }
                }

                String rawName = aPrefix + ":" + localName;
                _writer.attribute(rawName, value);

                if (mustDeclarePrefix) {
                    writeNamespaceDeclaration(aPrefix, uri);
                }

            } else {
                _writer.attribute(localName, value);
            }
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void writeAttributeUnquoted(String localName, String uri,
        String value) {

        try {
            if (!uri.equals("")) {

                String aPrefix = null;
                boolean mustDeclarePrefix = false;

                String defaultNamespaceURI = _nsSupport.getPrefix("");
                if (defaultNamespaceURI != null) {
                    if (uri.equals(defaultNamespaceURI)) {
                        aPrefix = "";
                    }
                }

                aPrefix = _nsSupport.getPrefix(uri);

                if (aPrefix == null) {
                    mustDeclarePrefix = true;

                    if (_prefixFactory != null) {
                        aPrefix = _prefixFactory.getPrefix(uri);
                    }

                    if (aPrefix == null) {
                        throw new XMLWriterException(
                            "xmlwriter.noPrefixForURI", uri);
                    }
                }

                _writer.attributeUnquoted(aPrefix, localName, value);

                if (mustDeclarePrefix) {
                    writeNamespaceDeclaration(aPrefix, uri);
                }

            } else {
                _writer.attributeUnquoted(localName, value);
            }
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void writeChars(CDATA chars) {
        try {
            _writer.chars(chars);
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void writeChars(String chars) {
        try {
            _writer.chars(chars);
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void writeCharsUnquoted(String chars) {
        try {
            _writer.charsUnquoted(chars);
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void writeCharsUnquoted(char[] buf, int offset, int len) {
        try {
            _writer.charsUnquoted(buf, offset, len);
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void endElement() {
        try {

            // write the end tag
            String rawName = (String)_elemStack.pop();
            _writer.end(rawName);

            _nsSupport.popContext();
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public PrefixFactory getPrefixFactory() {
        return _prefixFactory;
    }

    public void setPrefixFactory(PrefixFactory factory) {
        _prefixFactory = factory;
    }

    public String getURI(String prefix) {
        return _nsSupport.getURI(prefix);
    }

    public String getPrefix(String uri) {
        return _nsSupport.getPrefix(uri);
    }

    public void flush() {
        try {
            _writer.flush();
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    public void close() {
        try {
            _writer.close();
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    private XMLWriterException wrapException(IOException e) {
        return new XMLWriterException("xmlwriter.ioException",
            new LocalizableExceptionAdapter(e));
    }

    private PrettyPrintingXmlWriter _writer;
    private NamespaceSupport _nsSupport = new NamespaceSupport();
    private Stack _elemStack = new Stack();
    private PrefixFactory _prefixFactory;
}
