/*
 * $Id: ParserUtil.java,v 1.1 2005-05-23 23:13:22 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config.parser;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.config.ConfigurationException;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.util.xml.XmlUtil;

/**
 *
 * @author JAX-RPC Development Team
 */
public class ParserUtil {

    public static String getAttribute(XMLReader reader, String name) {
        Attributes attributes = reader.getAttributes();
        return attributes.getValue(name);
    }

    public static String getNonEmptyAttribute(XMLReader reader, String name) {
        String value = getAttribute(reader, name);
        if (value != null && value.equals("")) {
            failWithLocalName("configuration.invalidAttributeValue",
                reader, name);
        }
        return value;
    }

    public static String getMandatoryAttribute(XMLReader reader, String name) {
        String value = getAttribute(reader, name);
        if (value == null) {
            failWithLocalName("configuration.missing.attribute", reader, name);
        }
        return value;
    }

    public static String getMandatoryNonEmptyAttribute(XMLReader reader,
        String name) {

        String value = getAttribute(reader, name);
        if (value == null) {
            failWithLocalName("configuration.missing.attribute", reader, name);
        }
        else if (value.equals("")) {
            failWithLocalName("configuration.invalidAttributeValue",
                reader, name);
        }
        return value;
    }

    public static QName getQNameAttribute(XMLReader reader, String name) {
        String value = getAttribute(reader, name);

        if (value == null) {
            return null;
        }

        String prefix = XmlUtil.getPrefix(value);
        String uri = "";
        if (prefix != null) {
            uri = reader.getURI(prefix);
            if (uri == null) {
                failWithLocalName("configuration.invalidAttributeValue",
                    reader, name);
            }
        }
        String localPart = XmlUtil.getLocalPart(value);
        return new QName(uri, localPart);
    }

    public static void ensureNoContent(XMLReader reader) {
        if (reader.nextElementContent() != XMLReader.END) {
            fail("configuration.unexpectedContent", reader);
        }
    }

    public static void fail(String key, XMLReader reader) {
        throw new ConfigurationException(key,
            Integer.toString(reader.getLineNumber()));
    }

    public static void failWithFullName(String key, XMLReader reader) {
        throw new ConfigurationException(key, new Object[] { Integer.toString(
            reader.getLineNumber()), reader.getName().toString() });
    }

    public static void failWithLocalName(String key, XMLReader reader) {
        throw new ConfigurationException(key, new Object[] { Integer.toString(
            reader.getLineNumber()), reader.getLocalName() });
    }

    public static void failWithLocalName(String key, XMLReader reader,
        String arg) {

        throw new ConfigurationException(key, new Object[] { Integer.toString(
            reader.getLineNumber()), reader.getLocalName(), arg });
    }
}
