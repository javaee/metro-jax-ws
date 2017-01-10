/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
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
import java.util.concurrent.ThreadFactory;

/**
 * Simple utility class to instantiate correct Thread instance
 * depending on Java version.
 *
 * @author miroslav.kos@oracle.com
 */
final class ThreadHelper {

    private static final String SAFE_THREAD_NAME = "sun.misc.ManagedLocalsThread";

    private static final ThreadFactory threadFactory;

    // no instantiating wanted
    private ThreadHelper() {
    }

    static {
        threadFactory = AccessController.doPrivileged(
                new PrivilegedAction<ThreadFactory> () {
                    @Override
                    public ThreadFactory run() {
                        // In order of preference
                        try {
                            try {
                                Class<Thread> cls = Thread.class;
                                Constructor<Thread> ctr = cls.getConstructor(
                                        ThreadGroup.class,
                                        Runnable.class,
                                        String.class,
                                        long.class,
                                        boolean.class);
                                return new JDK9ThreadFactory(ctr);
                            } catch (NoSuchMethodException ignored) {
                                // constructor newly added in Java SE 9
                            }
                            Class<?> cls = Class.forName(SAFE_THREAD_NAME);
                            Constructor<?> ctr = cls.getConstructor(Runnable.class);
                            return new SunMiscThreadFactory(ctr);
                        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}
                        return new LegacyThreadFactory();
                    }
                }
        );
    }

    static Thread createNewThread(final Runnable r) {
        return threadFactory.newThread(r);
    }

    // A Thread factory backed by the Thread constructor that
    // suppresses inheriting of inheritable thread-locals.
    private static class JDK9ThreadFactory implements ThreadFactory {
        final Constructor<Thread> ctr;
        JDK9ThreadFactory(Constructor<Thread> ctr) { this.ctr = ctr; }
        @Override public Thread newThread(Runnable r) {
            try {
                return ctr.newInstance(null, r, "toBeReplaced", 0, false);
            } catch (ReflectiveOperationException x) {
                InternalError ie = new InternalError(x.getMessage());
                ie.initCause(ie);
                throw ie;
            }
        }
    }

    // A Thread factory backed by sun.misc.ManagedLocalsThread
    private static class SunMiscThreadFactory implements ThreadFactory {
        final Constructor<?> ctr;
        SunMiscThreadFactory(Constructor<?> ctr) { this.ctr = ctr; }
        @Override public Thread newThread(final Runnable r) {
            return AccessController.doPrivileged(
                    new PrivilegedAction<Thread>() {
                        @Override
                        public Thread run() {
                            try {
                                return (Thread) ctr.newInstance(r);
                            } catch (Exception e) {
                                return new Thread(r);
                            }
                        }
                    }
            );
        }
    }

    // A Thread factory backed by new Thread(Runnable)
    private static class LegacyThreadFactory implements ThreadFactory {
        @Override public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    }
}
