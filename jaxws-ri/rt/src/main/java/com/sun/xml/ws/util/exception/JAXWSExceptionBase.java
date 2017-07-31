/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.util.exception;

import com.sun.istack.localization.Localizable;
import com.sun.istack.localization.LocalizableMessage;
import com.sun.istack.localization.LocalizableMessageFactory;
import com.sun.istack.localization.Localizer;
import com.sun.istack.localization.NullLocalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.xml.ws.WebServiceException;

/**
 * Represents a {@link WebServiceException} with
 * localizable message.
 *
 * @author WS Development Team
 */
public abstract class JAXWSExceptionBase
    extends WebServiceException implements Localizable, LocalizableMessageFactory.ResourceBundleSupplier {

    //Don't worry about previous  serialVersionUID = 4818235090198755494L;, this class was not serializable before.
    private static final long serialVersionUID = 1L;

    private transient Localizable msg;

    /**
     * @param key
     * @param args
     * @deprecated Should use the localizable constructor instead.
     */
    @Deprecated
    protected JAXWSExceptionBase(String key, Object... args) {
        super(findNestedException(args));
        this.msg = new LocalizableMessage(getDefaultResourceBundleName(), this, key, args);
    }


    protected JAXWSExceptionBase(String message) {
        this(new NullLocalizable(message));
    }

    /**
     * Creates a new exception that wraps the specified exception.
     * @param throwable
     */
    protected JAXWSExceptionBase(Throwable throwable) {
        this(new NullLocalizable(throwable.toString()),throwable);
    }

    protected JAXWSExceptionBase(Localizable msg) {
        this.msg = msg;
    }

    protected JAXWSExceptionBase(Localizable msg, Throwable cause) {
        super(cause);
        this.msg = msg;
    }

    /**
     * @serialData Default fields,  followed by information in Localizable which comprises of.
     *  ResourceBundle name, then key and followed by arguments array.
     *  If there is no arguments array, then -1 is written.  If there is a argument array (possible of zero
     * length) then the array length is written as an integer, followed by each argument (Object).
     * If the Object is serializable, the argument is written. Otherwise the output of Object.toString()
     * is written.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // We have to call defaultWriteObject first.
        out.defaultWriteObject();

        out.writeObject(msg.getResourceBundleName());
        out.writeObject(msg.getKey());
        Object[] args = msg.getArguments();
        if (args == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(args.length);
        // Write Object values for the parameters, if it is serializable otherwise write String form of it..
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null || args[i] instanceof Serializable) {
                out.writeObject(args[i]);
            } else {
                out.writeObject(args[i].toString());
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // We have to call defaultReadObject first.
        in.defaultReadObject();
        Object[] args;
        String resourceBundleName = (String) in.readObject();
        String key = (String) in.readObject();
        int len = in.readInt();
        if (len == -1) {
            args = null;
        } else {
            args = new Object[len];
            for (int i = 0; i < args.length; i++) {
                args[i] = in.readObject();
            }
        }
        msg = new LocalizableMessageFactory(resourceBundleName).getMessage(key,args);
    }

    private static Throwable findNestedException(Object[] args) {
        if (args == null)
            return null;

        for( Object o : args )
            if(o instanceof Throwable)
                return (Throwable)o;
        return null;
    }

    @Override
    public String getMessage() {
        Localizer localizer = new Localizer();
        return localizer.localize(this);
    }

    /**
     * Gets the default resource bundle name for this kind of exception.
     * Used for {@link #JAXWSExceptionBase(String, Object[])}.
     * @return
     */
    protected abstract String getDefaultResourceBundleName();

//
// Localizable delegation
//
    @Override
    public final String getKey() {
        return msg.getKey();
    }

    @Override
    public final Object[] getArguments() {
        return msg.getArguments();
    }

    @Override
    public final String getResourceBundleName() {
        return msg.getResourceBundleName();
    }

    @Override
    public ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle(getDefaultResourceBundleName(), locale);
    }

}
