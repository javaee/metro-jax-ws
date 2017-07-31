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

package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Rama Pulavarthi
 */
public class WSAsyncListener {
    final private WSHTTPConnection con;
    final private HttpAdapter.CompletionCallback callback;

    WSAsyncListener(WSHTTPConnection con, HttpAdapter.CompletionCallback callback) {
        this.con = con;
        this.callback = callback;
    }

    public void addListenerTo(AsyncContext context, final ServletAdapter.AsyncCompletionCheck completionCheck) {
        context.addListener(new AsyncListener() {

            public void onComplete(AsyncEvent event) throws IOException {
                LOGGER.finer("Asynchronous Servlet Invocation completed for "+((HttpServletRequest)event.getAsyncContext().getRequest()).getRequestURL());
                callback.onCompletion();

            }

            public void onTimeout(AsyncEvent event) throws IOException {
                completionCheck.markComplete();                                
                LOGGER.fine("Time out on Request:" + ((HttpServletRequest)event.getAsyncContext().getRequest()).getRequestURL());
                con.close();
            }

            public void onError(AsyncEvent event) throws IOException {
                LOGGER.fine("Error processing Request:" + ((HttpServletRequest)event.getAsyncContext().getRequest()).getRequestURL());
                con.close();
            }

            public void onStartAsync(AsyncEvent event) throws IOException {
                LOGGER.finer("Asynchronous Servlet Invocation started for "+((HttpServletRequest)event.getAsyncContext().getRequest()).getRequestURL());
            }
        });
    }
    private static final Logger LOGGER = Logger.getLogger(WSAsyncListener.class.getName());
}
