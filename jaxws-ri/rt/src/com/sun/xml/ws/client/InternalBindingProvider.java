/**
 * $Id: InternalBindingProvider.java,v 1.6 2005-08-04 02:32:21 kwalsh Exp $
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
 *      QName, URI)
 */
public interface InternalBindingProvider {
    public void _setBinding(BindingImpl binding);
}
