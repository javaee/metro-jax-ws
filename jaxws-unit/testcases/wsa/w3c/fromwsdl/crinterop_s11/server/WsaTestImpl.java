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

package wsa.w3c.fromwsdl.crinterop_s11.server;

import javax.jws.WebService;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.WebServiceException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPException;
import javax.xml.namespace.QName;

import com.sun.xml.ws.api.SOAPVersion;

@WebService(endpointInterface="wsa.w3c.fromwsdl.crinterop_s11.server.WsaTestPortType")
public class WsaTestImpl implements WsaTestPortType {

    private final String NAME = getClass().getName();

    public void notify(String param) {
        System.out.println(NAME + ".notify: " + param);
    }

    public String echo(String param) {
        System.out.println(NAME + ".echo: " + param);

        String message = null;

        if (param.equals(""))
            message = "The echo string was empty";

       if (param.startsWith("fault"))
           message = "\"" + param + "\" triggered the fault";

        if (message != null)
            throw new SOAPFaultException(createSOAPFault(message));

        return param;
    }

    SOAPFault createSOAPFault(String message) {
        try {
            SOAPFault fault = SOAPVersion.SOAP_11.saajSoapFactory.createFault();
            fault.setFaultString(message);
            fault.setFaultCode(new QName("http://example.org/echo", "EmptyEchoString"));

            return fault;
        } catch (SOAPException ex) {
            throw new WebServiceException(ex);
        }
    }
}