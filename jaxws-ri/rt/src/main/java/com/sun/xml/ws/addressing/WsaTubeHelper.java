/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.addressing;

import com.sun.xml.ws.addressing.model.InvalidAddressingHeaderException;
import com.sun.xml.ws.addressing.model.MissingAddressingHeaderException;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.AddressingUtils;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.model.wsdl.WSDLOperationImpl;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.CheckedExceptionImpl;
import com.sun.istack.Nullable;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.util.Map;

/**
 * @author Rama Pulavarthi
 * @author Arun Gupta
 */
public abstract class WsaTubeHelper {

    public WsaTubeHelper(WSBinding binding, SEIModel seiModel, WSDLPort wsdlPort) {
        this.binding = binding;
        this.wsdlPort = wsdlPort;
        this.seiModel = seiModel;
        this.soapVer = binding.getSOAPVersion();
        this.addVer = binding.getAddressingVersion();

    }

    public String getFaultAction(Packet requestPacket, Packet responsePacket) {
        String action = null;
        if(seiModel != null) {
            action = getFaultActionFromSEIModel(requestPacket,responsePacket);
        }
        if(action != null)
            return action;
        else
            action = addVer.getDefaultFaultAction();
        if (wsdlPort != null) {
            QName wsdlOp = requestPacket.getWSDLOperation();
            if (wsdlOp != null) {
                WSDLBoundOperation wbo = wsdlPort.getBinding().get(wsdlOp);
                return getFaultAction(wbo, responsePacket);
            }
        }
        return action;
    }                        

