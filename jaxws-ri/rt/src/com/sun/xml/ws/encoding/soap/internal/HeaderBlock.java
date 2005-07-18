/*
 * $Id: HeaderBlock.java,v 1.3 2005-07-18 16:52:16 kohlert Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/
package com.sun.xml.ws.encoding.soap.internal;

import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import javax.xml.namespace.QName;

/**
 * @author WS Development Team
 */
public class HeaderBlock {
    
    private JAXBBridgeInfo value;
    
    public HeaderBlock(JAXBBridgeInfo bridgeInfo) {
        this.value = bridgeInfo;
    }
    
    public Object getValue() {
        return value;
    }
    
    public QName getName() {
        return value.getName();
    }
}
