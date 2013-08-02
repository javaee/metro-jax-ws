/*
 * $Id: MUHelperHandler.java,v 1.1 2007-08-11 03:10:39 vivekp Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package fromwsdl.wsdl_hello_lit.common;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Simple handler to add headers to an outgoing message.
 */
public class MUHelperHandler implements SOAPHandler<SOAPMessageContext> {

    private QName headerToAdd;
    private String roleToTarget;

    /*
     * This qname will be set on the outgoing message with
     * MU set to true, targeted at the role.
     */
    public void setMUHeader(QName qname, String role) {
        headerToAdd = qname;
        roleToTarget = role;
    }

    /*
     * The real work happens here.
     */
    public boolean handleMessage(SOAPMessageContext smc) {
        if (smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY) == Boolean.FALSE ||
            headerToAdd == null) {
            return true;
        }

        try {
            SOAPMessage message = smc.getMessage();
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            SOAPHeader header = envelope.getHeader();
            if (header == null) { // should be null originally
                header = envelope.addHeader();
            }
            SOAPHeaderElement element = header.addHeaderElement(headerToAdd);
            element.setActor(roleToTarget);
            element.setMustUnderstand(true);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /***** other handler methods stubbed out *****/
    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext messageContext) {
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

}
