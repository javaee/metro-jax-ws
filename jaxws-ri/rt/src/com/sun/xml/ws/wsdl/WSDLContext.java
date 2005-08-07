/**
 * $Id: WSDLContext.java,v 1.7 2005-08-07 20:16:42 kwalsh Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * $author: JAXWS Development Team
 */
public class WSDLContext {
    private URL orgWsdlLocation;
    private String targetNamespace;
    private URI bindingId;
    private String defaultBindingId = SOAPBinding.SOAP11HTTP_BINDING;
    private LinkedHashMap<QName, LinkedHashMap> service2portsLocationMap;   //service2

    public WSDLContext() {
        service2portsLocationMap = new LinkedHashMap<QName, LinkedHashMap>();
    }

    public void setOrigWSDLLocation(URL wsdlLocation) {
        orgWsdlLocation = wsdlLocation;
    }

    public URL getWsdlLocation() {
        return orgWsdlLocation;
    }

    public String getOrigURLPath() {
        return orgWsdlLocation.getPath();
    }

    public QName getServiceQName() {

        Set<QName> serviceKeys = service2portsLocationMap.keySet();
        Iterator<QName> iter = serviceKeys.iterator();
        if (iter.hasNext()) {
            return iter.next();
        }
        return null;
    }

    public QName getServiceQName(QName serviceName) {

        Set<QName> serviceKeys = service2portsLocationMap.keySet();
        if (serviceKeys.contains(serviceName))
            return serviceName;
        else
            throw new WebServiceException("Error supplied serviceQName is not correct.");
    }

    //just get the first one for now
    public String getEndpoint(QName serviceName) {
        String endpoint = null;
        if (serviceName != null) {
            //iterates in insertion order
            LinkedHashMap portsLocationMap = service2portsLocationMap.get(serviceName);
            if (portsLocationMap != null) {
                if (!portsLocationMap.isEmpty()) {
                    Set<QName> keys = portsLocationMap.keySet();
                    Iterator<QName> iter = keys.iterator();
                    if (iter.hasNext()) {
                        endpoint = (String) portsLocationMap.get(iter.next());
                    }
                }
            } else {
                throw new WebServiceException("No ports found for service " + serviceName);
            }
        } else {
            //service QName unknown throw exception
            throw new WebServiceException("Service unknown, can not identify ports for an unknown Service.");
        }
        if (endpoint == null)
            throw new WebServiceException("Endpoint not found. Check WSDL file to verify endpoint was provided.");

        return endpoint;
    }

    //just get the first one for now
    public QName getPortName() {
        QName portName = null;
        //iterates in insertion order
        if (!service2portsLocationMap.isEmpty()) {
            LinkedHashMap portsLocationMap =
                service2portsLocationMap.values().iterator().next();
            if (portsLocationMap != null) {
                if (!portsLocationMap.isEmpty()) {
                    Set<QName> keys = portsLocationMap.keySet();
                    Iterator<QName> iter = keys.iterator();

                    if (iter.hasNext()) {
                        portName = iter.next();
                    }
                }
            }
        }
        return portName;
    }

    public URI getBindingID() {
        if (bindingId == null)
            try {
                return new URI(defaultBindingId);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        return bindingId;
    }

    public void setBindingID(String id) {
        try {
            bindingId = new URI(id);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String tns) {
        targetNamespace = tns;
    }

    public void addService2Ports(QName serviceName, LinkedHashMap portsMap) {
        service2portsLocationMap.put(serviceName, portsMap);
    }

    public Set getPortsAsSet(QName serviceName) {

        LinkedHashMap portsMap = service2portsLocationMap.get(serviceName);
        if ((portsMap != null) && (!portsMap.isEmpty())) {
            Set<QName> keys = portsMap.keySet();
            return keys;
        }
        return null;
    }

    public Set<QName> contains(QName serviceName) {

        Set<QName> serviceNames = service2portsLocationMap.keySet();
        HashSet names = new HashSet(serviceNames.size());
        for (QName serviceNam : serviceNames) {
            if (serviceNam.equals(serviceName)) {
                names.add(serviceNam);
                return names;
            }
        }
        return serviceNames;
    }

    public Set<QName> contains(QName serviceName, QName portName) {

        LinkedHashMap portsMap = service2portsLocationMap.get(serviceName);
        HashSet<QName> names = new HashSet<QName>(portsMap.size());
        if ((portsMap != null) && (!portsMap.isEmpty())) {
            Set<QName> portNames = portsMap.keySet();
            for (QName portNam : portNames) {
                if (portNam.equals(portName)) {
                    names.add(portNam);
                    return names;
                }
            }
            return portNames;
        }
        return null;
    }

    public QName getFirstServiceName() {
        if (!service2portsLocationMap.isEmpty()) {
            Set<QName> serviceNames = service2portsLocationMap.keySet();
            Iterator iter = serviceNames.iterator();
            if (iter.hasNext())
                return (QName) iter.next();
        }
        return null;
    }

    public Set<QName> getAllServiceNames() {
        if (!service2portsLocationMap.isEmpty()) {
            return service2portsLocationMap.keySet();
        }
        return null;
    }

    public String getEndpoint(QName serviceName, QName portQName) {

        if (serviceName != null) {
            //iterates in insertion order
            LinkedHashMap portsLocationMap = service2portsLocationMap.get(serviceName);
            if (portsLocationMap != null) {
                if (!portsLocationMap.isEmpty()) {
                    Set<QName> keys = portsLocationMap.keySet();
                    for (QName portName: keys) {
                        if (portName.equals(portQName)){
                            return (String)portsLocationMap.get(portQName);

                        }
                    }
                }
            } else {
                throw new WebServiceException("No ports found for service " + serviceName);
            }
        } else {
            //service QName unknown throw exception
            throw new WebServiceException("Service unknown, can not identify ports for an unknown Service.");
        }        
        return null;
    }
}
