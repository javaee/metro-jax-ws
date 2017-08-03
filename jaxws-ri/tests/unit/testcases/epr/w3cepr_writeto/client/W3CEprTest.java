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

package epr.w3cepr_writeto.client;

import junit.framework.TestCase;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Rama Pulavarthi
 */
public class W3CEprTest extends TestCase {
    public final String epr = "<EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "<Address>in-vm://epr.epr_webparam.server/?AddNumbersPort</Address><ReferenceParameters/>" +
            "<Metadata xmlns:wsdli=\"http://www.w3.org/ns/wsdl-instance\" wsdli:wsdlLocation=\"http://foobar.org/ AddNumbersService.wsdl\">" +
            "<wsam:InterfaceName xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:wsns=\"http://foobar.org/\">wsns:AddNumbers</wsam:InterfaceName>" +
            "<wsam:ServiceName xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:wsns=\"http://foobar.org/\" EndpointName=\"AddNumbersPort\">wsns:AddNumbersService</wsam:ServiceName>" +
            "</Metadata></EndpointReference>";
    public void testWriteTo() {
        StreamSource source = new StreamSource(new ByteArrayInputStream(epr.getBytes()));
        W3CEndpointReference epr = new W3CEndpointReference(source);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        epr.writeTo(new StreamResult(baos));
        System.out.println(baos.toString());
        //dummy assertion
        assertTrue(baos.toString().contains("xmlns:wsns=\"http://foobar.org/\""));

    }
}
