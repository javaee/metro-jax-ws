/*
 * $Id: XMLReaderBase.java,v 1.2 2005-07-18 16:52:23 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

/**
 * <p> A base class for XMLReader implementations. </p>
 *
 * <p> It provides the implementation of some derived XMLReader methods. </p>
 *
 * @author WS Development Team
 */
public abstract class XMLReaderBase implements XMLReader {

    public int nextContent() {
        for (;;) {
            int state = next();
            switch (state) {
                case START :
                case END :
                case EOF :
                    return state;
                case CHARS :
                    if (getValue().trim().length() != 0) {
                        return CHARS;
                    }
                    continue;
                case PI :
                    continue;
            }
        }
    }

    public int nextElementContent() {
        int state = nextContent();
        if (state == CHARS) {
            throw new XMLReaderException(
                "xmlreader.unexpectedCharacterContent",
                getValue());
        }
        return state;
    }

    public void skipElement() {
        skipElement(getElementId());
    }
    public abstract void skipElement(int elementId);
}
