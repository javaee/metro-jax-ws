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

package com.sun.xml.ws.wsdl;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.WSDLOperationMapping;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.util.QNameMap;
import com.sun.xml.ws.wsdl.DispatchException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An {@link WSDLOperationFinder} that uses SOAP payload first child's QName as the key for dispatching.
 * <p/>
 * A map of all payload QNames that the operations in the port allow and the corresponding QName of the wsdl operation
 * is initialized in the constructor. The payload QName is extracted from the
 * request {@link com.sun.xml.ws.api.message.Packet} and used to identify the wsdl operation.
 *
 * @author Rama Pulavarthi
 * @author Arun Gupta
 * @author Jitendra Kotamraju
 */
final class PayloadQNameBasedOperationFinder extends WSDLOperationFinder {
    private static final Logger LOGGER = Logger.getLogger(PayloadQNameBasedOperationFinder.class.getName());

    public static final String EMPTY_PAYLOAD_LOCAL = "";
    public static final String EMPTY_PAYLOAD_NSURI = "";
    public static final QName EMPTY_PAYLOAD = new QName(EMPTY_PAYLOAD_NSURI, EMPTY_PAYLOAD_LOCAL);

    private final QNameMap<WSDLOperationMapping> methodHandlers = new QNameMap<WSDLOperationMapping>();
    private final QNameMap<List<String>> unique = new QNameMap<List<String>>();


    public PayloadQNameBasedOperationFinder(WSDLPort wsdlModel, WSBinding binding, @Nullable SEIModel seiModel) {
        super(wsdlModel,binding,seiModel);
        if (seiModel != null) {
            // Find if any payload QNames repeat for operations
            for (JavaMethodImpl m : ((AbstractSEIModelImpl) seiModel).getJavaMethods()) {
                if(m.getMEP().isAsync)
                    continue;
                QName name = m.getRequestPayloadName();
                if (name == null)
                    name = EMPTY_PAYLOAD;
                List<String> methods = unique.get(name);
                if (methods == null) {
                    methods = new ArrayList<String>();
                    unique.put(name, methods);
                }
                methods.add(m.getMethod().getName());
            }

            // Log warnings about non unique payload QNames
            for (QNameMap.Entry<List<String>> e : unique.entrySet()) {
                if (e.getValue().size() > 1) {
                    LOGGER.warning(ServerMessages.NON_UNIQUE_DISPATCH_QNAME(e.getValue(), e.createQName()));
                }
            }

            for (JavaMethodImpl m : ((AbstractSEIModelImpl) seiModel).getJavaMethods()) {
                QName name = m.getRequestPayloadName();
                if (name == null)
                    name = EMPTY_PAYLOAD;
                // Set up method handlers only for unique QNames. So that dispatching
                // happens consistently for a method
                if (unique.get(name).size() == 1) {
                    methodHandlers.put(name, wsdlOperationMapping(m));
                }
            }
        } else {
            for (WSDLBoundOperation wsdlOp : wsdlModel.getBinding().getBindingOperations()) {
                QName name = wsdlOp.getRequestPayloadName();
                if (name == null)
                    name = EMPTY_PAYLOAD;
                methodHandlers.put(name, wsdlOperationMapping(wsdlOp));
            }
        }
    }

    /**
     *
     * @return not null if it finds a unique handler for the request
     *         null if it cannot idenitify a unique wsdl operation from the Payload QName.
     *  
     * @throws DispatchException if the payload itself is incorrect, this happens when the payload is not accepted by
     *          any operation in the port.
     */
//  public QName getWSDLOperationQName(Packet request) throws DispatchException{

    public WSDLOperationMapping getWSDLOperationMapping(Packet request) throws DispatchException {
        Message message = request.getMessage();
        String localPart = message.getPayloadLocalPart();
        String nsUri;
        if (localPart == null) {
            localPart = EMPTY_PAYLOAD_LOCAL;
            nsUri = EMPTY_PAYLOAD_NSURI;
        } else {
            nsUri = message.getPayloadNamespaceURI();
            if(nsUri == null)
                nsUri = EMPTY_PAYLOAD_NSURI;
        }
        WSDLOperationMapping op = methodHandlers.get(nsUri, localPart);

        // Check if payload itself is correct. Usually it is, so let us check last
        if (op == null && !unique.containsKey(nsUri,localPart)) {
            String dispatchKey = "{" + nsUri + "}" + localPart;
            String faultString = ServerMessages.DISPATCH_CANNOT_FIND_METHOD(dispatchKey);
            throw new DispatchException(SOAPFaultBuilder.createSOAPFaultMessage(
                 binding.getSOAPVersion(), faultString, binding.getSOAPVersion().faultCodeClient));
        }
        return op;
    }
}
