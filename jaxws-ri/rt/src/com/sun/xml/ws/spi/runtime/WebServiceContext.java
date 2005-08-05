/**
 * $Id: WebServiceContext.java,v 1.2 2005-08-05 01:03:34 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

public interface WebServiceContext extends javax.xml.ws.WebServiceContext {
    
    /**
     *
     *
     */
    public void setMessageContext(MessageContext ctxt);
    
    public MessageContext getMessageContext();
    
}
