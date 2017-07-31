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

package com.sun.xml.ws.api.wsdl.parser;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.wsdl.editable.EditableWSDLModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.policy.PolicyResolver;

/**
 * Provides contextual information for {@link WSDLParserExtension}s.
 * 
 * @author Vivek Pandey
 * @author Fabian Ritzmann
 */
public interface WSDLParserExtensionContext {
    /**
     * Returns true if the WSDL parsing is happening on the client side. Returns false means
     * its started on the server side.
     */
    boolean isClientSide();

    /**
     * Gives the {@link EditableWSDLModel}. The WSDLModel may not be complete until
     * {@link WSDLParserExtension#finished(WSDLParserExtensionContext)} is called.
     */
    EditableWSDLModel getWSDLModel();

    /**
     * Provides the {@link Container} in which this service or client is running.
     * May return null.
     *
     * @return The container in which this service or client is running.
     */
    @NotNull Container getContainer();


    /**
     * Provides the PolicyResolver
     */
    @NotNull
    PolicyResolver getPolicyResolver();
}
