/*
 * $Id: NullEntityResolver.java,v 1.2 2005-07-18 18:14:08 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.util.xml;

import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author WS Development Team
 */
public class NullEntityResolver implements EntityResolver {

    public NullEntityResolver() {
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        // always resolve to an empty document
        return new InputSource(new StringReader(""));
    }
}
