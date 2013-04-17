/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2013 Oracle and/or its affiliates. All rights reserved.
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

package wsa.fromjava.epr.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.transform.stream.StreamResult;
import javax.xml.namespace.QName;

import testutil.EprUtil;
import testutil.ClientServerTestUtil;
import junit.framework.TestCase;

public class AddNumbersClient extends TestCase {
    private static final String endpointAddress = "http://localhost:/jaxrpc-wsa_fromjava_epr/hello";
    private static final QName serviceName = new QName("http://foobar.org/", "AddNumbersService");
    private static final QName portName = new QName("http://foobar.org/", "AddNumbersPort");
    private static final QName portTypeName = new QName("http://foobar.org/", "AddNumbers");

    public AddNumbersClient(String name) {
        super(name);
    }

    private AddNumbers createProxy() throws Exception {
        return new AddNumbersService().getAddNumbersPort();
    }

    public void testDefaultOutputAction() throws Exception {
        if (ClientServerTestUtil.useLocal()){
            return;
        }
        AddNumbers proxy = createProxy();
        W3CEndpointReference epr = proxy.getW3CEPR();
        System.out.println("---------------------------------------");
        epr.writeTo(new StreamResult(System.out));
        System.out.println("---------------------------------------");
        //EprUtil.validateEPR(epr, endpointAddress, serviceName, portName, portTypeName, Boolean.TRUE);
        EprUtil.validateEPR(epr, endpointAddress, null, null, null, false);

//        AddNumbers newProxy = epr.getPort(AddNumbers.class);
//        int result = newProxy.addNumbersNoAction(10, 10);
//        assertEquals(20, result);
    }


}
