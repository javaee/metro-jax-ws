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
package com.sun.xml.ws.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

/**
 *
 * @author WS Development Team
 */
public class HandlerResolverImpl implements HandlerResolver {
    
    private Map<PortInfo, List<Handler>> chainMap;
    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".handler");
    
    public HandlerResolverImpl() {
        chainMap = new HashMap<PortInfo, List<Handler>>();
    }

    public List<Handler> getHandlerChain(PortInfo info) {
        List<Handler> chain = chainMap.get(info);
        if (chain == null) {
            if (logger.isLoggable(Level.FINE)) {
                logGetChain(info);
            }
            chain = new ArrayList<Handler>();
        }
        return chain;
    }
    
    public void setHandlerChain(PortInfo info, List<Handler> chain) {
        if (logger.isLoggable(Level.FINER)) {
            logSetChain(info, chain);
        }
        chainMap.put(info, chain);
    }
    
    // logged at finer level
    private void logSetChain(PortInfo info, List<Handler> chain) {
        logger.finer("Setting chain of length " + chain.size() +
            " for port info");
        logPortInfo(info, Level.FINER);
    }
    
    // logged at fine level
    private void logGetChain(PortInfo info) {
        logger.fine("No handler chain found for port info:");
        logPortInfo(info, Level.FINE);
        logger.fine("Existing handler chains:");
        if (chainMap.isEmpty()) {
            logger.fine("none");
        } else {
            for (PortInfo key : chainMap.keySet()) {
                logger.fine(chainMap.get(key).size() +
                    " handlers for port info ");
                logPortInfo(key, Level.FINE);
            }
        }
    }
    
    private void logPortInfo(PortInfo info, Level level) {
        logger.log(level, "binding: " + info.getBindingID() +
            "\nservice: " + info.getServiceName() +
            "\nport: " + info.getPortName());
    }
}
