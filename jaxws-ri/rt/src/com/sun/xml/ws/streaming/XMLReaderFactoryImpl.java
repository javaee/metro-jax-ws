/*
 * $Id: XMLReaderFactoryImpl.java,v 1.2 2005-07-18 16:52:23 kohlert Exp $
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
 * @author WS Development Team
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
