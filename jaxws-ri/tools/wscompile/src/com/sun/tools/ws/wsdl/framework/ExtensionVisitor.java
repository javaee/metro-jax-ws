/*
 * $Id: ExtensionVisitor.java,v 1.1 2005-05-24 14:04:13 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * A visitor working on extension entities.
 *
 * @author JAX-RPC Development Team
 */
public interface ExtensionVisitor {
    public void preVisit(Extension extension) throws Exception;
    public void postVisit(Extension extension) throws Exception;
}
