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
package com.sun.xml.ws.client;

import com.sun.xml.ws.server.RuntimeContext;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;

import java.util.ArrayList;
import java.util.List;


/**
 * $author: WS Development Team
 */
public class EndpointIFContext {

    private RuntimeContext runtimeContext; //from annotationPro ess
    private Class serviceInterface;    //prop can take out
    private QName serviceName;
    private Class sei;
    private QName portName;
    private ArrayList<Handler> handlers;
    private String endpointAddress;
    private String bindingId;


    public EndpointIFContext(Class sei) {
        this.sei = sei;
        handlers = new ArrayList();
    }

    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    public void setRuntimeContext(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    public Class getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Class getSei() {
        return sei;
    }

    public void setSei(Class sei) {
        this.sei = sei;
    }

    public QName getPortName() {
        if (portName == null){
        if ((runtimeContext != null) && (runtimeContext.getModel() != null))
            portName = runtimeContext.getModel().getPortName();
        } 
        return portName;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public void setPortInfo(QName portQName, String endpoint, String bindingID) {
        portName = portQName;
        endpointAddress = endpoint;
        this.bindingId = bindingID;
    }

    public String getBindingID() {
        return bindingId;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public boolean contains(QName serviceName) {
        if (serviceName.equals(this.serviceName))
            return true;
        return false;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public void setBindingID(String bindingId) {
        this.bindingId = bindingId;
    }
}