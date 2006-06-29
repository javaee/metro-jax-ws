/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
     * @return the JAXBBidgeInfo for this fault
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
