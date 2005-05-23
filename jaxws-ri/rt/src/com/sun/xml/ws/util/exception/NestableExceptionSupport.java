/*
 * $Id: NestableExceptionSupport.java,v 1.1 2005-05-23 23:05:06 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * NestableExceptionSupport
 *
 * @author JAX-RPC Development Team
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
