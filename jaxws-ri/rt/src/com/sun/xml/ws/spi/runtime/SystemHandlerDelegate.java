/**
 * $Id: SystemHandlerDelegate.java,v 1.9 2005-09-10 01:52:10 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;


/**
 *
 * @author WS Development Team
 */

public interface SystemHandlerDelegate {

   /**
    * This method is called before MU processing. 
    *
    * Invoker object can be got from MessageContextUtil.getInvoker(MessageContext)
    */
    public boolean processRequest(MessageContext messageContext) throws Exception;

   /**
    * This method is called after Invoker.invoke().
    */
    public void processResponse(MessageContext messageContext) throws Exception;
    
    /**
     * Called before the method invocation.
     * @param messageContext contains property bag with the scope of the
     *                       properties
     */
    public void preInvokeEndpointHook(MessageContext messageContext);
}
