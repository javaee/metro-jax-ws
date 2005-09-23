/*
 * $Id: SerializationException.java,v 1.6 2005-09-23 22:05:28 kohsuke Exp $
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

package com.sun.xml.ws.encoding.soap;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * SerializationException represents an exception that occurred while
 * serializing a Java value as XML.
 * 
 * @see JAXWSExceptionBase
 * 
 * @author WS Development Team
 */
public class SerializationException extends JAXWSExceptionBase {

    public SerializationException(String key, Object... args) {
        super(key, args);
    }

    public SerializationException(Localizable arg) {
        super("nestedSerializationError", arg);
    }

    public SerializationException(Throwable throwable) {
        super(throwable);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.encoding";
    }

}
