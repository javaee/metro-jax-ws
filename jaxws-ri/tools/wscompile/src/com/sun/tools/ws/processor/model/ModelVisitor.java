/*
 * $Id: ModelVisitor.java,v 1.2 2005-07-18 18:14:00 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model;

/**
 *
 * @author WS Development Team
 */
public interface ModelVisitor {
    public void visit(Model model) throws Exception;
    public void visit(Service service) throws Exception;
    public void visit(Port port) throws Exception;
    public void visit(Operation operation) throws Exception;
    public void visit(Request request) throws Exception;
    public void visit(Response response) throws Exception;
    public void visit(Fault fault) throws Exception;
    public void visit(Block block) throws Exception;
    public void visit(Parameter parameter) throws Exception;
}
