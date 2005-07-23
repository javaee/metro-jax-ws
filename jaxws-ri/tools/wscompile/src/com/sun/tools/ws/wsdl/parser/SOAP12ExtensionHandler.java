/**
 * $Id: SOAP12ExtensionHandler.java,v 1.2 2005-07-23 04:11:06 kohlert Exp $
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
     * @see SOAPExtensionHandler#getNamespaceURI()
     */
    @Override
    public String getNamespaceURI() {
        return Constants.NS_WSDL_SOAP12;
    }

    /* 
     * @see SOAPExtensionHandler#getAddressQName()
     */
    @Override
    protected QName getAddressQName() {
        return SOAP12Constants.QNAME_ADDRESS;
    }

    /* 
     * @see SOAPExtensionHandler#getBindingQName()
     */
    @Override
    protected QName getBindingQName() {
        return SOAP12Constants.QNAME_BINDING;
    }

    /* 
     * @see SOAPExtensionHandler#getBodyQName()
     */
    @Override
    protected QName getBodyQName() {
        return SOAP12Constants.QNAME_BODY;
    }

    /* 
     * @see SOAPExtensionHandler#getFaultQName()
     */
    @Override
    protected QName getFaultQName() {
        return SOAP12Constants.QNAME_FAULT;
    }

    /* 
     * @see SOAPExtensionHandler#getHeaderfaultQName()
     */
    @Override
    protected QName getHeaderfaultQName() {
        return SOAP12Constants.QNAME_HEADERFAULT;
    }

    /* 
     * @see SOAPExtensionHandler#getHeaderQName()
     */
    @Override
    protected QName getHeaderQName() {
        return SOAP12Constants.QNAME_HEADER;
    }

    /* 
     * @see SOAPExtensionHandler#getOperationQName()
     */
    @Override
    protected QName getOperationQName() {
        return SOAP12Constants.QNAME_OPERATION;
    }
}
