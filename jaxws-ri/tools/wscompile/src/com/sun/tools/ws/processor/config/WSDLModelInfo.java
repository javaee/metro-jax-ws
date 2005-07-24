/*
 * $Id: WSDLModelInfo.java,v 1.3 2005-07-24 01:35:07 kohlert Exp $
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
import com.sun.tools.ws.util.JAXWSClassFactory;

/**
 *
 * @author WS Development Team
 */
public class WSDLModelInfo extends ModelInfo {

    public WSDLModelInfo() {}

    protected Modeler getModeler(Properties options) {
        return JAXWSClassFactory.newInstance().createWSDLModeler(this, options);
    }

    public String getLocation() {
        return _location;
    }

    public void setLocation(String s) {
        _location = s;
    }

    public Set<Element> getJAXWSBindings(){
        return _jaxwsBindings;
    }

    public void addJAXWSBindings(Element binding){        
        _jaxwsBindings.add(binding);
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

    //external jaxws:bindings elements
    private Set<Element> _jaxwsBindings = new HashSet<Element>();

    //we need an array of jaxb:binding elements, they are children of jaxws:bindings
    //and could come from an external customization file or wsdl.
    private Set<InputSource> _jaxbBindings = new HashSet<InputSource>();
}
