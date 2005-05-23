/*
 * $Id: XMLReaderFactoryImpl.java,v 1.1 2005-05-23 22:59:36 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import java.io.InputStream;

import javax.xml.transform.Source;

import org.xml.sax.InputSource;

/**
 * <p> A concrete factory for XMLReader objects. </p>
 *
 * @author JAX-RPC Development Team
 */
public class XMLReaderFactoryImpl extends XMLReaderFactory {

    public XMLReaderFactoryImpl() {
    }

    public XMLReader createXMLReader(InputStream in) {
        return createXMLReader(in, false);
    }

    public XMLReader createXMLReader(InputSource source) {
        return createXMLReader(source, false);
    }

    public XMLReader createXMLReader(InputStream in, boolean rejectDTDs) {
        return createXMLReader(new InputSource(in), rejectDTDs);
    }

    public XMLReader createXMLReader(InputSource source, boolean rejectDTDs) {
        return new StAXReader(source, rejectDTDs);
    }

    public XMLReader createXMLReader(Source source, boolean rejectDTDs) {
        return new StAXReader(source, rejectDTDs);
    }
}
