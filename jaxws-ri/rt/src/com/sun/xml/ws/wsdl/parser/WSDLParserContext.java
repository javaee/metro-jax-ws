/**
 * $Id: WSDLParserContext.java,v 1.1 2005-08-13 19:30:37 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import java.util.Stack;

public class WSDLParserContext {
    private WSDLDocument wsdlDoc;
    private Stack<String> uris;
    private Stack<String> wsdlProcessed;

    public WSDLParserContext(WSDLDocument wsdlDoc) {
        this.wsdlDoc = wsdlDoc;
        this.uris =  new Stack<String>();
        this.wsdlProcessed = new Stack<String>();
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
}
