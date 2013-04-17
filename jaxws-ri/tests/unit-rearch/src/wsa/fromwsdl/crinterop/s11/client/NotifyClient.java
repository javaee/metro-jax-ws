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

package wsa.fromwsdl.crinterop.s11.client;

import junit.framework.TestCase;
import static testutil.WsaUtils.S11_NS;
import static testutil.WsaUtils.invokeOneWay;
import static wsa.fromwsdl.crinterop.s11.client.BindingProviderUtil.createDispatchWithWSDLWithoutAddressing;
import static wsa.fromwsdl.crinterop.s11.client.BindingProviderUtil.createStub;
import static wsa.fromwsdl.crinterop.s11.client.BindingProviderUtil.getAddress;
import static wsa.fromwsdl.crinterop.s11.common.TestConstants.MESSAGES;
import static wsa.fromwsdl.crinterop.s11.common.TestConstants.NOTIFY_ACTION;

/**
 * @author Arun Gupta
 */
public class NotifyClient extends TestCase {
    public NotifyClient(String name) {
        super(name);
    }

    /**
     * SOAP 1.1 one-way message.
     */
    public void test1100() throws Exception {
        createStub().notify("test1100");
    }

    /**
     * SOAP 1.1 one-way defaulted with a MessageID value.
     */
    public void test1101() throws Exception {
        createStub().notify("test1101");
    }

    /**
     * SOAP 1.1 one-way message with a ReplyTo address of none.
     */
    public void test1102() throws Exception {
        invokeOneWay(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getNoneReplyToMessage(),
                     S11_NS,
                     getAddress(),
                     NOTIFY_ACTION,
                     "test1102");
    }

    /**
     * SOAP 1.1 one-way message with a FaultTo address of none.
     */
    public void test1103() throws Exception {
        invokeOneWay(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getNoneFaultToMessage(),
                     S11_NS,
                     getAddress(),
                     NOTIFY_ACTION,
                     "test1103");
    }

    /**
     * SOAP 1.1 one-way message with a ReplyTo and FaultTo address of none.
     */
    public void test1104() throws Exception {
        invokeOneWay(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getNoneReplyToFaultToMessage(),
                     S11_NS,
                     getAddress(),
                     NOTIFY_ACTION,
                     "test1104");
    }

    /**
     * SOAP 1.1 one-way message with ReplyTo containing a Reference Parameter.
     */
    public void test1106() throws Exception {
        invokeOneWay(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getReplyToRefpsNotifyMessage(),
                     S11_NS,
                     getAddress(),
                     NOTIFY_ACTION,
                     "test1106");
    }

    /**
     * SOAP 1.1 one-way message with ReplyTo containing WSDL Metadata.
     */
    public void test1107() throws Exception {
        invokeOneWay(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getReplyToMetadataMessage(),
                     S11_NS,
                     getAddress(),
                     NOTIFY_ACTION,
                     "test1107");
    }

    /**
     * SOAP 1.1 one-way message with ReplyTo containing EPR extensions.
     */
    public void test1108() throws Exception {
        invokeOneWay(createDispatchWithWSDLWithoutAddressing(),
                     MESSAGES.getReplyToExtensionsMessage(),
                     S11_NS,
                     getAddress(),
                     NOTIFY_ACTION,
                     "test1108");
    }
}
