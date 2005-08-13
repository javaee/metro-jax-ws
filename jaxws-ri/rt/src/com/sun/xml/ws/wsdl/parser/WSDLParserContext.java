/**
 * $Id: WSDLParserContext.java,v 1.2 2005-08-13 19:58:40 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.net.URL;

public class WSDLParserContext {
    private WSDLDocument wsdlDoc;
    private Stack<String> uris;
    private Stack<String> wsdlProcessed;
    private URL originalWsdlURL;
    private Set<String> importedWSDLs;

    public WSDLParserContext(WSDLDocument wsdlDoc) {
        this.wsdlDoc = wsdlDoc;
        this.uris =  new Stack<String>();
        this.wsdlProcessed = new Stack<String>();
        this.importedWSDLs = new HashSet<String>();
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

    public URL getOriginalWsdlURL() {
        return originalWsdlURL;
    }

    public void setOriginalWsdlURL(URL originalWsdlURL) {
        this.originalWsdlURL = originalWsdlURL;
    }

    public void addImportedWSDL(String location){
        importedWSDLs.add(location);
    }

    public boolean isImportedWSDL(String location){
        return importedWSDLs.contains(location);
    }
}
