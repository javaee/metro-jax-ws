/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.spi.runtime;

import javax.xml.ws.security.SecurityConfiguration;

/** The <code>Binding</code> is the spi interface that extends the base interface
 *  for JAX-WS protocol bindings.
 * This interface is implemented by com.sun.xml.ws.client.Binding.
**/
public interface Binding extends javax.xml.ws.Binding {

  public SystemHandlerDelegate getSystemHandlerDelegate();

  /** Sets the handler chain for the protocol binding instance.
   *
   *  @param chain    A List of handler configuration entries
   *  @throws WebServiceException On an error in the configuration of
   *                  the handler chain
   *  @throws java.lang.UnsupportedOperationException If this
   *          operation is not supported. This may be done to
   *          avoid any overriding of a pre-configured handler
   *          chain.
  **/
  public void setSystemHandlerDelegate(SystemHandlerDelegate delegate);
}
