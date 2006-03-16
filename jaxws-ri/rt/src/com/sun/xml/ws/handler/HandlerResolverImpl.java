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

import com.sun.xml.ws.client.*;
import com.sun.xml.ws.client.ServiceContext;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
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
 * <p>Implementation class of HandlerResolver. This class is a simple
 * map of PortInfo objects to handler chains. It is used by a
 * {@link com.sun.xml.ws.client.ServiceContext} object, and can
 * be replaced by user code with a different class implementing
 * HandlerResolver. This class is only used on the client side, and
 * it includes a lot of logging to help when there are issues since
 * it deals with port names, service names, and bindings. All three
 * must match when getting a handler chain from the map.
 *
 * <p>It is created by the {@link com.sun.xml.ws.client.ServiceContextBuilder}
 * class and set on the ServiceContext. The ServiceContextBuilder uses
 * the {@link com.sun.xml.ws.util.HandlerAnnotationProcessor} to create
 * a handler chain and then it sets the chains on this class and they
 * are put into the map. The ServiceContext uses the map to set handler
 * chains on bindings when they are created.
 * 
 * @see com.sun.xml.ws.client.ServiceContext
 * @see com.sun.xml.ws.handler.PortInfoImpl
 *
 * @author WS Development Team
 */
public class HandlerResolverImpl implements HandlerResolver {
    
    private Map<PortInfo, List<Handler>> chainMap;
    private ServiceContext serviceContext;
    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".handler");
    
    public HandlerResolverImpl(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        chainMap = new HashMap<PortInfo, List<Handler>>();
    }

    /**
     * API method to return the correct handler chain for a given
     * PortInfo class.
     *
     * @param info A PortInfo object.
     * @return A list of handler objects. If there is no handler chain
     * found, it will return an empty list rather than null.
     */
    public List<Handler> getHandlerChain(PortInfo info) {
        List<Handler> chain = chainMap.get(info);
        //For now parse Service class always
        HandlerAnnotationInfo chainInfo =
                HandlerAnnotationProcessor.buildHandlerInfo(serviceContext.getServiceClass(),
                    info.getServiceName(), info.getPortName(), info.getBindingID());
        if(chainInfo != null) {
            chain = chainInfo.getHandlers();
            chainMap.put(info,chain);
            serviceContext.setRoles(info.getPortName(),chainInfo.getRoles());
        }
        if (chain == null) {
            if (logger.isLoggable(Level.FINE)) {
                logGetChain(info);
            }
            chain = new ArrayList<Handler>();
        }
        return chain;
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
