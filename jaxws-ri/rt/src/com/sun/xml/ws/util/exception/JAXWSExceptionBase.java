/*
 * $Id: JAXWSExceptionBase.java,v 1.3 2005-09-10 19:48:17 kohsuke Exp $
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

package com.sun.xml.ws.util.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableSupport;
import com.sun.xml.ws.util.localization.Localizer;

/**
 * JAXWSExceptionBase
 * 
 * @author WS Development Team
 */
public abstract class JAXWSExceptionBase
    extends WebServiceException
    implements Localizable {
    protected LocalizableSupport localizablePart;
    protected NestableExceptionSupport nestablePart;

    public JAXWSExceptionBase() {
        nestablePart = new NestableExceptionSupport();
    }

    public JAXWSExceptionBase(String key) {
        this();
        localizablePart = new LocalizableSupport(key);
    }

    public JAXWSExceptionBase(String key, String arg) {
        this();
        localizablePart = new LocalizableSupport(key, arg);
    }

    public JAXWSExceptionBase(String key, Localizable localizable) {
        this(key, new Object[] { localizable });
    }

    protected JAXWSExceptionBase(String key, Object[] args) {
        this();
        localizablePart = new LocalizableSupport(key, args);
        if (args != null && nestablePart.getCause() == null) {
            for (int i = 0; i < args.length; ++i) {
                if (args[i] instanceof Throwable) {
                    nestablePart.setCause((Throwable) args[i]);
                    break;
                }
            }
        }
    }

    public String getKey() {
        return localizablePart.getKey();
    }

    public Object[] getArguments() {
        return localizablePart.getArguments();
    }

    public abstract String getResourceBundleName();

    public String toString() {
        // for debug purposes only
        //return getClass().getName() + " (" + getKey() + ")";
        return getMessage();
    }

    public String getMessage() {
        Localizer localizer = new Localizer();
        return localizer.localize(this);
    }

    public Throwable getLinkedException() {
        return nestablePart.getCause();
    }

    public void printStackTrace() {
        super.printStackTrace();
        nestablePart.printStackTrace();
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        nestablePart.printStackTrace(s);
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        nestablePart.printStackTrace(s);
    }
}
