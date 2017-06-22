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

package com.sun.xml.ws.api.wsdl.writer;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Container;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

/**
 * WSDLGeneatorContext provides a context for the WSDLGeneratorExtension and is used in
 * {@link WSDLGeneratorExtension#start(WSDLGenExtnContext)}. This context consists of TXW, {@link SEIModel},
 * {@link WSBinding}, {@link Container}, and implementation class. WSDL extensions are used to
 * extend the generated WSDL by adding implementation specific extensions.
 *
 * @author Jitendra Kotamraju
 */
public class WSDLGenExtnContext {
    private final TypedXmlWriter root;
    private final SEIModel model;
    private final WSBinding binding;
    private final Container container;
    private final Class endpointClass;

    /**
     * Constructs WSDL Generation context for the extensions
     *
     * @param root      This is the root element of the generated WSDL.
     * @param model     WSDL is being generated from this {@link SEIModel}.
     * @param binding   The binding for which we generate WSDL. the binding {@link WSBinding} represents a particular
     *                  configuration of JAXWS. This can be typically be overriden by
     * @param container The entry point to the external environment.
     *                  If this extension is used at the runtime to generate WSDL, you get a {@link Container}
     *                  that was given to {@link com.sun.xml.ws.api.server.WSEndpoint#create}.
     */
    public WSDLGenExtnContext(@NotNull TypedXmlWriter root, @NotNull SEIModel model, @NotNull WSBinding binding,
                              @Nullable Container container, @NotNull Class endpointClass) {
        this.root = root;
        this.model = model;
        this.binding = binding;
        this.container = container;
        this.endpointClass = endpointClass;
    }

    public TypedXmlWriter getRoot() {
        return root;
    }

    public SEIModel getModel() {
        return model;
    }

    public WSBinding getBinding() {
        return binding;
    }

    public Container getContainer() {
        return container;
    }

    public Class getEndpointClass() {
        return endpointClass;
    }

}
