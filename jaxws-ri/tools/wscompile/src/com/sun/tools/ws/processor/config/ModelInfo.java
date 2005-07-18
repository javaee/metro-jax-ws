/*
 * $Id: ModelInfo.java,v 1.2 2005-07-18 18:13:56 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config;

import java.util.Properties;

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.modeler.Modeler;

/**
 *
 * @author WS Development Team
 */
public abstract class ModelInfo {

    protected ModelInfo() {}

    public Configuration getParent() {
        return _parent;
    }

    public void setParent(Configuration c) {
        _parent = c;
    }

    public String getName() {
        return _name;
    }

    public void setName(String s) {
        _name = s;
    }

    public Configuration getConfiguration() {
        return _parent;
    }

//    public TypeMappingRegistryInfo getTypeMappingRegistry() {
//        return _typeMappingRegistryInfo;
//    }
//
//    public void setTypeMappingRegistry(TypeMappingRegistryInfo i) {
//        _typeMappingRegistryInfo = i;
//    }

    public HandlerChainInfo getClientHandlerChainInfo() {
        return _clientHandlerChainInfo;
    }

    public void setClientHandlerChainInfo(HandlerChainInfo i) {
        _clientHandlerChainInfo = i;
    }

    public HandlerChainInfo getServerHandlerChainInfo() {
        return _serverHandlerChainInfo;
    }

    public void setServerHandlerChainInfo(HandlerChainInfo i) {
        _serverHandlerChainInfo = i;
    }

/*    public NamespaceMappingRegistryInfo getNamespaceMappingRegistry() {
        return _namespaceMappingRegistryInfo;
    }

    public void setNamespaceMappingRegistry(
        com.sun.xml.rpc.spi.tools.NamespaceMappingRegistryInfo i) {

        _namespaceMappingRegistryInfo = (NamespaceMappingRegistryInfo) i;
    }*/

    public String getJavaPackageName() {
        return _javaPackageName;
    }

    public void setJavaPackageName(String s) {
        _javaPackageName = s;
    }

    public Model buildModel(Properties options){
        return getModeler(options).buildModel();
    }

    protected abstract Modeler getModeler(Properties options);

    private Configuration _parent;
    private String _name;
    private String _javaPackageName;
//    private TypeMappingRegistryInfo _typeMappingRegistryInfo;
    private HandlerChainInfo _clientHandlerChainInfo;
    private HandlerChainInfo _serverHandlerChainInfo;
//    private NamespaceMappingRegistryInfo _namespaceMappingRegistryInfo;
}
