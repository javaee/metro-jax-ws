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

package com.sun.xml.ws.wsdl.parser;


import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLReaderException;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;


/**
 *
 * TODO: made public just for now
 * @author WS Development Team
 */
public class ParserUtil {
    public static String getAttribute(XMLStreamReader reader, String name) {
        return reader.getAttributeValue(null, name);
    }

    public static String getAttribute(XMLStreamReader reader, String nsUri, String name) {
        return reader.getAttributeValue(nsUri, name);
    }

    public static String getAttribute(XMLStreamReader reader, QName name) {
        return reader.getAttributeValue(name.getNamespaceURI(), name.getLocalPart());
    }

    public static QName getQName(XMLStreamReader reader, String tag){
        String localName = XmlUtil.getLocalPart(tag);
        String pfix = XmlUtil.getPrefix(tag);
        String uri = reader.getNamespaceURI(fixNull(pfix));
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

    public static void failWithLocalName(String key, XMLStreamReader reader,
        String arg) {
        //throw new WebServicesClientException(key,
        //      new Object[]{
        //          Integer.toString(reader.getLineNumber()),
        //          reader.getLocalName(),
        //          arg});
    }

    private static @NotNull String fixNull(@Nullable String s) {
        if (s == null) return "";
        else return s;
    }
}
