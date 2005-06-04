package com.sun.xml.ws.encoding.soap.message;

import javax.xml.ws.ProtocolException;
import java.util.List;

/**
 * $Id: SOAP12FaultException.java,v 1.1 2005-06-04 01:48:14 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
public class SOAP12FaultException extends ProtocolException{
    private FaultCode code;
    private FaultReason reason;
    private String node;
    private String role;
    private List detail;

    public SOAP12FaultException(FaultCode code, FaultReason reason, String node, String role, List detail) {
        this.code = code;
        this.reason = reason;
        this.node = node;
        this.role = role;
        this.detail = detail;
    }

    public FaultCode getCode() {
        return code;
    }

    public FaultReason getReason() {
        return reason;
    }

    public String getNode() {
        return node;
    }

    public String getRole() {
        return role;
    }

    public List getDetail() {
        return detail;
    }
}
