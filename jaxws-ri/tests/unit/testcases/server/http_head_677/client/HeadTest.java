/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: HeadTest.java,v 1.2 2009-09-29 21:58:00 jitu Exp $
 */

/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package server.http_head_677.client;

import java.lang.reflect.Proxy;
import java.io.*;
import junit.framework.*;
import testutil.HTTPResponseInfo;
import javax.xml.soap.*;
import javax.xml.namespace.QName;
import java.net.*;
import javax.xml.ws.*;


/**
 * Tests HTTP HEAD requests
 *
 * @author Jitendra Kotamraju
 */
public class HeadTest extends TestCase {

    public HeadTest(String name) throws Exception {
        super(name);
    }

    Hello getStub() throws Exception {
        return new Hello_Service().getHelloPort();
    }

    /*
     * Tests HTTP HEAD requests
     */
    public void testHead() throws Exception {
        BindingProvider bp = (BindingProvider) getStub();
        String address =
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        assertEquals(200, head(address+"?wsdl"));
        assertEquals(200, head(address+"?xsd=1"));
        assertEquals(404, head(address+"?wsdl=1"));
        assertEquals(404, head(address+"?xsd=2"));
    }

    private int head(String address) throws Exception {
        // create connection
        HttpURLConnection conn =
            (HttpURLConnection) new URL(address).openConnection();
        conn.setRequestMethod("HEAD");
        // lwhs is not working with keep-alive. Issue: 6886723
        conn.setRequestProperty("Connection", "close");
        return conn.getResponseCode();
    }

}
