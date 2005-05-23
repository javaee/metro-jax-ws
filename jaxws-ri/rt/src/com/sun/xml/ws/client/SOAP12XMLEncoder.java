/**
 * $Id: SOAP12XMLEncoder.java,v 1.1 2005-05-23 22:26:37 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.streaming.XMLWriter;

public class SOAP12XMLEncoder extends SOAPXMLEncoder {

    /*
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPEncoder#startEnvelope(com.sun.xml.rpc.streaming.XMLWriter)
     */
    @Override
        protected void startEnvelope(XMLWriter writer) {
        writer.startElement(SOAPNamespaceConstants.TAG_ENVELOPE, SOAP12NamespaceConstants.ENVELOPE,
            SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE);
    }

    /*
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPEncoder#startBody(com.sun.xml.rpc.streaming.XMLWriter)
     */
    @Override
        protected void startBody(XMLWriter writer) {
        writer.startElement(SOAPNamespaceConstants.TAG_BODY, SOAP12NamespaceConstants.ENVELOPE,
            SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE);
    }

    /*
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPEncoder#startHeader(com.sun.xml.rpc.streaming.XMLWriter)
     */
    @Override
        protected void startHeader(XMLWriter writer) {
        writer.startElement(SOAPNamespaceConstants.TAG_HEADER,
            SOAP12NamespaceConstants.ENVELOPE,
            SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE); // <env:Header>
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.client.SOAPXMLEncoder#getContentType()
     */
    @Override
        protected String getContentType() {
        return "application/soap+xml";
    }
}
