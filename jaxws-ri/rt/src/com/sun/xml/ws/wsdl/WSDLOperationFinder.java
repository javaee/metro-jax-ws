/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.wsdl;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.WSBinding;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.namespace.QName;

/**
 * Extensions if this class will be used for dispatching the request message to the correct endpoint method by
 * identifying the wsdl operation associated with the request.
 *
 * @See OperationDispatcher
 * 
 * @author Rama Pulavarthi
 */
public abstract class WSDLOperationFinder {
    protected final WSDLPort wsdlModel;
    protected final WSBinding binding;
    protected final SEIModel seiModel;

    public WSDLOperationFinder(@NotNull WSDLPort wsdlModel, @NotNull WSBinding binding, @Nullable SEIModel seiModel) {
        this.wsdlModel = wsdlModel;
        this.binding = binding;
        this.seiModel= seiModel;
    }

    /**
     * This methods returns the QName of the WSDL operation correponding to a request Packet.
     * An implementation should return null when it cannot dispatch to a unique method based on the information it processes.
     * In such case, other OperationFinders are queried to resolve a WSDL operation.
     * It should throw an instance of DispatchException if it finds incorrect information in the packet.
     *
     * @param request  Request Packet that is used to find the associated WSDLOperation
     * @return QName of the WSDL Operation that this request correponds to.
     *          null when it cannot find a unique operation to dispatch to.
     * @throws DispatchException When the information in the Packet is invalid
     */
    public abstract QName getWSDLOperationQName(Packet request) throws DispatchException;
}