    String getFaultActionFromSEIModel(Packet requestPacket, Packet responsePacket) {
        String action = null;
        if (seiModel == null || wsdlPort == null)
            return action;

        try {
            SOAPMessage sm = responsePacket.getMessage().copy().readAsSOAPMessage();
            if (sm == null)
                return action;

            if (sm.getSOAPBody() == null)
                return action;

            if (sm.getSOAPBody().getFault() == null)
                return action;

            Detail detail = sm.getSOAPBody().getFault().getDetail();
            if (detail == null)
                return action;

            String ns = detail.getFirstChild().getNamespaceURI();
            String name = detail.getFirstChild().getLocalName();

            QName wsdlOp = requestPacket.getWSDLOperation();
            JavaMethodImpl jm = (JavaMethodImpl) seiModel.getJavaMethodForWsdlOperation(wsdlOp);
            if (jm != null) {
              for (CheckedExceptionImpl ce : jm.getCheckedExceptions()) {
                  if (ce.getDetailType().tagName.getLocalPart().equals(name) &&
                          ce.getDetailType().tagName.getNamespaceURI().equals(ns)) {
                      return ce.getFaultAction();
                  }
              }
            }
            return action;
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }

    String getFaultAction(@Nullable WSDLBoundOperation wbo, Packet responsePacket) {
    	String action = AddressingUtils.getAction(responsePacket.getMessage().getMessageHeaders(), addVer, soapVer);
    	if (action != null)
    		return action;
    	
        action = addVer.getDefaultFaultAction();
        if (wbo == null)
            return action;

        try {
            SOAPMessage sm = responsePacket.getMessage().copy().readAsSOAPMessage();
            if (sm == null)
                return action;

            if (sm.getSOAPBody() == null)
                return action;

            if (sm.getSOAPBody().getFault() == null)
                return action;

            Detail detail = sm.getSOAPBody().getFault().getDetail();
            if (detail == null)
                return action;

            String ns = detail.getFirstChild().getNamespaceURI();
            String name = detail.getFirstChild().getLocalName();

            WSDLOperation o = wbo.getOperation();

            WSDLFault fault = o.getFault(new QName(ns, name));
            if (fault == null)
                return action;

            WSDLOperationImpl impl = (WSDLOperationImpl)o;
            action = fault.getAction();

            return action;
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }

    public String getInputAction(Packet packet) {
        String action = null;

        if (wsdlPort != null) {
            QName wsdlOp = packet.getWSDLOperation();
            if (wsdlOp != null) {
                WSDLBoundOperation wbo = wsdlPort.getBinding().get(wsdlOp);
                WSDLOperation op = wbo.getOperation();
                action = op.getInput().getAction();
            }
        }

        return action;
    }

    /**
     * This method gives the Input addressing Action for a message.
     * It gives the Action set in the wsdl operation for the corresponding payload.
     * If it is not explicitly set, it gives the soapAction
     * @param packet
     * @return input Action
     */
    public String getEffectiveInputAction(Packet packet) {
        //non-default SOAPAction beomes wsa:action
        if(packet.soapAction != null && !packet.soapAction.equals(""))
                return packet.soapAction;
        String action = null;

        if (wsdlPort != null) {
            QName wsdlOp = packet.getWSDLOperation();
            if (wsdlOp != null) {
                WSDLBoundOperation wbo = wsdlPort.getBinding().get(wsdlOp);
                WSDLOperation op = wbo.getOperation();
                action = op.getInput().getAction();
            } else
                action = packet.soapAction;
        } else {
            action = packet.soapAction;
        }
        return action;
    }

    public boolean isInputActionDefault(Packet packet) {
        if (wsdlPort == null)
            return false;
        QName wsdlOp = packet.getWSDLOperation();
        if(wsdlOp == null)
            return false;
        WSDLBoundOperation wbo = wsdlPort.getBinding().get(wsdlOp);
        WSDLOperation op = wbo.getOperation();
        return ((WSDLOperationImpl) op).getInput().isDefaultAction();

    }

    public String getSOAPAction(Packet packet) {
        String action = "";

        if (packet == null || packet.getMessage() == null)
            return action;

        if (wsdlPort == null)
            return action;

        QName opName = packet.getWSDLOperation();
        if(opName == null)
            return action;

        WSDLBoundOperation op = wsdlPort.getBinding().get(opName);
        action = op.getSOAPAction();
        return action;
    }

    public String getOutputAction(Packet packet) {
        //String action = AddressingVersion.UNSET_OUTPUT_ACTION;
        String action = null;
        QName wsdlOp = packet.getWSDLOperation();
        if (wsdlOp != null) {
            if (seiModel != null) {
                JavaMethodImpl jm = (JavaMethodImpl) seiModel.getJavaMethodForWsdlOperation(wsdlOp);
                if (jm != null && jm.getOutputAction() != null && !jm.getOutputAction().equals("")) {
                    return jm.getOutputAction();
                }
            }
            if (wsdlPort != null) {
                WSDLBoundOperation wbo = wsdlPort.getBinding().get(wsdlOp);
                return getOutputAction(wbo);
            }
        }
        return action;
    }

    String getOutputAction(@Nullable WSDLBoundOperation wbo) {
        String action = AddressingVersion.UNSET_OUTPUT_ACTION;
        if (wbo != null) {
            WSDLOutput op = wbo.getOperation().getOutput();
            if (op != null)
                action = op.getAction();
        }
        return action;
    }
    
    public SOAPFault createInvalidAddressingHeaderFault(InvalidAddressingHeaderException e, AddressingVersion av) {
        QName name = e.getProblemHeader();
        QName subsubcode = e.getSubsubcode();
        QName subcode = av.invalidMapTag;
        String faultstring = String.format(av.getInvalidMapText(), name, subsubcode);

        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (soapVer == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.getSOAPFactory();
                fault = factory.createFault();
                fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(subcode);
                fault.appendFaultSubcode(subsubcode);
                getInvalidMapDetail(name, fault.addDetail());
            } else {
                factory = SOAPVersion.SOAP_11.getSOAPFactory();
                fault = factory.createFault();
                fault.setFaultCode(subsubcode);
            }

            fault.setFaultString(faultstring);

            return fault;
        } catch (SOAPException se) {
            throw new WebServiceException(se);
        }
    }

    public SOAPFault newMapRequiredFault(MissingAddressingHeaderException e) {
        QName subcode = addVer.mapRequiredTag;
        QName subsubcode = addVer.mapRequiredTag;
        String faultstring = addVer.getMapRequiredText();

        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (soapVer == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.getSOAPFactory();
                fault = factory.createFault();
                fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(subcode);
                fault.appendFaultSubcode(subsubcode);
                getMapRequiredDetail(e.getMissingHeaderQName(), fault.addDetail());
            } else {
                factory = SOAPVersion.SOAP_11.getSOAPFactory();
                fault = factory.createFault();
                fault.setFaultCode(subsubcode);
            }

            fault.setFaultString(faultstring);

            return fault;
        } catch (SOAPException se) {
            throw new WebServiceException(se);
        }
    }

    public abstract void getProblemActionDetail(String action, Element element);
    public abstract void getInvalidMapDetail(QName name, Element element);
    public abstract void getMapRequiredDetail(QName name, Element element);
    
    protected SEIModel seiModel;
    protected WSDLPort wsdlPort;
    protected WSBinding binding;
    protected final SOAPVersion soapVer;
    protected final AddressingVersion addVer;
}
