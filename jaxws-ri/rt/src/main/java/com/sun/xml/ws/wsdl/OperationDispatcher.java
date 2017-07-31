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

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.WSDLOperationMapping;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.fault.SOAPFaultBuilder;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.text.MessageFormat;

/**
 * This class abstracts the process of identifying the wsdl operation from a SOAP Message request.
 * This is primarily for dispatching the request messages to an endpoint method.
 *
 * Different implementations of {@link WSDLOperationFinder} are used underneath to identify the wsdl operation based on
 * if AddressingFeature is enabled or not.
 * 
 * @author Rama Pulavarthi
 */
public class OperationDispatcher {
    private List<WSDLOperationFinder> opFinders;
    private WSBinding binding;

    public OperationDispatcher(@NotNull WSDLPort wsdlModel, @NotNull WSBinding binding, @Nullable SEIModel seiModel) {
        this.binding = binding;
        opFinders = new ArrayList<WSDLOperationFinder>();
        if (binding.getAddressingVersion() != null) {
            opFinders.add(new ActionBasedOperationFinder(wsdlModel, binding, seiModel));
        }
        opFinders.add(new PayloadQNameBasedOperationFinder(wsdlModel, binding, seiModel));
        opFinders.add(new SOAPActionBasedOperationFinder(wsdlModel, binding, seiModel));

    }

    /**
     * @deprecated use getWSDLOperationMapping(Packet request)
     * @param request Packet
     * @return QName of the wsdl operation.
     * @throws DispatchException if a unique operartion cannot be associated with this packet.
     */
    public @NotNull QName getWSDLOperationQName(Packet request) throws DispatchException {
        WSDLOperationMapping m = getWSDLOperationMapping(request);
        return m != null ? m.getOperationName() : null;
    }

    public @NotNull WSDLOperationMapping getWSDLOperationMapping(Packet request) throws DispatchException {
        WSDLOperationMapping opName;
        for(WSDLOperationFinder finder: opFinders) {
            opName = finder.getWSDLOperationMapping(request);
            if(opName != null)
                return opName;
        }
        //No way to dispatch this request
        String err = MessageFormat.format("Request=[SOAPAction={0},Payload='{'{1}'}'{2}]",
                request.soapAction, request.getMessage().getPayloadNamespaceURI(),
                request.getMessage().getPayloadLocalPart());
        String faultString = ServerMessages.DISPATCH_CANNOT_FIND_METHOD(err);
        Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(
                binding.getSOAPVersion(), faultString, binding.getSOAPVersion().faultCodeClient);
        throw new DispatchException(faultMsg);
    }
}
