/*
 * $Id: QNameAction.java,v 1.1 2005-05-24 14:04:15 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import javax.xml.namespace.QName;

/**
 * An action operating on a QName.
 *
 * @author JAX-RPC Development Team
 */
public interface QNameAction {
    public void perform(QName name);
}
