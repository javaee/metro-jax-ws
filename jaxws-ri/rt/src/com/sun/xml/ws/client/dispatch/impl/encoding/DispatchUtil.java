/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.xml.ws.streaming.XMLStreamWriterFactory;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class DispatchUtil {

    private Map<String, String> namespacePrefixMap;

    public void clearNPMap() {
        namespacePrefixMap.clear();
    }

    public void populatePrefixes(XMLStreamWriter writer) {
        if (!namespacePrefixMap.isEmpty()) {
            Set<Map.Entry<String, String>> entrys = namespacePrefixMap.entrySet();
            for (Map.Entry<String, String> entry : entrys) {
                try {
                    writer.setPrefix(entry.getValue(), entry.getKey());
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void collectPrefixes(XMLStreamReader reader) {
        if (namespacePrefixMap == null)
            namespacePrefixMap = new HashMap<String, String>();

        int i = reader.getNamespaceCount();
        for (int j = 0; j < i; j++){
            String prefix = reader.getNamespacePrefix(j);
            String namespace = reader.getNamespaceURI(j);
            if (prefix.length() > 0 && namespace != null) {
                namespacePrefixMap.put(namespace, prefix);
            }
        }
    }
}

