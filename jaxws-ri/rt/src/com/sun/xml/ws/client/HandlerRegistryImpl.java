/**
 * $Id: HandlerRegistryImpl.java,v 1.4 2005-07-18 16:52:05 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerRegistry;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.http.HTTPBinding;
import java.net.URI;
import java.util.*;
import com.sun.xml.ws.binding.soap.BindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;

/**
 * Because this class is created even for binding providers that
 * have no preconfigured handlers, we put off creating the maps
 * and lists until needed.
 * <p/>
 * Because users can set chains that are Lists, the registry uses
 * simple lists to store handlers and not a more specific class
 * that extends List.
 *
 * @author WS Development Team
 */
public class HandlerRegistryImpl implements HandlerRegistry {

    public static final String HANDLER_FOR_PORT_NAME =
        "com.sun.xml.rpc.handler.PORTNAME";
    public static final String HANDLER_FOR_PORT_URI =
        "com.sun.xml.rpc.handler.PORTURI";
    public static final String HANDLER_FOR_PROTOCOL =
        "com.sun.xml.rpc.handler.protocol";

    private List<Handler> serviceHandlers;
    private Map<QName, List<Handler>> portHandlers;
    private Map<URI, List<Handler>> bindingHandlers;

    private HashSet<QName> ports;

    /*
     * Set of ports contained in the service. Needed
     * for checking of IllegalArgumentException cases.
     */
    public HandlerRegistryImpl(HashSet<QName> ports) {
        this.ports = ports;
    }

    /*
     * Used when a port has been created in a service after
     * the handler registry has already been created.
     */
    void addPort(QName port) {
        ports.add(port);
    }

    public List<Handler> getHandlerChain() {
        if (serviceHandlers == null) {
            serviceHandlers = new ArrayList();
        }
        return serviceHandlers;
    }

    public List<Handler> getHandlerChain(QName portName) {
        checkPortName(portName);
        Map<QName, List<Handler>> map = getHandlerChainsForPorts();
        if (map.get(portName) == null) {
            setHandlerChain(portName, new ArrayList<Handler>());
        }
        return map.get(portName);
    }

    public List<Handler> getHandlerChain(URI bindingId) {
        checkBindingId(bindingId);
        Map<URI, List<Handler>> map = getHandlerChainsForBindings();
        if (map.get(bindingId) == null) {
            setHandlerChain(bindingId, new ArrayList<Handler>());
        }
        return map.get(bindingId);
    }

    public void setHandlerChain(List<Handler> list) {
        serviceHandlers = list;
    }

    public void setHandlerChain(URI bindingId, List<Handler> list) {
        checkBindingId(bindingId);
        getHandlerChainsForBindings().put(bindingId, list);
    }

    public void setHandlerChain(QName portName, List list) {
        checkPortName(portName);
        getHandlerChainsForPorts().put(portName, list);
    }


    // todo: are these three methods needed?
    public void addHandler(Handler info) {
        getHandlerChain().add(info);
    }

    public void addHandler(QName portName, Handler info) {
        getHandlerChain(portName).add(info);
    }

    public void addHandler(URI bindingId, Handler info) {
        getHandlerChain(bindingId).add(info);
    }
    // end todo check

    // Called by service to set binding on binding provider.
    public BindingImpl createBinding(QName portName, URI bindingId) {
        List<Handler> logicalThenProtocolHandlers =
            new ArrayList<Handler>();
        List<Handler> protocolHandlers = new ArrayList<Handler>();

        // service handlers
        for (Handler handler : getHandlerChain()) {
            if (LogicalHandler.class.isAssignableFrom(handler.getClass())) {
                logicalThenProtocolHandlers.add(handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        // port handlers
        for (Handler handler : getHandlerChain(portName)) {
            if (LogicalHandler.class.isAssignableFrom(handler.getClass())) {
                logicalThenProtocolHandlers.add(handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        // protocol handlers
        for (Handler handler : getHandlerChain(bindingId)) {
            if (LogicalHandler.class.isAssignableFrom(handler.getClass())) {
                logicalThenProtocolHandlers.add(handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        // create binding
        logicalThenProtocolHandlers.addAll(protocolHandlers);
        if (bindingId.toString().equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                bindingId.toString().equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            return new SOAPBindingImpl(logicalThenProtocolHandlers, bindingId.toString());
        }else if(bindingId.toString().equals(HTTPBinding.HTTP_BINDING)){
            //TODO: HTTPBindingImpl()
        }

        //we dont support any other binding so return null???
        return null;
    }

    /*
     * Put off creation of map until needed
     */
    private Map<QName, List<Handler>> getHandlerChainsForPorts() {
        if (portHandlers == null) {
            portHandlers = new HashMap<QName, List<Handler>>();
        }
        return portHandlers;
    }

    /*
     * Put off creation of map until needed
     */
    private Map<URI, List<Handler>> getHandlerChainsForBindings() {
        if (bindingHandlers == null) {
            bindingHandlers = new HashMap<URI, List<Handler>>();
        }
        return bindingHandlers;
    }

    // check the QName array rather than keys in map
    private void checkPortName(QName portName) {
        if (ports.contains(portName)) {
            return;
        }

        // port name is invalid
        StringBuffer validPorts = new StringBuffer();
        for (QName port : ports) {
            validPorts.append(" " + port.toString());
        }

        Object[] messageArgs =
            new Object[]{portName.toString(), validPorts.toString()};
        LocalizableMessageFactory messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.client");
        Localizer localizer = new Localizer();
        Localizable locMessage = messageFactory.getMessage("invalid.port.name",
            messageArgs);
        throw new IllegalArgumentException(localizer.localize(locMessage));
    }

    // so far only supporting one binding
    private void checkBindingId(URI bindingId) {
        if (bindingId.toString().equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                bindingId.toString().equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            return;
        }

        // binding id not valid
        Object[] messageArgs = new Object[]{bindingId.toString(),
                                            SOAPBinding.SOAP11HTTP_BINDING};

        LocalizableMessageFactory messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.client");
        Localizable locMessage =
            messageFactory.getMessage("invalid.binding.id", messageArgs);
        Localizer localizer = new Localizer();
        throw new IllegalArgumentException(localizer.localize(locMessage));
    }
}
