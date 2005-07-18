/*
 * $Id: ExtensionVisitorBase.java,v 1.2 2005-07-18 18:14:20 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * A base class for extension visitors.
 *
 * @author WS Development Team
 */
public class ExtensionVisitorBase implements ExtensionVisitor {
    public ExtensionVisitorBase() {
    }

    public void preVisit(Extension extension) throws Exception {
    }
    public void postVisit(Extension extension) throws Exception {
    }
}
