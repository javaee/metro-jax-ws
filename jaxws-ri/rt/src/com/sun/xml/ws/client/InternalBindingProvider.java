/**
 * $Id: InternalBindingProvider.java,v 1.5 2005-07-27 18:50:01 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.binding.BindingImpl;


/**
 * A utility interface for all the classes
 * that implement BindingProvider.
 *
 * @see WebService#setBindingOnProvider(InternalBindingProvider,
 *                                       QName, URI)
 */
public interface InternalBindingProvider {
    public void _setBinding(BindingImpl binding);
}
