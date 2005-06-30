/**
 * $Id: WSDLContext.java,v 1.3 2005-06-30 15:10:41 kwalsh Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.WebServiceException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
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

    public URL getOrigWSDLLocation() {
        return orgWsdlLocation;
    }

    public String getOrigURLPath() {
        return orgWsdlLocation.getPath();
    }

    public QName getServiceQName(){

        Set<QName> serviceKeys = service2portsLocationMap.keySet();
        Iterator<QName> iter = serviceKeys.iterator();
        if (iter.hasNext()){
            return iter.next();
        }
        return null;
    }

    public QName getServiceQName(QName serviceName){

        Set<QName> serviceKeys = service2portsLocationMap.keySet();
        if (serviceKeys.contains(serviceName))
            return serviceName;
        else throw new WebServiceException("Error supplied serviceQName is not correct.");
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
        String endpoint = null;
        QName portName = null;
        HashMap portsLocationMap = null;
        if (!portsLocationMap.isEmpty()) {
            Set<QName> keys = portsLocationMap.keySet();
            Iterator<QName> iter = keys.iterator();

            if (iter.hasNext()) {
                portName = iter.next();
            }
        }
        return portName;
    }

    // public void setEndpoint(String loc) {
    //     endpoint = loc;
    // }

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
}
