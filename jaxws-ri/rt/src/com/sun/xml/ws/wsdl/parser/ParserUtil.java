/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
import javax.xml.stream.XMLStreamReader;


/**
 * @author WS Development Team
 */
public class ParserUtil {
    public static String getAttribute(XMLStreamReader reader, String name) {
        return reader.getAttributeValue(null, name);
    }

    public static void verifyTag(XMLReader reader, QName name) {
        if (!name.equals(reader.getName())) {
            throw new XMLReaderException("xmlreader.unexpectedState.tag",
                new Object[] { name, reader.getName() });
        }
    }

    public static QName getQName(XMLStreamReader reader, String tag){
        String localName = XmlUtil.getLocalPart(tag);
        String pfix = XmlUtil.getPrefix(tag);
        String uri = reader.getNamespaceURI(pfix);
        return new QName(uri, localName);
    }

    public static String getMandatoryNonEmptyAttribute(XMLStreamReader reader,
        String name) {
//        String value = getAttribute(reader, name);
        String value = reader.getAttributeValue(null, name);

        if (value == null) {
            failWithLocalName("client.missing.attribute", reader, name);
        } else if (value.equals("")) {
            failWithLocalName("client.invalidAttributeValue", reader, name);
        }

        return value;
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

    public static void failWithFullName(String key, XMLStreamReader reader) {
//        throw new WebServicesClientException(key,
//        new Object[]{
//          Integer.toString(reader.getLineNumber()),
//          reader.getName().toString()});
    }

    public static void failWithLocalName(String key, XMLStreamReader reader) {
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

    public static void failWithLocalName(String key, XMLStreamReader reader,
        String arg) {
        //throw new WebServicesClientException(key,
        //      new Object[]{
        //          Integer.toString(reader.getLineNumber()),
        //          reader.getLocalName(),
        //          arg});
    }
}
