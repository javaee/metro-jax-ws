/**
 * $Id: WSDLParserContext.java,v 1.3 2005-08-17 22:29:48 kohsuke Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import org.xml.sax.EntityResolver;

import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.net.URL;

public class WSDLParserContext {
    private final WSDLDocument wsdlDoc;
    private final Stack<String> uris;
    private final Stack<String> wsdlProcessed;
    private final Set<String> importedWSDLs;
    /**
     * Must not be null.
     */
    private final EntityResolver resolver;

    public WSDLParserContext(WSDLDocument wsdlDoc, EntityResolver resolver ) {
        assert resolver!=null;
        this.wsdlDoc = wsdlDoc;
        this.uris =  new Stack<String>();
        this.wsdlProcessed = new Stack<String>();
        this.importedWSDLs = new HashSet<String>();
        this.resolver = resolver;
    }

    public void pushContext(String systemId, String tns){
        wsdlProcessed.push(systemId);
        uris.push(tns);
    }

    public void popContext(){
        if(!uris.isEmpty())
            uris.pop();
        
        if(!wsdlProcessed.isEmpty())
            wsdlProcessed.pop();
    }

    public String getTargetNamespace(){
        if(uris.isEmpty())
            return null;
        return uris.peek();
    }

    public String getSystemId(){
        return wsdlProcessed.peek();
    }

    public WSDLDocument getWsdlDocument() {
        return wsdlDoc;
    }

    public void addImportedWSDL(String location){
        importedWSDLs.add(location);
    }

    public boolean isImportedWSDL(String location){
        return importedWSDLs.contains(location);
    }

    public EntityResolver getResolver() {
        return resolver;
    }
}
