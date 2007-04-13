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

package com.sun.xml.ws.addressing;

import com.sun.xml.ws.addressing.model.InvalidMapException;
import com.sun.xml.ws.addressing.model.MapRequiredException;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLFault;
import com.sun.xml.ws.api.model.wsdl.WSDLOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLOutput;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.model.wsdl.WSDLOperationImpl;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.CheckedExceptionImpl;
import com.sun.istack.Nullable;
import com.sun.istack.NotNull;
import org.w3c.dom.Element;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
        if (wsdlPort == null)
            return action;
        WSDLBoundOperation wbo = requestPacket.getMessage().getOperation(wsdlPort);
        return getFaultAction(wbo,responsePacket);
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
            JavaMethodImpl jm = (JavaMethodImpl) requestPacket.getMessage().getMethod(seiModel);
            for (CheckedExceptionImpl ce : jm.getCheckedExceptions()) {
                if (ce.getDetailType().tagName.getLocalPart().equals(name) &&
                        ce.getDetailType().tagName.getNamespaceURI().equals(ns)) {
                    return ce.getFaultAction();
                }
            }
            return action;
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }

    String getFaultAction(@Nullable WSDLBoundOperation wbo, Packet responsePacket) {
        String action = addVer.getDefaultFaultAction();
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
            Map<String,String> map = impl.getFaultActionMap();
            if (map == null)
                return action;

            action = map.get(fault.getName());

            return action;
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }

    public String getInputAction(Packet packet) {
        String action = null;

        if (wsdlPort != null) {
            WSDLBoundOperation wbo = wsdlPort.getBinding().getOperation(packet.getMessage().getPayloadNamespaceURI(), packet.getMessage().getPayloadLocalPart());
            if (wbo != null) {
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
            WSDLBoundOperation wbo = wsdlPort.getBinding().getOperation(packet.getMessage().getPayloadNamespaceURI(), packet.getMessage().getPayloadLocalPart());
            if (wbo != null) {
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

        WSDLBoundOperation wbo = wsdlPort.getBinding().getOperation(packet.getMessage().getPayloadNamespaceURI(), packet.getMessage().getPayloadLocalPart());
        if (wbo == null)
            return false;

        WSDLOperation op = wbo.getOperation();
        return ((WSDLOperationImpl) op).getInput().isDefaultAction();

    }

    public String getSOAPAction(Packet packet) {
        String action = "";

        if (packet == null)
            return action;

        if (packet.getMessage() == null)
            return action;

        WSDLBoundOperation op = packet.getMessage().getOperation(wsdlPort);
        if (op == null)
            return action;

        action = op.getSOAPAction();

        return action;
    }

    public String getOutputAction(Packet packet) {
        String action = AddressingVersion.UNSET_OUTPUT_ACTION;
        if(seiModel!= null) {
            JavaMethodImpl jm = (JavaMethodImpl) packet.getMessage().getMethod(seiModel);
            if(jm.getOutputAction() != null && !jm.getOutputAction().equals("")) {
                return jm.getOutputAction();
            }
        }
        if (wsdlPort != null) {
            WSDLBoundOperation wbo = wsdlPort.getBinding().getOperation(packet.getMessage().getPayloadNamespaceURI(), packet.getMessage().getPayloadLocalPart());
            return getOutputAction(wbo);
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
    public SOAPFault newInvalidMapFault(InvalidMapException e, AddressingVersion av) {
        QName name = e.getMapQName();
        QName subsubcode = e.getSubsubcode();
        QName subcode = av.invalidMapTag;
        String faultstring = String.format(av.getInvalidMapText(), name, subsubcode);

        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (soapVer == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(subcode);
                fault.appendFaultSubcode(subsubcode);
                getInvalidMapDetail(name, fault.addDetail());
            } else {
                factory = SOAPVersion.SOAP_11.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(subsubcode);
            }

            fault.setFaultString(faultstring);

            return fault;
        } catch (SOAPException se) {
            throw new WebServiceException(se);
        }
    }

    public SOAPFault newMapRequiredFault(MapRequiredException e, AddressingVersion av) {
        QName subcode = av.mapRequiredTag;
        QName subsubcode = av.mapRequiredTag;
        String faultstring = av.getMapRequiredText();

        try {
            SOAPFactory factory;
            SOAPFault fault;
            if (soapVer == SOAPVersion.SOAP_12) {
                factory = SOAPVersion.SOAP_12.saajSoapFactory;
                fault = factory.createFault();
                fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(subcode);
                fault.appendFaultSubcode(subsubcode);
                getMapRequiredDetail(e.getMapQName(), fault.addDetail());
            } else {
                factory = SOAPVersion.SOAP_11.saajSoapFactory;
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

    protected Unmarshaller unmarshaller;
    protected Marshaller marshaller;
    protected SEIModel seiModel;
    protected WSDLPort wsdlPort;
    protected WSBinding binding;
    protected final SOAPVersion soapVer;
    protected final AddressingVersion addVer;
}
