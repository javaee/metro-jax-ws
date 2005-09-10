/*
 * $Id: NestableExceptionSupport.java,v 1.3 2005-09-10 19:48:17 kohsuke Exp $
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

/**
 * NestableExceptionSupport
 *
 * @author WS Development Team
 */

public class NestableExceptionSupport {
    protected Throwable cause = null;

    public NestableExceptionSupport() {
    }

    public NestableExceptionSupport(Throwable cause) {
        this.cause = cause;
    }

    public void printStackTrace() {
        //super.printStackTrace();
        if (cause != null) {
            System.err.println("\nCAUSE:\n");
            cause.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream s) {
        //super.printStackTrace(s);
        if (cause != null) {
            s.println("\nCAUSE:\n");
            cause.printStackTrace(s);
        }
    }

    public void printStackTrace(PrintWriter s) {
        //super.printStackTrace(s);
        if (cause != null) {
            s.println("\nCAUSE:\n");
            cause.printStackTrace(s);
        }
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}
