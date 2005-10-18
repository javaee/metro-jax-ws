/*
 * $Id: WSRtObjectFactoryImpl.java,v 1.9 2005-10-18 22:16:55 jitu Exp $
 */

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
package com.sun.xml.ws.util;

import com.sun.xml.ws.binding.http.HTTPBindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;
import java.io.OutputStream;

import com.sun.xml.ws.client.ClientTransportFactory;
import com.sun.xml.ws.handler.MessageContextImpl;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;

import com.sun.xml.ws.spi.runtime.ClientTransportFactoryTypes;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.transport.http.servlet.ServletConnectionImpl;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.util.xml.XmlUtil;
import java.net.URL;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import org.xml.sax.EntityResolver;
import javax.xml.ws.handler.MessageContext;

/**
 * Singleton factory class to instantiate concrete objects.
 *
 * @author JAX-WS Development Team
 */
public class WSRtObjectFactoryImpl
    extends com.sun.xml.ws.spi.runtime.WSRtObjectFactory {
    
    @Override
    public com.sun.xml.ws.spi.runtime.ClientTransportFactory
        createClientTransportFactory(int type, OutputStream outputStream) {
        
        ClientTransportFactory clientFactory = null;
        switch (type) {
            case ClientTransportFactoryTypes.HTTP :
                //return new HttpClientTransportFactory(outputStream);
                
            case ClientTransportFactoryTypes.LOCAL:         
                //return new LocalClientTransportFactory(null, outputStream);
        }
        return null;
    }
    
    @Override
    public com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo createRuntimeEndpointInfo() {
        return new RuntimeEndpointInfo();
    }
    
    /**
     * Creates a connection for servlet transport
     */
    @Override
    public WSConnection createWSConnection(HttpServletRequest req,
            HttpServletResponse res) {
        return new ServletConnectionImpl(req, res);
    }
    
    @Override
    public com.sun.xml.ws.spi.runtime.SOAPMessageContext createSOAPMessageContext() {
        return null;
    }
    
    @Override
    public com.sun.xml.ws.spi.runtime.Tie createTie() {
        return new Tie();
    }
    
    @Override
    public MessageContext createMessageContext() {
        return new MessageContextImpl();
    }
    
    @Override
    public com.sun.xml.ws.spi.runtime.Binding createBinding(String bindingId) {
        if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            return new SOAPBindingImpl(bindingId);
        } else if (bindingId.equals(HTTPBinding.HTTP_BINDING)) {
            return new HTTPBindingImpl();
        }
        return new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING);
    }
    
    /**
     * creates an EntityResolver for the XML Catalog URL
     */
    @Override
    public EntityResolver createEntityResolver(URL catalogUrl) {
        return XmlUtil.createEntityResolver(catalogUrl);
    }

    public List<com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo> getRuntimeEndpointInfos(
            ServletContext ctxt) {
        return (List<com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo>)ctxt.getAttribute(
                WSServlet.JAXWS_RI_RUNTIME_INFO);
    }
}
