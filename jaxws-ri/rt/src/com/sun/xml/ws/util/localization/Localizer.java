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

package com.sun.xml.ws.util.localization;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Localizes the {@link Localizable} into a message
 * by using a configured {@link Locale}.
 *
 * @author WS Development Team
 */
public class Localizer {

    private final Locale _locale;
    private final HashMap _resourceBundles;

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
            if(key==Localizable.NOT_LOCALIZABLE) {
                // this message is not localizable
                return (String)l.getArguments()[0];
            }


            if (key == null)
                key = "undefined";

            String msg;
            try {
                msg = bundle.getString(key);
            } catch (MissingResourceException e) {
                // notice that this may throw a MissingResourceException of its own (caught below)
                msg = bundle.getString("undefined");
            }

            // localize all arguments to the given localizable object
            Object[] args = l.getArguments();
            for (int i = 0; i < args.length; ++i) {
                if (args[i] instanceof Localizable)
                    args[i] = localize((Localizable) args[i]);
            }

            String message = MessageFormat.format(msg, args);
            return message;

        } catch (MissingResourceException e) {
            return getDefaultMessage(l);
        }

    }

    private String getDefaultMessage(Localizable l) {
        String key = l.getKey();
        Object[] args = l.getArguments();
        StringBuilder sb = new StringBuilder();
        sb.append("[failed to localize] ");
        sb.append(key);
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
