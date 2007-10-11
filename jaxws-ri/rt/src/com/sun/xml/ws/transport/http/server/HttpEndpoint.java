/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.transport.http.server;

import com.sun.net.httpserver.HttpContext;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.HttpAdapterList;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.server.WSEndpointImpl;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.EndpointReference;
import java.util.concurrent.Executor;
import java.io.IOException;

import org.w3c.dom.Element;

/**
 * Hides {@link HttpContext} so that {@link EndpointImpl}
 * may load even without {@link HttpContext}.
 *
 * TODO: But what's the point? If Light-weight HTTP server isn't present,
 * all the publish operations will fail way. Why is it better to defer
 * the failure, as opposed to cause the failure as earyl as possible? -KK
 *
 * @author Jitendra Kotamraju
 */
public final class HttpEndpoint extends com.sun.xml.ws.api.server.HttpEndpoint {
    private String address;
    private HttpContext httpContext;
    private final HttpAdapter adapter;
    private final Executor executor;

    public HttpEndpoint(WSEndpoint endpoint, Executor executor) {
        this.executor = executor;
        this.adapter = HttpAdapter.createAlone(endpoint);
    }

    public void publish(String address) {
        this.address = address;
        httpContext = ServerMgr.getInstance().createContext(address);
        publish(httpContext);
    }

    public void publish(Object serverContext) {
        if (!(serverContext instanceof HttpContext)) {
            throw new ServerRtException("not.HttpContext.type", serverContext.getClass());
        }

        this.httpContext = (HttpContext)serverContext;
        publish(httpContext);
    }

    /**
     * This can be called only after publish
     * @return address of the Endpoint
     */
    private String getEPRAddress() {
        if(address == null) {
            // Application created its own HttpContext
            return httpContext.getServer().getAddress().toString();
        } else
            return address;
    }

    public void stop() {
        if (address == null) {
            // Application created its own HttpContext
            // httpContext.setHandler(null);
            httpContext.getServer().removeContext(httpContext);
        } else {
            // Remove HttpContext created by JAXWS runtime 
            ServerMgr.getInstance().removeContext(httpContext);
        }

        // Invoke WebService Life cycle method
        adapter.getEndpoint().dispose();
    }

    private void publish (HttpContext context) {
        context.setHandler(new WSHttpHandler(adapter, executor));
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element...referenceParameters) {
        WSEndpointImpl endpointImpl = (WSEndpointImpl) adapter.getEndpoint();
        String eprAddress = getEPRAddress();
        return clazz.cast(endpointImpl.getEndpointReference(clazz, eprAddress,eprAddress+"?wsdl", referenceParameters));
    }

}
