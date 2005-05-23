/**
 * $Id: InternalBindingProvider.java,v 1.1 2005-05-23 22:26:36 bbissett Exp $
 */
/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.client;


/**
 * A utility interface for all the classes
 * that implement BindingProvider.
 *
 * @see com.sun.xml.rpc.client.BasicService#setBindingOnProvider()
 */
public interface InternalBindingProvider {

    public void _setBinding(BindingImpl binding);

}
