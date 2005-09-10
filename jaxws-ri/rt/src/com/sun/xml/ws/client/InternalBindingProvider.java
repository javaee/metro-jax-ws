/**
 * $Id: InternalBindingProvider.java,v 1.7 2005-09-10 19:47:26 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
