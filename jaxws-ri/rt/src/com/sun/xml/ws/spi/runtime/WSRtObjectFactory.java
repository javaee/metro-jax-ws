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
package com.sun.xml.ws.spi.runtime;

import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.xml.ws.util.WSRtObjectFactoryImpl;
import java.net.URL;
import org.xml.sax.EntityResolver;

/**
 * Singleton abstract factory used to produce JAX-WS runtime related objects.
 */
public abstract class WSRtObjectFactory {

    private static final WSRtObjectFactory factory = new WSRtObjectFactoryImpl();

    /**
     * Obtain an instance of a factory. Don't worry about synchronization(at
     * the most, one more factory object is created).
     *
     */
    public static WSRtObjectFactory newInstance() {
        return factory;
    }

    /**
     * Creates SOAPMessageContext
     */
    public abstract SOAPMessageContext createSOAPMessageContext();


    /**
     * Creates an object with all endpoint info
     */
    public abstract RuntimeEndpointInfo createRuntimeEndpointInfo();
    
    /**
     * Creates a connection for servlet transport
     */
    public abstract WSConnection createWSConnection(
            HttpServletRequest req, HttpServletResponse res);


    /**
     * @param type The type of ClientTransportFactory
     * @see com.sun.xml.ws.spi.runtime.ClientTransportFactoryTypes
     */
    public abstract ClientTransportFactory createClientTransportFactory(
        int type,
        OutputStream logStream);

    
    /**
     * creates a Tie object, entry point to JAXWS runtime.
     */
    public abstract Tie createTie();
    
    /**
     * creates a MesageContext object. Create it for each MEP.
     */
    public abstract MessageContext createMessageContext();
    
    /**
     * creates the Binding object implementation. Set the object on
     * RuntimeEndpointInfo.
     * bindingId should be one of these values:
     * javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING,
     * javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING,
     * javax.xml.ws.http.HTTPBinding.HTTP_BINDING
     */
    public abstract Binding createBinding(String bindingId);
    
    /**
     * creates an EntityResolver for the XML Catalog URL
     */
    public abstract EntityResolver createEntityResolver(URL catalogUrl);

}
