/*
 * $Id: QNameAction.java,v 1.2 2005-07-18 18:14:21 kohlert Exp $
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
 * @author WS Development Team
 */
public interface QNameAction {
    public void perform(QName name);
}
