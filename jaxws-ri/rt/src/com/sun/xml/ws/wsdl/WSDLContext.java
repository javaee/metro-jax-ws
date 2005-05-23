/**
 * $Id: WSDLContext.java,v 1.1 2005-05-23 23:07:15 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPBinding;
import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * $author: JAXRPC Development Team
 */
public class WSDLContext {
    private URL orgWsdlLocation;
    private String targetNamespaceURI;
    private QName serviceName;
    //private String endpoint;
    private URI bindingId;
    private String defaultBindingId = SOAPBinding.SOAP11HTTP_BINDING;
    private HashMap<QName, String> portsLocationMap; //ports2LocationMap
    private HashMap<QName, HashMap> service2portsLocationMap;   //service2

    public WSDLContext() {
        portsLocationMap = new HashMap<QName, String>();
        service2portsLocationMap = new HashMap<QName, HashMap>();
    }


    public void setOrigWSDLLocation(URL wsdlLocation){
        orgWsdlLocation = wsdlLocation;

    }


     public URL getOrigWSDLLocation(){
        return orgWsdlLocation;
    }

    public String getOrigURLPath(){
        return orgWsdlLocation.getPath();
    }

    //just get the first one for now
    public String getEndpoint() {
        String endpoint = null;
        if (!portsLocationMap.isEmpty()){
            Set<QName> keys = portsLocationMap.keySet();
            Iterator<QName> iter =  keys.iterator();

            if (iter.hasNext()){
                endpoint = portsLocationMap.get(iter.next());
            }
        }
        return endpoint;
    }

     //just get the first one for now
    public QName getPortName() {
        String endpoint = null;
        QName portName = null;
        if (!portsLocationMap.isEmpty()){
            Set<QName> keys = portsLocationMap.keySet();
            Iterator<QName> iter =  keys.iterator();

            if (iter.hasNext()){
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

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName name) {
        serviceName = name;
    }

    public void addPort(QName portName, String location){
        portsLocationMap.put(portName, location);
    }
}
