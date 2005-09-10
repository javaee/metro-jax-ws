/*
 * $Id: CDATA.java,v 1.3 2005-09-10 19:48:19 kohsuke Exp $
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

package com.sun.xml.ws.util.xml;

/**
 * @author WS Development Team
 */
public final class CDATA {

    public CDATA(String text) {
        _text = text;
    }

    public String getText() {
        return _text;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof CDATA))
            return false;

        CDATA cdata = (CDATA) obj;

        return this._text.equals(cdata._text);
    }

    public int hashCode() {
        return _text.hashCode();
    }

    private String _text;
}
