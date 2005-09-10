/*
 * $Id: ModelVisitor.java,v 1.3 2005-09-10 19:49:38 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
