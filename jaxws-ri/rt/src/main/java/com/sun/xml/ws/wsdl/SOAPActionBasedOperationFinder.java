/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.WSDLOperationMapping;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.JavaMethodImpl;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link WSDLOperationFinder} that uses SOAPAction as the key for dispatching.
 * <p/>
 * A map of all SOAPAction on the port and the corresponding WSDL operation QName
 * is initialized in the constructor. The SOAPAction from the
 * request {@link com.sun.xml.ws.api.message.Packet} is used as the key to identify the associated wsdl operation.
 *
 * @author Jitendra Kotamraju
 */
final class SOAPActionBasedOperationFinder extends WSDLOperationFinder {
    private final Map<String, WSDLOperationMapping> methodHandlers;

    public SOAPActionBasedOperationFinder(WSDLPort wsdlModel, WSBinding binding, @Nullable SEIModel seiModel) {
        super(wsdlModel,binding,seiModel);
        methodHandlers = new HashMap<String, WSDLOperationMapping>();

        // Find if any SOAPAction repeat for operations
        Map<String, Integer> unique = new HashMap<String, Integer>();
        if (seiModel != null) {
            for (JavaMethodImpl m : ((AbstractSEIModelImpl) seiModel).getJavaMethods()) {
                String soapAction = m.getSOAPAction();
                Integer count = unique.get(soapAction);
                if (count == null) {
                    unique.put(soapAction, 1);
                } else {
                    unique.put(soapAction, ++count);
                }
            }

            for (JavaMethodImpl m : ((AbstractSEIModelImpl) seiModel).getJavaMethods()) {
                String soapAction = m.getSOAPAction();
                // Set up method handlers only for unique SOAPAction values so
                // that dispatching happens consistently for a method
                if (unique.get(soapAction) == 1) {
                    methodHandlers.put('"' + soapAction + '"', wsdlOperationMapping(m));
                }
            }
        } else {
            for(WSDLBoundOperation wsdlOp: wsdlModel.getBinding().getBindingOperations()) {
                methodHandlers.put(wsdlOp.getSOAPAction(), wsdlOperationMapping(wsdlOp));
            }
        }

    }

//  public QName getWSDLOperationQName(Packet request) {
    public WSDLOperationMapping getWSDLOperationMapping(Packet request) throws DispatchException {
        return request.soapAction == null ? null : methodHandlers.get(request.soapAction);
    }
}
