package com.sun.xml.ws.protocol.soap;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.ExceptionHasMessage;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.fault.SOAPFaultBuilder;

import javax.xml.namespace.QName;

/**
 * This is used to represent SOAP VersionMismatchFault. Use
 * this when the received soap envelope is in a different namespace
 * than what the specified Binding says.
 *
 * @author Jitendra Kotamraju
 */
public class VersionMismatchException extends ExceptionHasMessage {

    private final SOAPVersion soapVersion;

    public VersionMismatchException(SOAPVersion soapVersion, Object... args) {
        super("soap.version.mismatch.err", args);
        this.soapVersion = soapVersion;
    }

    public String getDefaultResourceBundleName() {
        return "com.sun.xml.ws.resources.soap";
    }

    public Message getFaultMessage() {
        QName faultCode = (soapVersion == SOAPVersion.SOAP_11)
            ? SOAPConstants.FAULT_CODE_VERSION_MISMATCH
            : SOAP12Constants.FAULT_CODE_VERSION_MISMATCH;
        return SOAPFaultBuilder.createSOAPFaultMessage(
                soapVersion, getLocalizedMessage(), faultCode);
    }

}
