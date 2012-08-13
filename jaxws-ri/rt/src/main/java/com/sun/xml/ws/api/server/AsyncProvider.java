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

package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;

import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import java.util.concurrent.Executor;

/**
 * Asynchronous version of {@link Provider}.
 *
 * <p>
 * Applications that use the JAX-WS RI can implement this interface instead of
 * {@link Provider} to implement asynchronous web services (AWS.) AWS enables
 * applications to perform operations with long latency without blocking a thread,
 * and thus particularly suitable for highly scalable service implementation,
 * at the expesnce of implementation complexity. 
 *
 * <h2>Programming Model</h2>
 * <p>
 * Whenever a new reuqest arrives, the JAX-WS RI invokes the {@link #invoke} method
 * to notify the application. Normally, the application then schedules an execution
 * of this request, and exit from this method immediately (the point of AWS is not
 * to use this calling thread for request processing.)
 *
 * <p>
 * Unlike the synchronous version, which requires the response to be given as the return value,
 * with AWS the JAX-WS RI will keep the connection with client open, until the application
 * eventually notifies the JAX-WS RI via {@link AsyncProviderCallback}. When that
 * happens that causes the JAX-WS RI to send back a response to the client.
 *
 * <p>
 * The following code shows a very simple AWS example:
 *
 * <pre>
 * &#64;WebService
 * class MyAsyncEchoService implements AsyncProvider&lt;Source> {
 *     private static final {@link Executor} exec = ...;
 *
 *     public void invoke( final Source request, final AsyncProviderCallback&lt;Source> callback, final WebServiceContext context) {
 *         exec.execute(new {@link Runnable}() {
 *             public void run() {
 *                 Thread.sleep(1000);     // kill time.
 *                 callback.send(request); // just echo back
 *             }
 *         });
 *     }
 * }
 * </pre>
 *
 * <p>
 * Please also check the {@link Provider} and its programming model for general
 * provider programming model.
 *
 *
 * <h2>WebServiceContext</h2>
 * <p>
 * In synchronous web services, the injected {@link WebServiceContext} instance uses
 * the calling {@link Thread} to determine which request it should return information about.
 * This no longer works with AWS, as you may need to call {@link WebServiceContext}
 * much later, possibly from entirely different thread.
 *
 * <p>
 * For this reason, {@link AsyncProvider} passes in {@link WebServiceContext} as
 * a parameter. This object remains usable until you invoke {@link AsyncProviderCallback},
 * and it can be invoked from any thread, even concurrently. AWS must not use the injected
 * {@link WebServiceContext}, as its behavior is undefined.
 *
 * @see Provider
 * @author Jitendra Kotamraju
 * @author Kohsuke Kawaguchi
 * @since 2.1
 */
public interface AsyncProvider<T> {
    /**
     * Schedules an execution of a request.
     *
     * @param request
     *      Represents the request message or payload.
     * @param callback
     *      Application must notify this callback interface when the processing
     *      of a request is complete.
     * @param context
     *      The web service context instance that can be used to retrieve
     *      context information about the given request.
     */
    public void invoke(
        @NotNull T request,
        @NotNull AsyncProviderCallback<T> callback,
        @NotNull WebServiceContext context);
}
