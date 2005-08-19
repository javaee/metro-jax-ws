/**
 * $Id: WSDLDocument.java,v 1.2 2005-08-19 01:17:19 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPBinding;
import java.util.*;

public class WSDLDocument {
    protected Map<QName, Message> messages;
    protected Map<QName, PortType> portTypes;
    protected Map<QName, Binding> bindings;
    protected Map<QName, Service> services;

    public WSDLDocument() {
//        messages = new HashMap<QName, Message>();
//        portTypes = new HashMap<QName, PortType>();
        bindings = new HashMap<QName, Binding>();
        services = new LinkedHashMap<QName, Service>();
    }

    public void addMessage(Message msg){
        messages.put(msg.getName(), msg);
    }

    public Message getMessage(QName name){
        return messages.get(name);
    }

    public void addPortType(PortType pt){
        portTypes.put(pt.getName(), pt);
    }

    public PortType getPortType(QName name){
        return portTypes.get(name);
    }

    public void addBinding(Binding binding){
        bindings.put(binding.getName(), binding);
    }

    public Binding getBinding(QName name){
        return bindings.get(name);
    }

    public void addService(Service svc){
        services.put(svc.getName(), svc);
    }

    public Service getService(QName name){
        return services.get(name);
    }

    public Map<QName, Service> getServices(){
        return services;
    }

    /**
     * Returns the first service QName from insertion order
     * @return
     */
    public QName getFirstServiceName(){
        if(services.isEmpty())
            return null;
        return services.values().iterator().next().getName();
    }

    /**
     * Returns first port QName from first service as per the insertion order
     * @return
     */
    public QName getFirstPortName(){
        if(services.isEmpty())
            return null;
        Service service = services.values().iterator().next();
        Iterator<QName> iter = service.keySet().iterator();
        QName port = (iter.hasNext())?iter.next():null;
        return port;
    }

    private Port getFirstPort(){
        if(services.isEmpty())
            return null;
        Service service = services.values().iterator().next();
        Collection<Port> coll = service.values();
        Port port = (coll != null)?((coll.iterator().hasNext())?coll.iterator().next():null):null;
        return port;
    }


    /**
     * Returns biningId of the first port
     * @return
     */
    public String getBindingId(){
        Port port = getFirstPort();
        if(port == null)
            return null;
        Binding binding = bindings.get(port.getBindingName());
        if(binding == null)
            return null;
        return binding.getBindingId();
    }

    /**
     * Gives the binding Id of the given service and port
     * @param service
     * @param port
     * @return
     */
    public String getBindingId(QName service, QName port){
        Service s = services.get(service);
        if(s != null){
            Port p = s.get(port);
            if(p != null){
                Binding b = bindings.get(p.getBindingName());
                if(b != null)
                    return b.getBindingId();
            }

        }
        return null;
    }

     /**
     *
     * @param serviceName non-null service QName
     * @param portName    non-null port QName
     * @return
     *          BindingOperation on success otherwise null. throws NPE if any of the parameters null
     */
    public Binding getBinding(QName serviceName, QName portName){
        Service service = services.get(serviceName);
        if(service != null){
            Port port = service.get(portName);
            if(port != null){
                QName bindingName = port.getBindingName();
                return bindings.get(bindingName);
            }
        }
        return null;
    }

    /**
     * Returns the bindings for the given bindingId
     * @param service  non-null service
     * @param bindingId  non-null binding id
     * @return
     */
    public List<Binding> getBindings(Service service, String bindingId){
        List<Binding> bs = new ArrayList<Binding>();
        Collection<Port> ports = service.values();
        if(ports.isEmpty())
            return bs;
        for(Port port:ports){
            Binding b = bindings.get(port.getName());
            if(b == null)
                return bs;
            if(b.equals(bindingId))
                bs.add(b);
        }
        return bs;
    }

}
