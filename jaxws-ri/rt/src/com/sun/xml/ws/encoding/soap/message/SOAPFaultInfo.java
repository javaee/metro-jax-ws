/*
 * $Id: SOAPFaultInfo.java,v 1.3 2005-07-26 23:43:44 vivekp Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.message;

import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.util.SOAPUtil;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.Detail;
import javax.xml.ws.soap.SOAPBinding;

/**
 * @author WS Development Team
 */
public class SOAPFaultInfo {

    public SOAPFaultInfo(){}
    /**
     * create SOAPFaultInfo with SOAPFault
     *
     * @param fault
     */
    public SOAPFaultInfo(SOAPFault fault) {
        this.soapFault = fault;
    }

    /**
     * Accessor method to get the fault bean
     *
     * @return
     */
    public JAXBBridgeInfo getFaultBean() {
        return faultBean;
    }

    /**
     * creates SOAPFaultInfo, could be SOAP 1.1 or SOAP 1.2 fault.
     *
     * @param string
     * @param code
     * @param actor
     * @param detail
     * @param bindingId
     */
    public SOAPFaultInfo(String string, QName code, String actor, Object detail, String bindingId) {
        if (detail == null || detail instanceof Detail) {
            Detail det = (detail != null) ? (Detail) detail : null;
            soapFault = SOAPUtil.createSOAPFault(string, code, actor, det, bindingId);
        } else {
            soapFault = SOAPUtil.createSOAPFault(string, code, actor, null, bindingId);
            faultBean = (JAXBBridgeInfo) detail;
        }
    }

    public QName getCode() {
        return soapFault.getFaultCodeAsQName();
    }

    public String getString() {
        return soapFault.getFaultString();
    }

    public String getActor() {
        return soapFault.getFaultActor();
    }

    public Object getDetail() {
        if (faultBean != null)
            return faultBean;

        return soapFault.getDetail();
    }

    public SOAPFault getSOAPFault() {
        return soapFault;
    }

    protected SOAPFault soapFault;
    protected JAXBBridgeInfo faultBean;
}
