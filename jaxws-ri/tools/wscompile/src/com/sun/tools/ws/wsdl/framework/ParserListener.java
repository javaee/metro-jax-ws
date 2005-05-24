/*
 * $Id: ParserListener.java,v 1.1 2005-05-24 14:04:15 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import javax.xml.namespace.QName;

/**
 * A listener for parsing-related events.
 *
 * @author JAX-RPC Development Team
 */
public interface ParserListener {
    public void ignoringExtension(QName name, QName parent);
    public void doneParsingEntity(QName element, Entity entity);
}
