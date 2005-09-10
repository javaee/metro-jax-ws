/**
 * $Id: PortTypeOperation.java,v 1.4 2005-09-10 19:48:22 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
