/**
 * $Id: PortTypeOperation.java,v 1.1 2005-08-13 19:30:36 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;

public class PortTypeOperation{
    private QName name;
    private String parameterOrder;
    private Message inputMessage;
    private Message outputMessage;
    private Message faultMessage;

    public PortTypeOperation(QName name) {
        this.name = name;
    }

    public QName getName() {
        return name;
    }

    public String getParameterOrder() {
        return parameterOrder;
    }

    public void setParameterOrder(String parameterOrder) {
        this.parameterOrder = parameterOrder;
    }

    public Message getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(Message inputMessage) {
        this.inputMessage = inputMessage;
    }

    public Message getOutputMessage() {
        return outputMessage;
    }

    public void setOutputMessage(Message outputMessage) {
        this.outputMessage = outputMessage;
    }

    public Message getFaultMessage() {
        return faultMessage;
    }

    public void setFaultMessage(Message faultMessage) {
        this.faultMessage = faultMessage;
    }

}
