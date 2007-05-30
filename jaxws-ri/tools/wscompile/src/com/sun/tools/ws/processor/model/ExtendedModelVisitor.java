/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.tools.ws.processor.model;

import java.util.Iterator;

/**
 *
 * A model visitor incorporating all the logic required to walk through the model.
 *
 * @author WS Development Team
 */
public class ExtendedModelVisitor {

    public ExtendedModelVisitor() {}

    public void visit(Model model) throws Exception {
        preVisit(model);
        for (Service service : model.getServices()) {
            preVisit(service);
            for (Port port : service.getPorts()) {
                preVisit(port);
                if (shouldVisit(port)) {
                    for (Operation operation : port.getOperations()) {                        
                        preVisit(operation);
                        Request request = operation.getRequest();
                        if (request != null) {
                            preVisit(request);
                            for (Iterator iter4 = request.getHeaderBlocks();
                                iter4.hasNext();) {

                                Block block = (Block) iter4.next();
                                visitHeaderBlock(block);
                            }
                            for (Iterator iter4 = request.getBodyBlocks();
                                iter4.hasNext();) {

                                Block block = (Block) iter4.next();
                                visitBodyBlock(block);
                            }
                            for (Iterator iter4 = request.getParameters();
                                iter4.hasNext();) {

                                Parameter parameter = (Parameter) iter4.next();
                                visit(parameter);
                            }
                            postVisit(request);
                        }

                        Response response = operation.getResponse();
                        if (response != null) {
                            preVisit(response);
                            for (Iterator iter4 = response.getHeaderBlocks();
                                iter4.hasNext();) {

                                Block block = (Block) iter4.next();
                                visitHeaderBlock(block);
                            }
                            for (Iterator iter4 = response.getBodyBlocks();
                                iter4.hasNext();) {

                                Block block = (Block) iter4.next();
                                visitBodyBlock(block);
                            }
                            for (Iterator iter4 = response.getParameters();
                                iter4.hasNext();) {

                                Parameter parameter = (Parameter) iter4.next();
                                visit(parameter);
                            }
                            postVisit(response);
                        }

                        for (Iterator iter4 = operation.getFaults();
                            iter4.hasNext();) {

                            Fault fault = (Fault) iter4.next();
                            preVisit(fault);
                            visitFaultBlock(fault.getBlock());
                            postVisit(fault);
                        }
                        postVisit(operation);
                    }
                }
                postVisit(port);
            }
            postVisit(service);
        }
        postVisit(model);
    }

    protected boolean shouldVisit(Port port) {
        return true;
    }

    // these methods are intended for subclasses
    protected void preVisit(Model model) throws Exception {}
    protected void postVisit(Model model) throws Exception {}
    protected void preVisit(Service service) throws Exception {}
    protected void postVisit(Service service) throws Exception {}
    protected void preVisit(Port port) throws Exception {}
    protected void postVisit(Port port) throws Exception {}
    protected void preVisit(Operation operation) throws Exception {}
    protected void postVisit(Operation operation) throws Exception {}
    protected void preVisit(Request request) throws Exception {}
    protected void postVisit(Request request) throws Exception {}
    protected void preVisit(Response response) throws Exception {}
    protected void postVisit(Response response) throws Exception {}
    protected void preVisit(Fault fault) throws Exception {}
    protected void postVisit(Fault fault) throws Exception {}
    protected void visitBodyBlock(Block block) throws Exception {}
    protected void visitHeaderBlock(Block block) throws Exception {}
    protected void visitFaultBlock(Block block) throws Exception {}
    protected void visit(Parameter parameter) throws Exception {}
}
