/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Interception for {@link Fiber} context switch.
 *
 * <p>
 * Even though pipeline runs asynchronously, sometimes it's desirable
 * to bind some state to the current thread running a fiber. Such state
 * may include security subject (in terms of {@link AccessController#doPrivileged}),
 * or a transaction.
 *
 * <p>
 * This mechanism makes it possible to do such things, by allowing
 * some code to be executed before and after a thread executes a fiber.
 *
 * <p>
 * The design also encapsulates the entire fiber execution in a single
 * opaque method invocation {@link Work#execute}, allowing the use of
 * <tt>finally</tt> block. 
 *
 *
 * @author Kohsuke Kawaguchi
 */
public interface FiberContextSwitchInterceptor {
    /**
     * Allows the interception of the fiber execution.
     *
     * <p>
     * This method needs to be implemented like this:
     *
     * <pre>
     * &lt;R,P> R execute( Fiber f, P p, Work&lt;R,P> work ) {
     *   // do some preparation work
     *   ...
     *   try {
     *     // invoke
     *     return work.execute(p);
     *   } finally {
     *     // do some clean up work
     *     ...
     *   }
     * }
     * </pre>
     *
     * <p>
     * While somewhat unintuitive,
     * this interception mechanism enables the interceptor to wrap
     * the whole fiber execution into a {@link AccessController#doPrivileged(PrivilegedAction)},
     * for example.
     *
     * @param f
     *      {@link Fiber} to be executed.
     * @param p
     *      The opaque parameter value for {@link Work}. Simply pass this value to
     *      {@link Work#execute(Object)}.
     * @return
     *      The opaque return value from the the {@link Work}. Simply return
     *      the value from {@link Work#execute(Object)}.
     */
    <R,P> R execute( Fiber f, P p, Work<R,P> work );

    /**
     * Abstraction of the execution that happens inside the interceptor.
     */
    interface Work<R,P> {
        /**
         * Have the current thread executes the current fiber,
         * and returns when it stops doing so.
         *
         * <p>
         * The parameter and the return value is controlled by the
         * JAX-WS runtime, and interceptors should simply treat
         * them as opaque values.
         */
        R execute(P param);
    }
}
