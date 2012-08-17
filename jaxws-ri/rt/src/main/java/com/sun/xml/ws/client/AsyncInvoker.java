/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.client;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber.CompletionCallback;
import com.sun.xml.ws.api.pipe.Tube;

import javax.xml.ws.WebServiceException;

/**
 * Invokes {@link Tube}line asynchronously for the client's async API(for e.g.: Dispatch#invokeAsync}
 * The concrete classes need to call {@link Stub#processAsync(AsyncResponseImpl, Packet, RequestContext, Fiber.CompletionCallback) } in
 * run() method.
 *
 * @author Jitendra Kotamraju
 */
public abstract class AsyncInvoker implements Runnable {
    /**
     * Because of the object instantiation order,
     * we can't take this as a constructor parameter.
     */
    protected AsyncResponseImpl responseImpl;
    protected boolean nonNullAsyncHandlerGiven;
    
    public void setReceiver(AsyncResponseImpl responseImpl) {
        this.responseImpl = responseImpl;
    }

  public AsyncResponseImpl getResponseImpl() {
    return responseImpl;
  }

  public void setResponseImpl(AsyncResponseImpl responseImpl) {
    this.responseImpl = responseImpl;
  }

  public boolean isNonNullAsyncHandlerGiven() {
    return nonNullAsyncHandlerGiven;
  }

  public void setNonNullAsyncHandlerGiven(boolean nonNullAsyncHandlerGiven) {
    this.nonNullAsyncHandlerGiven = nonNullAsyncHandlerGiven;
  }

  public void run () {
        try {
            do_run();
        }catch(WebServiceException e) {
            throw e;
        }catch(Throwable t) {
            //Wrap it in WebServiceException
            throw new WebServiceException(t);
        }
    }

    public abstract void do_run();
}
