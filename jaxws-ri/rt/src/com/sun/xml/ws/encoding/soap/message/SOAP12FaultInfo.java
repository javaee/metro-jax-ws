/**
 * $Id: SOAP12FaultInfo.java,v 1.1 2005-06-04 01:48:14 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.pept.ept.MessageInfo;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.soap.Detail;
import java.util.List;

/**
 * SOAP 1.2 soap fault info class
 */
public class SOAP12FaultInfo{
    private FaultCode code;
    private FaultReason reason;
    private String node;
    private String role;
    private List detail;

    public SOAP12FaultInfo(FaultCode code, FaultReason reason,  String node, String role, List detail) {
        this.code = code;
        this.reason = reason;
        this.node = node;
        this.role = role;
        this.detail = detail;
    }

    public FaultCode getCode() {
        return code;
    }

    public FaultReason getReasons() {
        return reason;
    }

    public String getNode() {
        return node;
    }

    public String getRole() {
        return role;
    }

    public List getDetail() {
        return detail;
    }

    public void write(XMLStreamWriter writer, MessageInfo mi){
        try {
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAP12Constants.QNAME_SOAP_FAULT.getLocalPart(),
                SOAP12Constants.QNAME_SOAP_FAULT.getNamespaceURI());

            code.write(writer); //<soapenv:Code> ... </soapenv:Code>
            reason.write(writer);


            //<soapenv:Node>...</soapenv:Node>
            if (node != null) {
                writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                    SOAP12Constants.QNAME_FAULT_NODE.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);
                writer.writeCharacters(node);
                writer.writeEndElement();
            }

            //<soapenv:Role>...</soapenv:Role>
            if(role != null){
                writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                    SOAP12Constants.QNAME_FAULT_ROLE.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);
                writer.writeCharacters(role);
                writer.writeEndElement();
            }

            //<soapenv:Detail>...</soapenv:Detail>
            if(detail != null){
                for(Object obj : detail){
                    if (obj != null) {
                        // Not RuntimeException, Not header fault
                        if (obj instanceof Detail) {
                            // SOAPFaultException
                            //encodeDetail((Detail)detail, writer);
                        } else if (obj instanceof JAXBBridgeInfo) {
                            // Service specific exception
                            startDetailElement(writer);     // <soapenv:Detail>
                            RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
                            BridgeContext bridgeContext = rtCtxt.getBridgeContext();
                            JAXBTypeSerializer.getInstance().serialize((JAXBBridgeInfo)obj, bridgeContext, writer);
                            writer.writeEndElement();  // </soapenv:Detail>
                        }
                    }
                }
            }
            writer.writeEndElement();                // </soapenv:Fault>
        }catch (XMLStreamException e) {
            throw new ServerRtException(new LocalizableExceptionAdapter(e));
        }
    }

    private void startDetailElement(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                    SOAP12Constants.QNAME_FAULT_DETAIL.getLocalPart(), SOAP12NamespaceConstants.ENVELOPE);
    }
}