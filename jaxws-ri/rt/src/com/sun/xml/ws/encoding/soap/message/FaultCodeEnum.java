package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;

import javax.xml.namespace.QName;

/**
 * $Id: FaultCodeEnum.java,v 1.3 2005-09-10 19:47:43 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
public enum FaultCodeEnum {
    VersionMismatch(new QName(SOAP12NamespaceConstants.ENVELOPE, "VersionMismatch", SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE)),
    MustUnderstand(new QName(SOAP12NamespaceConstants.ENVELOPE, "MustUnderstand", SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE)),
    DataEncodingUnknown(new QName(SOAP12NamespaceConstants.ENVELOPE, "DataEncodingUnknown", SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE)),
    Sender(new QName(SOAP12NamespaceConstants.ENVELOPE, "Sender", SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE)),
    Receiver(new QName(SOAP12NamespaceConstants.ENVELOPE, "Receiver", SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE));

    private FaultCodeEnum(QName code){
        this.code = code;
    }

    public QName value(){
        return code;
    }

    public String getLocalPart(){
        return code.getLocalPart();
    }

    public String getNamespaceURI(){
        return code.getNamespaceURI();
    }

    public String getPrefix(){
        return code.getPrefix();
    }

    public static FaultCodeEnum get(QName soapFaultCode){
        if(VersionMismatch.code.equals(soapFaultCode))
            return VersionMismatch;
        else if(MustUnderstand.code.equals(soapFaultCode))
            return MustUnderstand;
        else if(DataEncodingUnknown.code.equals(soapFaultCode))
            return DataEncodingUnknown;
        else if(Sender.code.equals(soapFaultCode))
            return Sender;
        else if(Receiver.code.equals(soapFaultCode))
            return Receiver;
        return null;
    }

    private final QName code;
}
