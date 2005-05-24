/*
 * $Id: Elemental.java,v 1.1 2005-05-24 14:04:10 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import javax.xml.namespace.QName;

/**
 * Interface implemented by classes that are mappable to XML elements.
 *
 * @author JAX-RPC Development Team
 */
public interface Elemental {
    public QName getElementName();
}
