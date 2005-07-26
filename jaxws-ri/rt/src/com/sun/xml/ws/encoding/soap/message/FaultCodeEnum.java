package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;

import javax.xml.namespace.QName;

/**
 * $Id: FaultCodeEnum.java,v 1.2 2005-07-26 23:43:44 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
