/*
 * $Id: SOAPFaultInfo.java,v 1.1 2005-05-23 22:30:17 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.message;

import javax.xml.namespace.QName;

/**
 * @author JAX-RPC Development Team
 */
public class SOAPFaultInfo {

    // called SOAPFaultInfo to avoid clashes with the SOAPFault in JAXM
    public SOAPFaultInfo(QName code, String string, String actor) {
        this(code, string, actor, null);
    }

    public SOAPFaultInfo(
        QName code,
        String string,
        String actor,
        Object detail) {
        this.code = code;
        this.string = string;
        this.actor = actor;
        this.detail = detail;
    }

    public QName getCode() {
        return code;
    }

    public void setCode(QName code) {
        this.code = code;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    private QName code;
    private String string;
    private String actor;
    private Object detail;
}
