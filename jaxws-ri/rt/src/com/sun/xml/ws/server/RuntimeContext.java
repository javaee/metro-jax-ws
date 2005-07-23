/**
 * $Id: RuntimeContext.java,v 1.6 2005-07-23 04:10:11 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.model.RuntimeModel;




/**
 * $author: WS Development Team
 */
public class RuntimeContext {

    public RuntimeContext(RuntimeModel model) {
        this.model = model;
    }
    
    /**
     * @return Returns the model.
     */
    public RuntimeModel getModel() {
        return model;
    }
    
    /**
     * @return Returns info about endpoint
     */
    public RuntimeEndpointInfo getRuntimeEndpointInfo() {
        return endpointInfo;
    }
    
    /**
     * sets info about endpoint
     */
    public void setRuntimeEndpointInfo(RuntimeEndpointInfo endpointInfo) {
        this.endpointInfo = endpointInfo;
    }
    
    /**
     * @param name
     * @param mi
     * @return the <code>Method</code> associated with the operation named name
     */
    public Method getDispatchMethod(QName name, MessageInfo mi) {
        return getDispatchMethod(name);
    }
    
    /**
     * @param name
     * @return the <code>Method</code> associated with the operation named name
     */
    public Method getDispatchMethod(QName name){
        return model.getDispatchMethod(name);
    }
        
    /**
     * @param qname
     * @param mi
     */
    public void setMethodAndMEP(QName qname, MessageInfo mi) {
        if (model != null) {
            mi.setMethod(model.getDispatchMethod(qname));
        }
    }
    
    /**
     * @param name
     * @return the decoder Info associated with operation named name
     */
    public Object getDecoderInfo(QName name) {
        return model.getDecoderInfo(name);
    }
    
    public BridgeContext getBridgeContext() {
        return (model != null)?model.getBridgeContext():null;
    }
    
    private RuntimeModel model;   
    private RuntimeEndpointInfo endpointInfo;  
}
