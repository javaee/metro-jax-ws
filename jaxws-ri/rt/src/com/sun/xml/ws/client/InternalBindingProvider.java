/**
 * $Id: InternalBindingProvider.java,v 1.3 2005-07-20 20:28:22 kwalsh Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.binding.soap.BindingImpl;


/**
 * A utility interface for all the classes
 * that implement BindingProvider.
 *
 * @see com.sun.xml.rpc.client.BasicService#setBindingOnProvider()
 */
public interface InternalBindingProvider {
    public void _setBinding(BindingImpl binding);
}
