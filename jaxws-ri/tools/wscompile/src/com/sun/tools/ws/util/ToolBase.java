/*
 * $Id: ToolBase.java,v 1.2 2005-07-18 18:14:08 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.util;

import java.io.OutputStream;
import java.io.PrintStream;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

/**
 * A base class for command-line tools.
 *
 * @author WS Development Team
 */
public abstract class ToolBase {

    public ToolBase(OutputStream out, String program) {
        this.out = out;
        this.program = program;
        initialize();
    }

    protected void initialize() {
        messageFactory = new LocalizableMessageFactory(getResourceBundleName());
        localizer = new Localizer();
    }

    public boolean run(String[] args) {
        if (!parseArguments(args)) {
            return false;
        }

        try {
            run();
            return wasSuccessful();
        } catch (Exception e) {
            if (e instanceof Localizable) {
                report((Localizable) e);
            } else {
                report(getMessage(getGenericErrorMessage(), e.toString()));
            }
            printStackTrace(e);
            return false;
        }
    }

    public boolean wasSuccessful() {
        return true;
    }

    protected abstract boolean parseArguments(String[] args);
    protected abstract void run() throws Exception;
    public void runProcessorActions() {}
    protected abstract String getGenericErrorMessage();
    protected abstract String getResourceBundleName();

    public void printStackTrace(Throwable t) {
        PrintStream outstream =
                out instanceof PrintStream
                        ? (PrintStream) out
                        : new PrintStream(out, true);
        t.printStackTrace(outstream);
        outstream.flush();
    }

    protected void report(String msg) {
        PrintStream outstream =
                out instanceof PrintStream
                        ? (PrintStream) out
                        : new PrintStream(out, true);
        outstream.println(msg);
        outstream.flush();
    }

    protected void report(Localizable msg) {
        report(localizer.localize(msg));
    }

    public Localizable getMessage(String key) {
        return getMessage(key, (Object[]) null);
    }

    public Localizable getMessage(String key, String arg) {
        return messageFactory.getMessage(key, new Object[] { arg });
    }

    public Localizable getMessage(String key, String arg1, String arg2) {
        return messageFactory.getMessage(key, new Object[] { arg1, arg2 });
    }

    public Localizable getMessage(
        String key,
        String arg1,
        String arg2,
        String arg3) {
        return messageFactory.getMessage(
                key,
                new Object[] { arg1, arg2, arg3 });
    }

    public Localizable getMessage(String key, Localizable localizable) {
        return messageFactory.getMessage(key, new Object[] { localizable });
    }

    public Localizable getMessage(String key, Object[] args) {
        return messageFactory.getMessage(key, args);
    }

    protected OutputStream out;
    protected String program;
    protected Localizer localizer;
    protected LocalizableMessageFactory messageFactory;

    protected final static String TRUE = "true";
    protected final static String FALSE = "false";

}
