/*
 * $Id: HeaderBlock.java,v 1.1 2005-05-23 22:30:16 bbissett Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/
package com.sun.xml.ws.encoding.soap.internal;

import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import javax.xml.namespace.QName;

/**
 * @author JAX-RPC RI Development Team
 */
public class HeaderBlock extends MessageBlock {
    
    /*
     * @deprecated
     */
    public HeaderBlock(QName name, Object value) {
        super(name, value);
    }
    
    /*
     * @deprecated
     */
    public HeaderBlock(QName name) {
        super(name);
    }
    
    /*
     * @deprecated
     */
    public HeaderBlock() {
        super();
    }
    
    public HeaderBlock(JAXBBridgeInfo bridgeInfo) {
        this._value = bridgeInfo;
    }
}
