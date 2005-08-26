/**
 * $Id: HandlerResolverImpl.java,v 1.1 2005-08-26 21:42:19 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

/**
 *
 * @author WS Development Team
 */
public class HandlerResolverImpl implements HandlerResolver {
    
    private Map<PortInfo, List<Handler>> chainMap;
    
    public HandlerResolverImpl() {
        chainMap = new HashMap<PortInfo, List<Handler>>();
    }

    public List<Handler> getHandlerChain(PortInfo info) {
        List<Handler> chain = chainMap.get(info);
        if (chain == null) {
            chain = new ArrayList<Handler>();
        }
        return chain;
    }
    
    public void setHandlerChain(PortInfo info, List<Handler> chain) {
        chainMap.put(info, chain);
    }
}
