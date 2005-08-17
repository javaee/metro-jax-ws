/**
 * $Id: WSDLContext.java,v 1.13 2005-08-17 23:43:41 kohsuke Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl;

import com.sun.xml.ws.wsdl.parser.Binding;
import com.sun.xml.ws.wsdl.parser.Port;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import com.sun.xml.ws.wsdl.parser.Service;
import com.sun.xml.ws.wsdl.parser.WSDLDocument;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * $author: JAXWS Development Team
 */
public class WSDLContext {
    private final URL orgWsdlLocation;
    private String targetNamespace;
    private URI bindingId;
    private String defaultBindingId = SOAPBinding.SOAP11HTTP_BINDING;
    private LinkedHashMap<QName, LinkedHashMap> service2portsLocationMap = new LinkedHashMap<QName, LinkedHashMap>();   //service2
    private final WSDLDocument wsdlDoc;

    /**
     * Creates a {@link WSDLContext} by parsing the given wsdl file.
     */
    public WSDLContext(URL wsdlDocumentLocation, EntityResolver entityResolver) throws WebServiceException {
        //must get binding information
        assert entityResolver!=null;

        if (wsdlDocumentLocation == null)
            throw new WebServiceException("No WSDL location Information present, error");

        //WSDLParser parser = new WSDLParser();
        orgWsdlLocation = wsdlDocumentLocation;
        try {
            //return parser.parse(new BufferedInputStream(wsdlDocumentLocation.openStream()), getWSDLContext());
            wsdlDoc = RuntimeWSDLParser.parse(wsdlDocumentLocation,entityResolver);
        } catch (IOException e) {
            throw new WebServiceException(e);
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        } catch (SAXException e) {
            throw new WebServiceException(e);
        }

        String bId = wsdlDoc.getBindingId();
        if(bId != null)
            setBindingID(bId);
    }

    public URL getWsdlLocation() {
        return orgWsdlLocation;
    }

    public String getOrigURLPath() {
        return orgWsdlLocation.getPath();
    }

    public QName getServiceQName() {
        return wsdlDoc.getFirstServiceName();
    }

    public QName getServiceQName(QName serviceName) {
        if(wsdlDoc.getServices().containsKey(serviceName))
            return serviceName;
        throw new WebServiceException("Error supplied serviceQName is not correct.");
    }

    //just get the first one for now
    public String getEndpoint(QName serviceName) {
        if(serviceName == null)
            throw new WebServiceException("Service unknown, can not identify ports for an unknown Service.");
        Service service = wsdlDoc.getService(serviceName);
        String endpoint = null;
        if(service != null){
            Iterator<Map.Entry<QName, Port>> iter = service.entrySet().iterator();
            if(iter.hasNext()){
                Port port = iter.next().getValue();
                endpoint = port.getAddress();
            }
        }
        if(endpoint == null)
            throw new WebServiceException("Endpoint not found. Check WSDL file to verify endpoint was provided.");
        return endpoint;
    }

    //just get the first one for now
    public QName getPortName() {
        return wsdlDoc.getFirstPortName();
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

    public Set<QName> getPortsAsSet(QName serviceName) {
        Service service = wsdlDoc.getService(serviceName);
        if(service != null){
            return service.keySet();
        }
        return null;
    }

    //whats the purpose of this method??? a HashMap doesnt contain duplicate keys.
    public Set<QName> contains(QName serviceName) {
        Service service = wsdlDoc.getService(serviceName);
        if(service != null){
            Set<QName> services = new HashSet<QName>();
            services.add(serviceName);
            return services;
        }
        return null;

//        Set<QName> serviceNames = service2portsLocationMap.keySet();
//        HashSet names = new HashSet(serviceNames.size());
//        for (QName serviceNam : serviceNames) {
//            if (serviceNam.equals(serviceName)) {
//                names.add(serviceNam);
//                return names;
//            }
//        }
//        return serviceNames;
    }

    //Not sure whats the purpose of return Set when all you can have is only one port for
    //the given set of service and port QName
    public Set<QName> contains(QName serviceName, QName portName) {
        Service service = wsdlDoc.getService(serviceName);
        if(service != null){
            Port p = service.get(portName);
            Set<QName> ports = new HashSet<QName>();
            if(p != null)
                ports.add(p.getName());
            return ports;
        }
        return null;
//        LinkedHashMap portsMap = service2portsLocationMap.get(serviceName);
//        HashSet<QName> names = new HashSet<QName>(portsMap.size());
//        if ((portsMap != null) && (!portsMap.isEmpty())) {
//            Set<QName> portNames = portsMap.keySet();
//            for (QName portNam : portNames) {
//                if (portNam.equals(portName)) {
//                    names.add(portNam);
//                    return names;
//                }
//            }
//            return portNames;
//        }
//        return null;
    }

    public QName getFirstServiceName() {
        return wsdlDoc.getFirstServiceName();
    }

    public Set<QName> getAllServiceNames() {
        return wsdlDoc.getServices().keySet();
    }

    public WSDLDocument getWsdlDocument(){
        return wsdlDoc;
    }

    public Binding getWsdlBinding(QName service, QName port){
        if(wsdlDoc == null)
            return null;
        return wsdlDoc.getBinding(service, port);
    }

    public String getEndpoint(QName serviceName, QName portQName) {
        Service service = wsdlDoc.getService(serviceName);
        if(service != null){
            Port p = service.get(portQName);
            if(p != null)
                return p.getAddress();
            else
                throw new WebServiceException("No ports found for service " + serviceName);
        }else{
            throw new WebServiceException("Service unknown, can not identify ports for an unknown Service.");
        }

//        if (serviceName != null) {
//            //iterates in insertion order
//            LinkedHashMap portsLocationMap = service2portsLocationMap.get(serviceName);
//            if (portsLocationMap != null) {
//                if (!portsLocationMap.isEmpty()) {
//                    Set<QName> keys = portsLocationMap.keySet();
//                    for (QName portName: keys) {
//                        if (portName.equals(portQName)){
//                            return (String)portsLocationMap.get(portQName);
//
//                        }
//                    }
//                }
//            } else {
//                throw new WebServiceException("No ports found for service " + serviceName);
//            }
//        } else {
//            //service QName unknown throw exception
//            throw new WebServiceException("Service unknown, can not identify ports for an unknown Service.");
//        }
//        return null;
    }
}
