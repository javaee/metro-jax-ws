/**
 * $Id: InternalBindingProvider.java,v 1.4 2005-07-23 04:10:00 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.binding.soap.BindingImpl;


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
