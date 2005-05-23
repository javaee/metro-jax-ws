/**
 * $Id: JAXBTypeVisitor.java,v 1.1 2005-05-23 23:18:53 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.model.jaxb;

/**
 * @author Vivek Pandey
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface JAXBTypeVisitor {
    public void visit(JAXBType type) throws Exception;
    public void visit(RpcLitStructure type) throws Exception;
}
