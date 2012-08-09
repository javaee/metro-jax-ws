/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package wsa.w3c.fromwsdl.crinterop_s11.client;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.soap.AddressingFeature;
import testutil.PortAllocator;

/**
 * @author Rama Pulavarthi
 */
public class BindingProviderUtil {
    public static String getAddress() {
        WsaTestPortType stub = new WsaTestService().getWsaTestPort();
        return (String) ((BindingProvider)stub).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

    public static String getNonAnonymousEndpointAddress() {
        return getAddress();
    }

    public static String getNonAnonymousClientAddress() {
	return "http://localhost:"+PortAllocator.getFreePort()+"/jaxws-crinterop-s11/nonanonymous";
    }

    public static WsaTestPortType createStub() {
        WsaTestPortType stub = new WsaTestService().getWsaTestPort();
        return stub;
    }

    public static  Dispatch<SOAPMessage> createDispatchWithWSDLWithAddressing() {
        Dispatch<SOAPMessage> dispatch = new WsaTestService().createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, ENABLED_RESPECT_BINDING_FEATURE);
        return dispatch;
    }

    public static  Dispatch<SOAPMessage> createDispatchWithWSDLWithoutAddressing() {
        Dispatch<SOAPMessage> dispatch = new WsaTestService().createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, DISABLED_ADDRESSING_FEATURE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.org/action/echoIn");
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        return dispatch;
    }

    public static  Dispatch<SOAPMessage> createDispatchForNonAnonymousWithWSDLWithoutAddressing() {
        Dispatch<SOAPMessage> dispatch = new WsaTestService().createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, DISABLED_ADDRESSING_FEATURE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "http://example.org/action/echoIn");
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        return dispatch;
    }

    public static  Dispatch<SOAPMessage> createNotifyDispatchWithWSDLWithoutAddressing() {
        Dispatch<SOAPMessage> dispatch = new WsaTestService().createDispatch(PORT_QNAME, SOAPMessage.class, Service.Mode.MESSAGE, DISABLED_ADDRESSING_FEATURE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, TestConstants.NOTIFY_ACTION);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        return dispatch;
    }


    private static final String NAMESPACE_URI = "http://example.org";
    private static final QName PORT_QNAME = new QName(NAMESPACE_URI, "wsaTestPort");

    private static final AddressingFeature DISABLED_ADDRESSING_FEATURE = new AddressingFeature(false);
    private static final RespectBindingFeature ENABLED_RESPECT_BINDING_FEATURE = new RespectBindingFeature(true);

}
