/*
 * $Id: Localizer.java,v 1.1 2005-05-23 23:05:07 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.localization;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

/**
 * @author JAX-RPC Development Team
 */
public class Localizer {

    protected Locale _locale;
    protected HashMap _resourceBundles;

    public Localizer() {
        this(Locale.getDefault());
    }

    public Localizer(Locale l) {
        _locale = l;
        _resourceBundles = new HashMap();
    }

    public Locale getLocale() {
        return _locale;
    }

    public String localize(Localizable l) {
        String bundlename = l.getResourceBundleName();

        try {
            ResourceBundle bundle =
                (ResourceBundle) _resourceBundles.get(bundlename);

            if (bundle == null) {
                try {
                    bundle = ResourceBundle.getBundle(bundlename, _locale);
                } catch (MissingResourceException e) {
                    // work around a bug in the com.sun.enterprise.deployment.WebBundleArchivist:
                    //   all files with an extension different from .class (hence all the .properties files)
                    //   get copied to the top level directory instead of being in the package where they
                    //   are defined
                    // so, since we can't find the bundle under its proper name, we look for it under
                    //   the top-level package

                    int i = bundlename.lastIndexOf('.');
                    if (i != -1) {
                        String alternateBundleName =
                            bundlename.substring(i + 1);
                        try {
                            bundle =
                                ResourceBundle.getBundle(
                                    alternateBundleName,
                                    _locale);
                        } catch (MissingResourceException e2) {
                            // give up
                            return getDefaultMessage(l);
                        }
                    }
                }

                _resourceBundles.put(bundlename, bundle);
            }

            if (bundle == null) {
                return getDefaultMessage(l);
            }

            String key = l.getKey();
            if (key == null)
                key = "undefined";

            String msg = null;
            try {
                msg = bundle.getString(key);
            } catch (MissingResourceException e) {
                // notice that this may throw a MissingResourceException of its own (caught below)
                msg = bundle.getString("undefined");
            }

            // localize all arguments to the given localizable object
            Object[] args = l.getArguments();
            if (args != null) {
                for (int i = 0; i < args.length; ++i) {
                    if (args[i] instanceof Localizable)
                        args[i] = localize((Localizable) args[i]);
                }
            }

            String message = MessageFormat.format(msg, args);
            return message;

        } catch (MissingResourceException e) {
            return getDefaultMessage(l);
        }

    }

    protected String getDefaultMessage(Localizable l) {
        String key = l.getKey();
        Object[] args = l.getArguments();
        StringBuffer sb = new StringBuffer();
        if (!(l instanceof LocalizableExceptionAdapter)) {
            // avoid to point out the failure to localize an exception that cannot possibly be localized
            sb.append("[failed to localize] ");
        }
        sb.append(String.valueOf(key));
        if (args != null) {
            sb.append('(');
            for (int i = 0; i < args.length; ++i) {
                if (i != 0)
                    sb.append(", ");
                sb.append(String.valueOf(args[i]));
            }
            sb.append(')');
        }
        return sb.toString();
    }

}
