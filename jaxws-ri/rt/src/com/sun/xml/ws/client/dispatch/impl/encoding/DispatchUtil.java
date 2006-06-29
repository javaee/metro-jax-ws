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

