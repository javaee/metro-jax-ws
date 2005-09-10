/*
 * $Id: LocalizableExceptionAdapter.java,v 1.3 2005-09-10 19:48:17 kohsuke Exp $
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

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.Localizer;
import com.sun.xml.ws.util.localization.NullLocalizable;

/**
 * LocalizableExceptionAdapter
 *
 * @author WS Development Team
 */
public class LocalizableExceptionAdapter
    extends Exception
    implements Localizable {
    protected Localizable localizablePart;
    protected Throwable nestedException;

    public LocalizableExceptionAdapter(Throwable nestedException) {
        this.nestedException = nestedException;
        if (nestedException instanceof Localizable) {
            localizablePart = (Localizable) nestedException;
        } else {
            localizablePart = new NullLocalizable(nestedException.toString());
        }
    }

    public String getKey() {
        return localizablePart.getKey();
    }

    public Object[] getArguments() {
        return localizablePart.getArguments();
    }

    public String getResourceBundleName() {
        return localizablePart.getResourceBundleName();
    }

    public String toString() {
        // for debug purposes only
        return nestedException.toString();
    }

    public String getLocalizedMessage() {
        if (nestedException == localizablePart) {
            Localizer localizer = new Localizer();
            return localizer.localize(localizablePart);
        } else {
            return nestedException.getLocalizedMessage();
        }
    }

    public String getMessage() {
        return getLocalizedMessage();
    }

    public Throwable getNestedException() {
        return nestedException;
    }

    public void printStackTrace() {
        nestedException.printStackTrace();
    }

    public void printStackTrace(PrintStream s) {
        nestedException.printStackTrace(s);
    }

    public void printStackTrace(PrintWriter s) {
        nestedException.printStackTrace(s);
    }
}
