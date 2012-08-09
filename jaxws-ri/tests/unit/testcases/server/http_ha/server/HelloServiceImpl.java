/*
 * $Id: HelloServiceImpl.java,v 1.1 2010-11-20 01:08:54 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.http_ha.server;

import java.util.*;

import javax.annotation.Resource;

import javax.jws.WebMethod;
import javax.jws.WebService;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.ha.HaInfo;

/**
 * Tests if Packet.HA_INFO is populated or not
 *
 * @author Jitendra Kotamraju
 */
@WebService(name="Hello", serviceName="HelloService", targetNamespace="urn:test")
public class HelloServiceImpl {
    @Resource
    private WebServiceContext wsc;

    public void testHa() {
        MessageContext ctxt = wsc.getMessageContext();
        HaInfo haInfo = (HaInfo)ctxt.get(Packet.HA_INFO);
        if (haInfo == null) {
            throw new WebServiceException("Packet.HA_INFO is not populated.");
        }
        String replica = (String)haInfo.getReplicaInstance();
        if (replica == null || !replica.equals("replica1")) {
            throw new WebServiceException("Expecting getReplicaInstance()=replica1 but got="+replica);
        }
        String key = (String)haInfo.getKey();
        if (key == null || !key.equals("key1")) {
            throw new WebServiceException("Expecting getKey()=key1 but got="+key);
        }
        Boolean failOver = (Boolean)haInfo.isFailOver();
        if (failOver == null || !failOver) {
            throw new WebServiceException("Expecting isFailOver()=true but got="+failOver);
        }
    }
}
