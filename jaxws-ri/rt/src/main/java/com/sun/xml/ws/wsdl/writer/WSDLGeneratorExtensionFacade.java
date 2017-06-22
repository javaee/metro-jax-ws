/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.wsdl.writer;

import com.sun.istack.NotNull;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.wsdl.writer.WSDLGenExtnContext;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;

/**
 * {@link WSDLGeneratorExtension} that delegates to
 * multiple {@link WSDLGeneratorExtension}s.
 *
 * <p>
 * This simplifies {@link WSDLGenerator} since it now
 * only needs to work with one {@link WSDLGeneratorExtension}.
 *
 *
 * @author Doug Kohlert
 */
final class WSDLGeneratorExtensionFacade extends WSDLGeneratorExtension {
    private final WSDLGeneratorExtension[] extensions;

    WSDLGeneratorExtensionFacade(WSDLGeneratorExtension... extensions) {
        assert extensions!=null;
        this.extensions = extensions;
    }

    public void start(WSDLGenExtnContext ctxt) {
        for (WSDLGeneratorExtension e : extensions)
            e.start(ctxt);
    }

    public void end(@NotNull WSDLGenExtnContext ctxt) {
        for (WSDLGeneratorExtension e : extensions)
            e.end(ctxt);
    }

    public void addDefinitionsExtension(TypedXmlWriter definitions) {
        for (WSDLGeneratorExtension e : extensions)
            e.addDefinitionsExtension(definitions);
    }

    public void addServiceExtension(TypedXmlWriter service) {
        for (WSDLGeneratorExtension e : extensions)
            e.addServiceExtension(service);
    }

    public void addPortExtension(TypedXmlWriter port) {
        for (WSDLGeneratorExtension e : extensions)
            e.addPortExtension(port);
    }

    public void addPortTypeExtension(TypedXmlWriter portType) {
        for (WSDLGeneratorExtension e : extensions)
            e.addPortTypeExtension(portType);
    }

    public void addBindingExtension(TypedXmlWriter binding) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingExtension(binding);
    }

    public void addOperationExtension(TypedXmlWriter operation, JavaMethod method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOperationExtension(operation, method);
    }

    public void addBindingOperationExtension(TypedXmlWriter operation, JavaMethod method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingOperationExtension(operation, method);
    }

    public void addInputMessageExtension(TypedXmlWriter message, JavaMethod method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addInputMessageExtension(message, method);
    }

    public void addOutputMessageExtension(TypedXmlWriter message, JavaMethod method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOutputMessageExtension(message, method);
    }

    public void addOperationInputExtension(TypedXmlWriter input, JavaMethod method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOperationInputExtension(input, method);
    }

    public void addOperationOutputExtension(TypedXmlWriter output, JavaMethod method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOperationOutputExtension(output, method);
    }

    public void addBindingOperationInputExtension(TypedXmlWriter input, JavaMethod method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingOperationInputExtension(input, method);
    }

    public void addBindingOperationOutputExtension(TypedXmlWriter output, JavaMethod method) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingOperationOutputExtension(output, method);
    }

    public void addBindingOperationFaultExtension(TypedXmlWriter fault, JavaMethod method, CheckedException ce) {
        for (WSDLGeneratorExtension e : extensions)
            e.addBindingOperationFaultExtension(fault, method, ce);
    }

    public void addFaultMessageExtension(TypedXmlWriter message, JavaMethod method, CheckedException ce) {
        for (WSDLGeneratorExtension e : extensions)
            e.addFaultMessageExtension(message, method, ce);
    }

    public void addOperationFaultExtension(TypedXmlWriter fault, JavaMethod method, CheckedException ce) {
        for (WSDLGeneratorExtension e : extensions)
            e.addOperationFaultExtension(fault, method, ce);
    }
}
