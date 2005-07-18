/*
 * $Id: ExtensionVisitor.java,v 1.2 2005-07-18 18:14:20 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * A visitor working on extension entities.
 *
 * @author WS Development Team
 */
public interface ExtensionVisitor {
    public void preVisit(Extension extension) throws Exception;
    public void postVisit(Extension extension) throws Exception;
}
