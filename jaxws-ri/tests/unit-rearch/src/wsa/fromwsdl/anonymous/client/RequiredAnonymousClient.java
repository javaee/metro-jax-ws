/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package wsa.fromwsdl.anonymous.client;

import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.TestCase;
import testutil.WsaUtils;
import com.sun.xml.ws.addressing.W3CAddressingConstants;

/**
 * @author Arun Gupta
 */
public class RequiredAnonymousClient extends TestCase {
    public RequiredAnonymousClient(String name) {
        super(name);
    }

    public void testAnonymousReplyTo() throws Exception {
        BindingProviderUtil.createRequiredAnonymousStub().addNumbers(30, 30, "RequiredAnonymous.testAnonymousReplyTo");
    }

    public void testAnonymousFaultTo() throws Exception {
        WsaUtils.invoke(BindingProviderUtil.createRequiredAnonymousDispatch(),
                        BindingProviderUtil.ANONYMOUS_FAULT_TO_MESSAGE,
                        WsaUtils.S11_NS,
                        "RequiredAnonymous.testAnonymousFaultTo");
    }

    public void testNonAnonymousFaultTo() throws Exception {
        try {
            WsaUtils.invoke(BindingProviderUtil.createRequiredAnonymousDispatch(),
                            BindingProviderUtil.NON_ANONYMOUS_FAULT_TO_MESSAGE,
                            WsaUtils.S11_NS,
                            BindingProviderUtil.getRequiredAnonymousAddress(),
                            "RequiredAnonymous.testNonAnonymousFaultTo");
            fail("MUST throw SOAPFaultException with ONLY_ANONYMOUS_ADDRESS_SUPPORTED fault code");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertEquals(W3CAddressingConstants.ONLY_ANONYMOUS_ADDRESS_SUPPORTED, e.getFault().getFaultCodeAsQName());
        }
    }

    public void testNonAnonymousReplyTo() throws Exception {
        try {
            WsaUtils.invoke(BindingProviderUtil.createRequiredAnonymousDispatchWithoutAddressing(),
                            BindingProviderUtil.NON_ANONYMOUS_REPLY_TO_ANONYMOUS_FAULT_TO_COMPLETE_MESSAGE,
                            WsaUtils.S11_NS,
                            BindingProviderUtil.getRequiredAnonymousAddress(),
                            BindingProviderUtil.REQUIRED_IN_ACTION,
                            BindingProviderUtil.getRequiredAnonymousAddress(),
                            "RequiredAnonymous.testNonAnonymousReplyTo");
            fail("MUST throw SOAPFaultException due to non-anon ReplyTo with required wsaw:Anonymous");
        } catch (SOAPFaultException e) {
            assertNotNull(e.getFault());
            assertEquals(W3CAddressingConstants.ONLY_ANONYMOUS_ADDRESS_SUPPORTED, e.getFault().getFaultCodeAsQName());
        }
    }
}
