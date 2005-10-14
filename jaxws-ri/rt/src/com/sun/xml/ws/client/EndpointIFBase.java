/*
 * $Id: EndpointIFBase.java,v 1.14 2005-10-14 18:27:59 bbissett Exp $
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
package com.sun.xml.ws.client;

import com.sun.pept.Delegate;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import java.net.URI;
import java.util.Map;


/**
 * @author WS Development Team
 */
public class EndpointIFBase implements com.sun.pept.presentation.Stub,
    com.sun.xml.ws.spi.runtime.StubBase, BindingProvider, InternalBindingProvider {

    protected Map _requestContext;
    protected Map _responseContext;

    protected URI _bindingId = null;
    protected Delegate _delegate = null;
    protected BindingImpl binding;

    private ClientTransportFactory _transportFactory;

    void setResponseContext(ResponseContext context) {
        _responseContext = (ContextMap) context;
    }

    public void _setDelegate(Delegate delegate) {
        _delegate = delegate;
    }

    public Delegate _getDelegate() {
        return _delegate;
    }

    public ClientTransportFactory _getTransportFactory() {
        _transportFactory =
            (ClientTransportFactory) getRequestContext().get(BindingProviderProperties.CLIENT_TRANSPORT_FACTORY);

        if (_transportFactory == null) {
            _transportFactory = new HttpClientTransportFactory();
        }
        return _transportFactory;
    }

    public void _setTransportFactory(ClientTransportFactory f) {
        getRequestContext().put(BindingProviderProperties.CLIENT_TRANSPORT_FACTORY, f);
        _transportFactory = f;
    }

    //toDo: have to update generator on PeptStub to getContext
    public void updateResponseContext(MessageInfo messageInfo) {
        ResponseContext responseContext = (ResponseContext)
            messageInfo.getMetaData(BindingProviderProperties.JAXWS_RESPONSE_CONTEXT_PROPERTY);
        if (responseContext != null) { // null in async case
            setResponseContext(responseContext);
        }
    }

    /**
     * Get the JAXWSContext that is used in processing request messages.
     * <p/>
     * Modifications to the request context do not affect asynchronous
     * operations that have already been started.
     *
     * @return The JAXWSContext that is used in processing request messages.
     */
    public Map<String, Object> getRequestContext() {
        if (_requestContext == null)
            _requestContext = new RequestContext(this);

        return _requestContext;
    }

    /**
     * Get the JAXWSContext that resulted from processing a response message.
     * <p/>
     * The returned context is for the most recently completed synchronous
     * operation. Subsequent synchronous operation invocations overwrite the
     * response context. Asynchronous operations return their response context
     * via the Response interface.
     *
     * @return The JAXWSContext that is used in processing request messages.
     */
    public Map<String, Object> getResponseContext() {
        if (_responseContext == null)
            _responseContext = new ResponseContext(this);
        return _responseContext;
    }

    public Binding getBinding() {
        return (Binding) binding;
    }

    public void _setBinding(BindingImpl binding) {
        this.binding = binding;
    }


    public void _setTransportFactory(com.sun.xml.ws.spi.runtime.ClientTransportFactory f) {
        _setTransportFactory((com.sun.xml.ws.spi.runtime.ClientTransportFactory) f);
    }

    /**
     * returns binding id from BindingImpl
     *
     * @return the URI representing the BindingID
     */
    public URI _getBindingId() {
        return _bindingId;
    }

//    // default for now is soap binding
//    public URI _getBindingId() {
//
//        if (_bindingId == null) {
//            try {
//                // this is a known string and should not cause error
//                _bindingId = new URI(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING);
//            } catch (java.net.URISyntaxException e) {
//                // should never happen, but just in case
//                throw new RuntimeException(e);
//            }
//        }
//        return _bindingId;
//    }
}
