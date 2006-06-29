/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.wsdl.document;

import com.sun.tools.ws.wsdl.framework.ExtensionVisitor;

/**
 * A visitor for WSDL documents.
 *
 * @author WS Development Team
 */
public interface WSDLDocumentVisitor extends ExtensionVisitor {

    public void preVisit(Definitions definitions) throws Exception;
    public void postVisit(Definitions definitions) throws Exception;
    public void visit(Import i) throws Exception;
    public void preVisit(Types types) throws Exception;
    public void postVisit(Types types) throws Exception;
    public void preVisit(Message message) throws Exception;
    public void postVisit(Message message) throws Exception;
    public void visit(MessagePart part) throws Exception;
    public void preVisit(PortType portType) throws Exception;
    public void postVisit(PortType portType) throws Exception;
    public void preVisit(Operation operation) throws Exception;
    public void postVisit(Operation operation) throws Exception;
    public void preVisit(Input input) throws Exception;
    public void postVisit(Input input) throws Exception;
    public void preVisit(Output output) throws Exception;
    public void postVisit(Output output) throws Exception;
    public void preVisit(Fault fault) throws Exception;
    public void postVisit(Fault fault) throws Exception;
    public void preVisit(Binding binding) throws Exception;
    public void postVisit(Binding binding) throws Exception;
    public void preVisit(BindingOperation operation) throws Exception;
    public void postVisit(BindingOperation operation) throws Exception;
    public void preVisit(BindingInput input) throws Exception;
    public void postVisit(BindingInput input) throws Exception;
    public void preVisit(BindingOutput output) throws Exception;
    public void postVisit(BindingOutput output) throws Exception;
    public void preVisit(BindingFault fault) throws Exception;
    public void postVisit(BindingFault fault) throws Exception;
    public void preVisit(Service service) throws Exception;
    public void postVisit(Service service) throws Exception;
    public void preVisit(Port port) throws Exception;
    public void postVisit(Port port) throws Exception;
    public void visit(Documentation documentation) throws Exception;
}
