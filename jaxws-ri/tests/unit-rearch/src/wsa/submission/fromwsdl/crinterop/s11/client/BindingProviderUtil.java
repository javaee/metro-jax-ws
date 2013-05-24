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

package wsa.submission.fromwsdl.crinterop.s11.client;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.namespace.QName;

import testutil.ClientServerTestUtil;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;

/**
 * @author Arun Gupta
 */
public class BindingProviderUtil {
    public static String getAddress() {
        if(ClientServerTestUtil.useLocal())
            return ClientServerTestUtil.getLocalAddress(BindingProviderUtil.PORT_QNAME);
        else
            return BindingProviderUtil.ENDPOINT_ADDRESS;
    }

    public static String getNonAnonymousAddress() {
        if(ClientServerTestUtil.useLocal())
            return ClientServerTestUtil.getLocalAddress(BindingProviderUtil.PORT_QNAME);
        else
            return BindingProviderUtil.NON_ANONYMOUS_ENDPOINT_ADDRESS;
    }

    public static WsaTestPortType createStub() {
        return new WsaTestService().getPort(WsaTestPortType.class, ENABLED_ADDRESSING_FEATURE);
    }

    public static  Dispatch<SOAPMessage> createDispatchWithWSDLWithAddressing() {
        return new WsaTestService().createDispatch(BindingProviderUtil.PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, ENABLED_ADDRESSING_FEATURE);
    }

    public static  Dispatch<SOAPMessage> createDispatchWithWSDLWithoutAddressing() {
        return new WsaTestService().createDispatch(BindingProviderUtil.PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, DISABLED_ADDRESSING_FEATURE);
    }

    private static final String ENDPOINT_ADDRESS = "http://localhost:8080/jaxrpc-wsa_submission_fromwsdl_crinterop_s11/member";
    private static final String NON_ANONYMOUS_ENDPOINT_ADDRESS = "http://localhost:8080/jaxrpc-wsa_submission_fromwsdl_crinterop_s12/nonanonymous";
    private static final String NAMESPACE_URI = "http://example.org/wsaTestService";
    private static final QName PORT_QNAME = new QName(BindingProviderUtil.NAMESPACE_URI, "wsaTestPort");
    private static final MemberSubmissionAddressingFeature ENABLED_ADDRESSING_FEATURE = new MemberSubmissionAddressingFeature(true);
    private static final MemberSubmissionAddressingFeature DISABLED_ADDRESSING_FEATURE = new MemberSubmissionAddressingFeature(false);
}
