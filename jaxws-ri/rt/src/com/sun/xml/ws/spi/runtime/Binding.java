/**
 * $Id: Binding.java,v 1.2 2005-06-30 18:50:32 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
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

  public void setSystemHandlerDelegate(SystemHandlerDelegate delegate);
}
