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

package com.sun.xml.ws.binding;

import com.sun.xml.ws.binding.http.HTTPBindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.namespace.QName;

/**
 * Instances are created by the service, which then
 * sets the handler chain on the binding impl. The handler
 * caller class actually creates and manages the handlers.
 *
 * <p>Also used on the server side, where non-api calls such as
 * getHandlerChainCaller cannot be used. So the binding impl
 * now stores the handler list rather than deferring to the
 * handler chain caller.
 *
 * <p>This class is made abstract as we dont see a situation when a BindingImpl has much meaning without binding id.
 * IOw, for a specific binding there will be a class extending BindingImpl, for example SOAPBindingImpl.
 *
 * <p>The spi Binding interface extends Binding.
 *
 * @author WS Development Team
 */
public abstract class BindingImpl implements
    com.sun.xml.ws.spi.runtime.Binding {

    // caller ignored on server side
    protected HandlerChainCaller chainCaller;

    private SystemHandlerDelegate systemHandlerDelegate;
    private List<Handler> handlers;
    private String bindingId;
    protected QName serviceName;

   // called by DispatchImpl
    public BindingImpl(String bindingId, QName serviceName) {
        this.bindingId = bindingId;
        this.serviceName = serviceName;
    }

    public BindingImpl(List<Handler> handlerChain, String bindingId, QName serviceName) {
        handlers = handlerChain;
        this.bindingId = bindingId;
        this.serviceName = serviceName;
    }


    /**
     * Return a copy of the list. If there is a handler chain caller,
     * this is the proper list. Otherwise, return a copy of 'handlers'
     * or null if list is null. The RuntimeEndpointInfo.init() method
     * relies on this list being null if there were no handlers
     * in the deployment descriptor file.
     *
     * @return The list of handlers. This can be null if there are
     * no handlers. The list may have a different order depending on
     * whether or not the handlers have been called yet, since the
     * logical and protocol handlers will be sorted before calling them.
     *
     * @see com.sun.xml.ws.server.RuntimeEndpointInfo#init
     */
    public List<Handler> getHandlerChain() {
        if (chainCaller != null) {
            return new ArrayList(chainCaller.getHandlerChain());
        }
        if (handlers == null) {
            return null;
        }
        return new ArrayList(handlers);
    }
    
    public boolean hasHandlers() {
        if (handlers == null || handlers.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Sets the handlers on the binding. If the handler chain
     * caller already exists, then the handlers will be set on
     * the caller and the handler chain held by the binding will
     * be the sorted list.
     */
    public void setHandlerChain(List<Handler> chain) {
        if (chainCaller != null) {
            chainCaller = new HandlerChainCaller(chain);
            handlers = chainCaller.getHandlerChain();
        } else {
            handlers = chain;
        }
    }

    /**
     * Creates the handler chain caller if needed and returns
     * it. Once the handler chain caller exists, this class
     * defers getHandlers() calls to it to get the new sorted
     * list of handlers.
     */
    public HandlerChainCaller getHandlerChainCaller() {
        if (chainCaller == null) {
            chainCaller = new HandlerChainCaller(handlers);
        }
        return chainCaller;
    }

    public String getBindingId(){
        return bindingId;
    }

    public String getActualBindingId() {
        return bindingId;
    }

    public void setServiceName(QName serviceName){
        this.serviceName = serviceName;
    }

    public SystemHandlerDelegate getSystemHandlerDelegate() {
        return systemHandlerDelegate;
    }

    public void setSystemHandlerDelegate(SystemHandlerDelegate delegate) {
        systemHandlerDelegate = delegate;
    }

    public static com.sun.xml.ws.spi.runtime.Binding getBinding(String bindingId,
                                                                Class implementorClass, QName serviceName, boolean tokensOK) {

        if (bindingId == null) {
            // Gets bindingId from @BindingType annotation
            bindingId = RuntimeModeler.getBindingId(implementorClass);
            if (bindingId == null) {            // Default one
                bindingId = SOAPBinding.SOAP11HTTP_BINDING;
            }
        }
        if (tokensOK) {
            if (bindingId.equals("##SOAP11_HTTP")) {
                bindingId = SOAPBinding.SOAP11HTTP_BINDING;
            } else if (bindingId.equals("##SOAP12_HTTP")) {
                bindingId = SOAPBinding.SOAP12HTTP_BINDING;
            } else if (bindingId.equals("##XML_HTTP")) {
                bindingId = HTTPBinding.HTTP_BINDING;
            }
        }
        if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)
            || bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)
            || bindingId.equals(SOAPBindingImpl.X_SOAP12HTTP_BINDING)) {
            return new SOAPBindingImpl(bindingId, serviceName);
        } else if (bindingId.equals(HTTPBinding.HTTP_BINDING)) {
            return new HTTPBindingImpl();
        } else {
            throw new IllegalArgumentException("Wrong bindingId "+bindingId);
        }
    }

    public static Binding getDefaultBinding() {
        return new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING);
    }

    public static Binding getDefaultBinding(QName serviceName) {
        return new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING, serviceName);
    }
}
