/**
 * $Id: ExternalObject.java,v 1.1 2005-05-23 23:15:59 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model.exporter;

import org.xml.sax.ContentHandler;

/**
 * "Opaque" object in the object graph that knows how
 * to persist itself to XML.
 *
 * TODO: ExternalObjectReader
 *
 */
public interface ExternalObject {
    /**
     * Type name of this object. This will be used
     * when loading the object back from XML.
     */
    String getType();

    /**
     * Saves the object into XML.
     */
    void saveTo(ContentHandler receiver);
}
