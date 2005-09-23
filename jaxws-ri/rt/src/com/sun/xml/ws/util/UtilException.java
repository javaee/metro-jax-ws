/*
 * $Id: UtilException.java,v 1.5 2005-09-23 22:05:39 kohsuke Exp $
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

package com.sun.xml.ws.util;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * UtilException represents an exception that occurred while
 * one of the util classes is operating.
 * 
 * @see JAXWSExceptionBase
 * 
 * @author JAX-WS Development Team
 */
public class UtilException extends JAXWSExceptionBase {
    public UtilException(String key, Object... args) {
        super(key, args);
    }

    public UtilException(Throwable throwable) {
        super(throwable);
    }

    public UtilException(Localizable arg) {
        super("nestedUtilError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.util";
    }

}
