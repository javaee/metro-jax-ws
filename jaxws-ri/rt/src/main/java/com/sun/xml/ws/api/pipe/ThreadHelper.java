/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.api.pipe;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Simple utility class to instantiate correct Thread instance
 * depending on runtime context (jdk/non-jdk usage)
 *
 * @author miroslav.kos@oracle.com
 */
final class ThreadHelper {

    private static final String SAFE_THREAD_NAME = "sun.misc.ManagedLocalsThread";
    private static final Constructor THREAD_CONSTRUCTOR;

    // no instantiating wanted
    private ThreadHelper() {
    }

    static {
        THREAD_CONSTRUCTOR = AccessController.doPrivileged(
                new PrivilegedAction<Constructor> () {
                    @Override
                    public Constructor run() {
                        try {
                            Class cls = Class.forName(SAFE_THREAD_NAME);
                            if (cls != null) {
                                return cls.getConstructor(Runnable.class);
                            }
                        } catch (ClassNotFoundException ignored) {
                        } catch (NoSuchMethodException ignored) {
                        }
                        return null;
                    }
                }
        );
    }

    static Thread createNewThread(final Runnable r) {
        if (isJDKInternal()) {
            return AccessController.doPrivileged(
                    new PrivilegedAction<Thread>() {
                        @Override
                        public Thread run() {
                            try {
                                return (Thread) THREAD_CONSTRUCTOR.newInstance(r);
                            } catch (Exception e) {
                                return new Thread(r);
                            }
                        }
                    }
            );
        } else {
            return new Thread(r);
        }
    }

    private static boolean isJDKInternal() {
        String className = ThreadHelper.class.getName();
        return className.contains(".internal.");
    }
}
