/**
 * $Id: ParserUtil.java,v 1.4 2005-07-20 20:58:52 kwalsh Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;


import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.streaming.XMLReaderException;
import com.sun.xml.ws.util.xml.XmlUtil;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;


/**
 * @author WS Development Team
 */
public class ParserUtil {
    public static String getAttribute(XMLReader reader, String name) {
        Attributes attributes = reader.getAttributes();

        return attributes.getValue(name);
    }

    public static String getMandatoryAttribute(XMLReader reader, String name) {
        String value = getAttribute(reader, name);

        if (value == null) {
            failWithLocalName("client.missing.attribute", reader, name);
        }

        return value;
    }

    public static void verifyTag(XMLReader reader, QName name) {
        if (!name.equals(reader.getName())) {
            throw new XMLReaderException("xmlreader.unexpectedState.tag",
                new Object[] { name, reader.getName() });
        }
    }

    public static String getMandatoryNonEmptyAttribute(XMLReader reader,
        String name) {
        String value = getAttribute(reader, name);

        if (value == null) {
            failWithLocalName("client.missing.attribute", reader, name);
        } else if (value.equals("")) {
            failWithLocalName("client.invalidAttributeValue", reader, name);
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
                failWithLocalName("client.invalidAttributeValue", reader, name);
            }
        }

        String localPart = XmlUtil.getLocalPart(value);

        return new QName(uri, localPart);
    }

    public static void verifyReaderState(XMLReader reader, int expectedState) {
        if (reader.getState() != expectedState) {
            throw new XMLReaderException("xmlreader.unexpectedState");
        }
    }

    public static String getStateName(XMLReader reader) {
        switch (reader.getState()) {
        case XMLReader.BOF:
            return "BOF";

        case XMLReader.START:
            return "START";

        case XMLReader.END:
            return "END";

        case XMLReader.CHARS:
            return "CHARS";

        case XMLReader.PI:
            return "PI";

        case XMLReader.EOF:
            return "EOF";

        default:
            return "UNKNOWN";
        }
    }

    public static String processSystemIdWithBase(String baseSystemId,
        String systemId) {
        try {
            URL base = null;

            try {
                base = new URL(baseSystemId);
            } catch (MalformedURLException e) {
                base = new File(baseSystemId).toURL();
            }

            try {
                URL url = new URL(base, systemId);

                //return url.toString();
            } catch (MalformedURLException e) {
                //fail("parsing.invalidURI", systemId);
            }
        } catch (MalformedURLException e) {
            //fail("parsing.invalidURI", baseSystemId);
        }

        return null; // keep compiler happy
    }

    /* public static void fail(String key) {
         throw new ParseException(key);
     }
     */
    public static void ensureNoContent(XMLReader reader) {
        if (reader.nextElementContent() != XMLReader.END) {
            fail("client.unexpectedContent", reader);
        }
    }

    public static void fail(String key, XMLReader reader) {
        //throw new WebServicesClientException(key,
        //        Integer.toString(reader.getLineNumber()));
    }

    public static void failWithFullName(String key, XMLReader reader) {
        //throw new WebServicesClientException(key,
        //new Object[]{
        //  Integer.toString(reader.getLineNumber()),
        //  reader.getName().toString()});
    }

    public static void failWithLocalName(String key, XMLReader reader) {
        //throw new WebServicesClientException(key,
        //        new Object[]{
        //           Integer.toString(reader.getLineNumber()),
        //          reader.getLocalName()});
    }

    public static void failWithLocalName(String key, XMLReader reader,
        String arg) {
        //throw new WebServicesClientException(key,
        //      new Object[]{
        //          Integer.toString(reader.getLineNumber()),
        //          reader.getLocalName(),
        //          arg});
    }
}
