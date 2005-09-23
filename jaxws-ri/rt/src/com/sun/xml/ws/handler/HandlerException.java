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

package com.sun.xml.ws.handler;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * Exception thrown by handler-related code. Extends
 * {@link com.sun.xml.ws.util.exception.JAXWSExceptionBase}
 * using the appropriate resource bundle.
 *
 * @see com.sun.xml.ws.util.exception.JAXWSExceptionBase
 *
 * @author WS Development Team
 */
public class HandlerException extends JAXWSExceptionBase {
    public HandlerException(String key, Object... args) {
        super(key, args);
    }

    public HandlerException(Throwable throwable) {
        super(throwable);
    }

    public HandlerException(Localizable arg) {
        super("handler.nestedError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.handler";
    }
}
