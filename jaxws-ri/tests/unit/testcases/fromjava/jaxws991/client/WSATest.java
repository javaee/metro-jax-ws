/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package fromjava.jaxws991.client;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.message.StringHeader;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple test reproducing bug JAXWS-991 (duplicate WS-A headers when added manually by user)
 * @author Miroslav Kos (miroslav.kos at oracle.com)
 */
public class WSATest extends TestCase {

    Echo port;

    protected void setUp() throws Exception {
        port = new EchoService().getEchoPort();
    }

    public void testEcho() {

        // setting user defined MessageID:
        List<Header> headers = new ArrayList<Header>();
        headers.add(new StringHeader(AddressingVersion.W3C.messageIDTag, "userDefinedMessageID"));
        ((WSBindingProvider) port).setOutboundHeaders(headers);

        port.echoString("String to be echoed");

        // without fix, there is duplicit header wsa:MessageID so the WS call crashes with
        //  SOAPFaultException: A header representing a Message Addressing Property is
        //  not valid and the message cannot be processed ...
    }

}
