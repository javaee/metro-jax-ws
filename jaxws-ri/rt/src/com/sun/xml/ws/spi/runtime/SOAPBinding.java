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
}
