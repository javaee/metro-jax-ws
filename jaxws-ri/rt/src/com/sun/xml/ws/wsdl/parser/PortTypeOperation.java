/**
 * $Id: PortTypeOperation.java,v 1.3 2005-09-07 19:40:05 vivekp Exp $
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
    private QName inputMessage;
    private QName outputMessage;
    private QName faultMessage;

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

    public QName getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(QName inputMessage) {
        this.inputMessage = inputMessage;
    }

    public QName getOutputMessage() {
        return outputMessage;
    }

    public void setOutputMessage(QName outputMessage) {
        this.outputMessage = outputMessage;
    }

    public QName getFaultMessage() {
        return faultMessage;
    }

    public void setFaultMessage(QName faultMessage) {
        this.faultMessage = faultMessage;
    }

}
