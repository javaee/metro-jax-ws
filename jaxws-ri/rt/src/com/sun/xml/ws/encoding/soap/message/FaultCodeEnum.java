package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;

import javax.xml.namespace.QName;

/**
 * $Id: FaultCodeEnum.java,v 1.1 2005-06-04 01:48:13 vivekp Exp $
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

    public static FaultCodeEnum get(QName faultCode){
        if(VersionMismatch.code.equals(faultCode))
            return VersionMismatch;
        else if(MustUnderstand.code.equals(faultCode))
            return MustUnderstand;
        else if(DataEncodingUnknown.code.equals(faultCode))
            return DataEncodingUnknown;
        else if(Sender.code.equals(faultCode))
            return Sender;
        else if(Receiver.code.equals(faultCode))
            return Receiver;
        return null;
    }

    private final QName code;
}
