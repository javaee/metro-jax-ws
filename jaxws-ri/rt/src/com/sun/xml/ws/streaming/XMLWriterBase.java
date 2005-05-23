/*
 * $Id: XMLWriterBase.java,v 1.1 2005-05-23 22:59:37 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import javax.xml.namespace.QName;

import com.sun.xml.ws.util.xml.CDATA;

/**
 * <p> A base class for XMLWriter implementations. </p>
 *
 * <p> It provides the implementation of some derived XMLWriter methods. </p>
 *
 * @author JAX-RPC Development Team
 */
public abstract class XMLWriterBase implements XMLWriter {

    public void startElement(String localName) {
        startElement(localName, "");
    }

    public void startElement(QName name) {
        startElement(name.getLocalPart(), name.getNamespaceURI());
    }

    public void startElement(QName name, String prefix) {
        startElement(name.getLocalPart(), name.getNamespaceURI(), prefix);
    }

    public void writeAttribute(String localName, String value) {
        writeAttribute(localName, "", value);
    }

    public void writeAttribute(QName name, String value) {
        writeAttribute(name.getLocalPart(), name.getNamespaceURI(), value);
    }

    public void writeAttributeUnquoted(QName name, String value) {
        writeAttributeUnquoted(
            name.getLocalPart(),
            name.getNamespaceURI(),
            value);
    }

    public void writeAttributeUnquoted(String localName, String value) {
        writeAttributeUnquoted(localName, "", value);
    }

    public abstract void writeChars(CDATA chars);

    public abstract void writeChars(String chars);

    public void writeComment(String comment) {
    }
}
