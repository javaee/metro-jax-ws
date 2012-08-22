/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
