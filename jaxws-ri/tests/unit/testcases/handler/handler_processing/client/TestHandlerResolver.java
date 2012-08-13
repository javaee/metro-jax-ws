/*
 * $Id: TestHandlerResolver.java,v 1.1 2007-09-22 00:39:24 ramapulavarthi Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package handler.handler_processing.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

/**
 * Simple Handlerresolver
 */
public class TestHandlerResolver implements HandlerResolver {

    private List<PortInfo> portInfos;
    
    public TestHandlerResolver() {
        portInfos = new ArrayList<PortInfo>();
    }
    
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        portInfos.add(portInfo);
        return new ArrayList<Handler>();
    }
    
    // returns a copy so it won't get clobbered
    List<PortInfo> getPortInfos() {
        return new ArrayList(portInfos);
    }
    
    void clearPortInfos() {
        portInfos.clear();
    }
}
