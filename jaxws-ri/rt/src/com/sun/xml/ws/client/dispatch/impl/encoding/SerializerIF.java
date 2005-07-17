/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client.dispatch.impl.encoding;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.bind.JAXBContext;

public interface SerializerIF {
     public void serialize(Object obj, XMLStreamWriter writer, JAXBContext context);
     public Object deserialize(XMLStreamReader reader, JAXBContext context);
}
