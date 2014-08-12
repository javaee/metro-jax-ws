/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.model.wsdl.WSDLDirectProperties;
import junit.framework.TestCase;

/**
 * Simple test to reproduce PIT failure. The problem is that Distributed
 */
public class MessageContextImplTest extends TestCase {

    private static final String DIRECT_CHILD = "com.sun.xml.ws.client.ContentNegotiation";
    private static final String SATELLITES_CHILD = "javax.xml.ws.wsdl.service";

    public void test() {

        try {
            Packet packet = new Packet();
            packet.addSatellite(new WSDLDirectProperties(null, null));
            MessageContextImpl ctx = new MessageContextImpl(packet);
            System.out.println(ctx.keySet());
            System.out.println(ctx.entrySet());

            checkProperty(ctx, DIRECT_CHILD);

            // this property wasn't found before fix
            checkProperty(ctx, SATELLITES_CHILD);

            // check all ...
            checkAll(ctx);

        } catch(Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }

    }

    private void checkAll(MessageContextImpl ctx) {
        for (String key : ctx.keySet()) {
            checkProperty(ctx, key);
        }
    }

    private void checkProperty(MessageContextImpl ctx, String key) {
        // key known by the MessageContextImpl object
        System.out.println("\n=== property =====================");
        System.out.println("key = " + key);
        System.out.println("value = " + ctx.get(key));

        // keySet knows the key, although it is reported as not in the context?! It seems weird to me ...
        System.out.println("ctx.containsKey(key) = " + ctx.containsKey(key));

        // this regularly fails ...
        System.out.println("ctx.getScope(key) = " + ctx.getScope(key));
    }

}
