/*
 * $Id: ParserUtil.java,v 1.4 2005-08-05 23:51:50 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
