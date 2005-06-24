/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.spi.runtime;

import java.net.URI;
import java.util.Set;
import com.sun.xml.ws.spi.runtime.Binding;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.MessageFactory;

/** The <code>SOAPBinding</code> spi interface an spi abstraction for
 *  the SOAP binding and extends the spi Binding interface.
 *  This is implemented by the class com.sun.xml.ws.client.SOAPBindingImpl
 *  @since JAX-WS 2.0
**/
public interface SOAPBinding extends com.sun.xml.ws.spi.runtime.Binding ,javax.xml.ws.soap.SOAPBinding {

  /** Gets the roles played by the SOAP binding instance.
   *
   *  @return Set<URI> The set of roles played by the binding instance.
  **/
  public Set<URI> getRoles();

  /** Sets the roles played by the SOAP binding instance.
   *
   *  @param roles    The set of roles played by the binding instance.
   *  @throws WebServiceException On an error in the configuration of
   *                  the list of roles.
  **/
  public void setRoles(Set<URI> roles);

  /**
   * Returns <code>true</code> if the use of MTOM is enabled.
   *
   * @return <code>true</code> if and only if the use of MTOM is enabled.
  **/

  public boolean isMTOMEnabled();

  /**
   * Enables or disables use of MTOM.
   *
   * @param flag   A <code>boolean</code> specifying whether the use of MTOM should
   *               be enabled or disabled.
   *  @throws WebServiceException If the specified setting is not supported
   *                  by this binding instance.
  **/
  public void setMTOMEnabled(boolean flag);

  /**
   * Gets the SAAJ <code>SOAPFactory</code> instance used by this SOAP binding.
   *
   * @return SOAPFactory instance used by this SOAP binding.
  **/
  public SOAPFactory getSOAPFactory();

  /**
   * Gets the SAAJ <code>MessageFactory</code> instance used by this SOAP binding.
   *
   * @return MessageFactory instance used by this SOAP binding.
  **/
  public MessageFactory getMessageFactory();
}
