/*
 * $Id: WSDLModelInfo.java,v 1.1 2005-05-23 23:13:26 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config;


import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.sun.tools.ws.processor.modeler.Modeler;
import com.sun.tools.ws.util.JAXRPCClassFactory;

/**
 *
 * @author JAX-RPC Development Team
 */
public class WSDLModelInfo extends ModelInfo {

    public WSDLModelInfo() {}

    protected Modeler getModeler(Properties options) {
        return JAXRPCClassFactory.newInstance().createWSDLModeler(this, options);
    }

    public String getLocation() {
        return _location;
    }

    public void setLocation(String s) {
        _location = s;
    }

    public Set<Element> getJAXRPCBindings(){
        return _jaxrpcBindings;
    }

    public void addJAXRPCBindings(Element binding){        
        _jaxrpcBindings.add(binding);
    }

    public Set<InputSource> getJAXBBindings(){
        return _jaxbBindings;
    }

    public void addJAXBBIndings(InputSource jaxbBinding){        
        _jaxbBindings.add(jaxbBinding);
    }

    public void setHandlerConfig(Element handlerConfig){
        this.handlerConfig = handlerConfig;
    }
    
    public Element getHandlerConfig(){
        return handlerConfig;
    }
    
    private Element handlerConfig;
    
    private String _location;

    //external jaxrpc:bindings elements
    private Set<Element> _jaxrpcBindings = new HashSet<Element>();

    //we need an array of jaxb:binding elements, they are children of jaxrpc:bindings
    //and could come from an external customization file or wsdl.
    private Set<InputSource> _jaxbBindings = new HashSet<InputSource>();
}
