/*
 * $Id: XMLReaderBase.java,v 1.3 2005-09-10 19:48:03 kohsuke Exp $
 */

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
