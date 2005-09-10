/*
 * $Id: XMLReaderUtil.java,v 1.3 2005-09-10 19:48:04 kohsuke Exp $
 */

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

import javax.xml.namespace.QName;

import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.util.xml.XmlUtil;

/**
 * <p> XMLReaderUtil provides some utility methods intended to be used
 * in conjunction with a XMLReader. </p>
 *
 * @see XMLReader
 *
 * @author WS Development Team
 */
public class XMLReaderUtil {

    private XMLReaderUtil() {
    }

    // sample method signature:
    // public static void foo(XMLReader reader, args...);
    //

    public static QName getQNameValue(XMLReader reader, QName attributeName) {
        String attribute = reader.getAttributes().getValue(attributeName);
        return attribute == null ? null : decodeQName(reader, attribute);
    }

    public static QName decodeQName(XMLReader reader, String rawName) {
        // NOTE: Here it is assumed that we do not want to use default namespace
        // declarations and therefore a null prefix means "no namespace" and
        // not "default namespace"

        String prefix = XmlUtil.getPrefix(rawName);
        String local = XmlUtil.getLocalPart(rawName);
        String uri = ((prefix == null) ? null : reader.getURI(prefix));
        return new QName(uri, local);
    }

    public static void verifyReaderState(XMLReader reader, int expectedState) {
        if (reader.getState() != expectedState) {
            throw new XMLReaderException(
                "xmlreader.unexpectedState",
                new Object[] {
                    getStateName(expectedState),
                    getLongStateName(reader)});
        }
    }

    public static void verifyTag(XMLReader reader, QName name) {
        if (!name.equals(reader.getName())) {
            throw new XMLReaderException(
                "xmlreader.unexpectedState.tag",
                new Object[] {
                    name,
                    reader.getName()});
        }
    }

    public static String getStateName(XMLReader reader) {
        return getStateName(reader.getState());
    }

    public static String getLongStateName(XMLReader reader) {
        int state = reader.getState();
        String name = getStateName(state);
        if (state == XMLReader.START || state == XMLReader.START) {
            name += ": " + reader.getName();
        }
        return name;
    }

    public static String getStateName(int state) {
        switch (state) {
            case XMLReader.BOF :
                return "BOF";
            case XMLReader.START :
                return "START";
            case XMLReader.END :
                return "END";
            case XMLReader.CHARS :
                return "CHARS";
            case XMLReader.PI :
                return "PI";
            case XMLReader.EOF :
                return "EOF";
            default :
                return "UNKNOWN";
        }
    }

}
