/**
 * $Id: WSDLContext.java,v 1.14 2005-09-07 19:07:15 kwalsh Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl;

import com.sun.xml.ws.wsdl.parser.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * $author: JAXWS Development Team
 */
public class WSDLContext {
    private final URL orgWsdlLocation;
    private String targetNamespace;
    private URI bindingId;
    private String defaultBindingId = SOAPBinding.SOAP11HTTP_BINDING;
    private final WSDLDocument wsdlDoc;

    /**
     * Creates a {@link WSDLContext} by parsing the given wsdl file.
     */
    public WSDLContext(URL wsdlDocumentLocation, EntityResolver entityResolver) throws WebServiceException {
        //must get binding information
        assert entityResolver != null;

        if (wsdlDocumentLocation == null)
            throw new WebServiceException("No WSDL location Information present, error");

        orgWsdlLocation = wsdlDocumentLocation;
        try {
            wsdlDoc = RuntimeWSDLParser.parse(wsdlDocumentLocation, entityResolver);
        } catch (IOException e) {
            throw new WebServiceException(e);
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        } catch (SAXException e) {
            throw new WebServiceException(e);
        }

        String bId = wsdlDoc.getBindingId();
        if (bId != null)
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
        if (wsdlDoc.getServices().containsKey(serviceName))
            return serviceName;
        throw new WebServiceException("Error supplied serviceQName is not correct.");
    }

    //just get the first one for now
    public String getEndpoint(QName serviceName) {
        if (serviceName == null)
            throw new WebServiceException("Service unknown, can not identify ports for an unknown Service.");
        Service service = wsdlDoc.getService(serviceName);
        String endpoint = null;
        if (service != null) {
            Iterator<Map.Entry<QName, Port>> iter = service.entrySet().iterator();
            if (iter.hasNext()) {
                Port port = iter.next().getValue();
                endpoint = port.getAddress();
            }
        }
        if (endpoint == null)
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

    public Set<QName> getPortsAsSet(QName serviceName) {
        Service service = wsdlDoc.getService(serviceName);
        if (service != null) {
            return service.keySet();
        }
        return null;
    }


    public boolean contains(QName serviceName, QName portName) {
        Service service = wsdlDoc.getService(serviceName);
        if (service != null) {

            Iterator<Map.Entry<QName, Port>> iter = service.entrySet().iterator();
            while (iter.hasNext()) {
                Port port = iter.next().getValue();
                if (port.getName().equals(portName))
                    return true;
            }
        }
        return false;
    }

    public QName getFirstServiceName() {
        return wsdlDoc.getFirstServiceName();
    }

    public Set<QName> getAllServiceNames() {
        return wsdlDoc.getServices().keySet();
    }

    public WSDLDocument getWsdlDocument() {
        return wsdlDoc;
    }

    public Binding getWsdlBinding(QName service, QName port) {
        if (wsdlDoc == null)
            return null;
        return wsdlDoc.getBinding(service, port);
    }

    public String getEndpoint(QName serviceName, QName portQName) {
        Service service = wsdlDoc.getService(serviceName);
        if (service != null) {
            Port p = service.get(portQName);
            if (p != null)
                return p.getAddress();
            else
                throw new WebServiceException("No ports found for service " + serviceName);
        } else {
            throw new WebServiceException("Service unknown, can not identify ports for an unknown Service.");
        }
    }
}
