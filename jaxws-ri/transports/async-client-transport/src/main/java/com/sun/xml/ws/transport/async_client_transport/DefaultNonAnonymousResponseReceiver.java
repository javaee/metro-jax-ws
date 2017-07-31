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

package com.sun.xml.ws.transport.async_client_transport;

import com.sun.xml.ws.api.message.Message;
import java.util.logging.Level;
import javax.xml.ws.*;
import java.util.logging.Logger;

/**
 * @author Rama.Pulavarthi@sun.com
 */
public class DefaultNonAnonymousResponseReceiver implements NonAnonymousResponsesReceiver<Message>{
    private Endpoint e;
    private String bindingId;
    private String nonanonAddress;
    public DefaultNonAnonymousResponseReceiver(String nonanonAddress, String bindingId) {
        this.bindingId = bindingId;
        this.nonanonAddress = nonanonAddress;
    }

    @Override
    public void register(NonAnonymousResponseHandler<Message> nonAnonymousResponseHandler) {
        e = Endpoint.create(bindingId, new DefaultNonAnonymousEndpoint(nonAnonymousResponseHandler));
        if(nonanonAddress == null) {
            nonanonAddress = NonAnonymousAddressAllocator.getInstance().createNonAnonymousAddress();

        }
        LOGGER.log(Level.INFO, "Starting NonAnonymousResponseReceiver on:{0}", nonanonAddress);
        try {
        e.publish(nonanonAddress);
        } catch (Exception ex) {
//           ex.printStackTrace();
           throw new WebServiceException(ex);
        }
    }

    @Override
    public void unregister(NonAnonymousResponseHandler<Message> nonAnonymousResponseHandler) {
        if (e != null) {
            e.stop();
        }
    }

    @Override
    public String getAddress() {
        return nonanonAddress;
    }

    @ServiceMode(value= Service.Mode.MESSAGE)
    @WebServiceProvider(serviceName ="RINonAnonService", portName ="RINonAnonPort",targetNamespace ="http://jax-ws//foo")
    private static class DefaultNonAnonymousEndpoint implements Provider<Message> {
        private NonAnonymousResponseHandler<Message> handler;

        private DefaultNonAnonymousEndpoint(NonAnonymousResponseHandler<Message> handler) {
            this.handler = handler;
        }

        @Override
        public Message invoke(Message m) {
            LOGGER.log(Level.FINE, "Message receieved by{0}", this.getClass());
            if (handler != null) {
                handler.onReceive(m);
            }
            return null;
        }
    }
    private static final Logger LOGGER = Logger.getLogger(DefaultNonAnonymousResponseReceiver.class.getName());
}
