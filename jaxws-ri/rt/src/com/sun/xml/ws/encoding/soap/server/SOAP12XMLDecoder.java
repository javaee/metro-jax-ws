/**
 * $Id: SOAP12XMLDecoder.java,v 1.1 2005-07-19 20:41:24 arungupta Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.encoding.soap.server;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.server.*;

public class SOAP12XMLDecoder extends SOAPXMLDecoder {

    private static Set<String> requiredRoles;

    public SOAP12XMLDecoder() {
        requiredRoles = new HashSet<String>();
        requiredRoles.add("http://www.w3.org/2003/05/soap-envelope/role/next");
        requiredRoles.add("http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver");
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#decodeHeader(com.sun.xml.rpc.streaming.XMLReader, com.sun.pept.ept.MessageInfo, com.sun.xml.rpc.soap.internal.InternalMessage)
     */
    @Override
    protected void decodeHeader(XMLStreamReader reader, MessageInfo messageInfo, InternalMessage request) {
        // TODO Auto-generated method stub
        super.decodeHeader(reader, messageInfo, request);
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getBodyTag()
     */
    @Override
    protected QName getBodyTag() {
        return SOAP12Constants.QNAME_SOAP_BODY;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getEnvelopeTag()
     */
    @Override
    protected QName getEnvelopeTag() {
        return SOAP12Constants.QNAME_SOAP_ENVELOPE;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getHeaderTag()
     */
    @Override
    protected QName getHeaderTag() {
        return SOAP12Constants.QNAME_SOAP_HEADER;
    }
    
    @Override
    protected QName getMUAttrQName(){
        return SOAP12Constants.QNAME_MUSTUNDERSTAND;
    }
    
    @Override
    protected QName getRoleAttrQName(){
        return SOAP12Constants.QNAME_ROLE;
    }

    public Set<String> getRequiredRoles() {
        return requiredRoles;
    }
    
}
