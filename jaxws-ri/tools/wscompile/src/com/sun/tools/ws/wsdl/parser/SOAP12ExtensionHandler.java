/**
 * $Id: SOAP12ExtensionHandler.java,v 1.1 2005-05-24 14:07:29 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.parser;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.document.soap.SOAP12Constants;


public class SOAP12ExtensionHandler extends SOAPExtensionHandler {

    /* 
     * @see com.sun.xml.rpc.wsdl.parser.SOAPExtensionHandler#getNamespaceURI()
     */
    @Override
    public String getNamespaceURI() {
        return Constants.NS_WSDL_SOAP12;
    }

    /* 
     * @see com.sun.xml.rpc.wsdl.parser.SOAPExtensionHandler#getAddressQName()
     */
    @Override
    protected QName getAddressQName() {
        return SOAP12Constants.QNAME_ADDRESS;
    }

    /* 
     * @see com.sun.xml.rpc.wsdl.parser.SOAPExtensionHandler#getBindingQName()
     */
    @Override
    protected QName getBindingQName() {
        return SOAP12Constants.QNAME_BINDING;
    }

    /* 
     * @see com.sun.xml.rpc.wsdl.parser.SOAPExtensionHandler#getBodyQName()
     */
    @Override
    protected QName getBodyQName() {
        return SOAP12Constants.QNAME_BODY;
    }

    /* 
     * @see com.sun.xml.rpc.wsdl.parser.SOAPExtensionHandler#getFaultQName()
     */
    @Override
    protected QName getFaultQName() {
        return SOAP12Constants.QNAME_FAULT;
    }

    /* 
     * @see com.sun.xml.rpc.wsdl.parser.SOAPExtensionHandler#getHeaderfaultQName()
     */
    @Override
    protected QName getHeaderfaultQName() {
        return SOAP12Constants.QNAME_HEADERFAULT;
    }

    /* 
     * @see com.sun.xml.rpc.wsdl.parser.SOAPExtensionHandler#getHeaderQName()
     */
    @Override
    protected QName getHeaderQName() {
        return SOAP12Constants.QNAME_HEADER;
    }

    /* 
     * @see com.sun.xml.rpc.wsdl.parser.SOAPExtensionHandler#getOperationQName()
     */
    @Override
    protected QName getOperationQName() {
        return SOAP12Constants.QNAME_OPERATION;
    }
}
