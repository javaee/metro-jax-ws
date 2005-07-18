/*
 * $Id: ParserListener.java,v 1.2 2005-07-18 18:14:21 kohlert Exp $
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
 * @author WS Development Team
 */
public interface ParserListener {
    public void ignoringExtension(QName name, QName parent);
    public void doneParsingEntity(QName element, Entity entity);
}
