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

package com.sun.tools.ws.processor.config.parser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import com.sun.tools.ws.processor.config.ConfigurationException;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.util.xml.XmlUtil;

/**
 *
 * @author WS Development Team
 */
public class ParserUtil {
    public static void failWithFullName(String key, String file, XMLReader reader) {
        throw new ConfigurationException(key, new Object[] { file, Integer.toString(
            reader.getLineNumber()), reader.getName().toString() });
    }

    public static void failWithFullName(String key, String file, XMLStreamReader reader) {
        throw new ConfigurationException(key, new Object[] { file, Integer.toString(
            reader.getLocation().getLineNumber()), reader.getName().toString() });
    }
}
